var MarkerOverlay = Backbone.View.extend({

    initialize: function() {
        this.pdfViewer = $('.pdf-viewer');
        this.pdfContainer = this.pdfViewer.find('#viewerContainer');
        //this.selecting = false;

        // Bind all events handlers to the marker
        //this.setupBindings();
        _.bindAll(this,
            'enableMarking',
            'startSelecting',
            'stopSelecting',
            'handleMouseMove',
            'handleResizableResize',
            'handleRescale',
            'handlePositionTopClick',
            'handlePositionBottomClick'
        );

        /*this.model.on("change:question", function(evt) {
            this.set("hidden", false);
            this.set("name", this.get("question").get("name"))
        })*/

        this.model.on("change:selecting", this.enableMarking);

    },

    setupBindings: function () {
        this.startSelecting = bindThis(this.startSelecting, this);
        this.stopSelecting = bindThis(this.stopSelecting, this);

        this.handleMouseMove = bindThis(this.handleMouseMove, this);
        this.handleResizableResize = bindThis(this.handleResizableResize, this);
        this.handleRescale = bindThis(this.handleRescale, this);

        this.handlePositionTopClick = bindThis(this.handlePositionTopClick, this);
        this.handlePositionBottomClick = bindThis(this.handlePositionBottomClick, this);
    },

    enableMarking: function (marker, selecting) {
        if (selecting)
            $('.pdf-viewer .page').click(this.startSelecting);
    },

    calculateDistance: function (elem, evt, scrollParent) {
        // distance from the top of the page
        var dist = evt.pageY - elem.offset().top;

        // elem.position().top gives the top of the page in the viewing area
        // elem.scrollParent().scrollTop() gives the scroll
        // But really there should be a better way of doing this
        var top;
        if (scrollParent === undefined || scrollParent === null)
            top = elem.position().top + dist + elem.scrollParent().scrollTop();
        else
            top = dist + scrollParent.scrollTop();

        return top;
    },

    handleRescale: function(evt) {
        this.updateSize();
        this.updatePosition();
    },

    parse: function (value) {
        return parseFloat(value.replace('px', ''));
    },

    updatePosition: function() {
        var currentWidth = this.parse(this.page.css("width")),
            prevWidth = this.pageWidth,
            scaleFactor = currentWidth / prevWidth;
        this.top *= scaleFactor;
        this.marker.css("top", this.top + "px");
        this.savePageWidth();
    },

    updateSize: function () {
        var pageMargin = this.parse(this.page.css("margin-left"));
        var pageBorder = this.parse(this.page.css("border-left-width"));

        this.marker.css("margin-left", pageMargin + pageBorder + "px");

        // Now lets fix the width

        this.marker.css("width", this.page.css("width"))
    },

    savePageWidth: function () {
        this.pageWidth = this.parse(this.page.css("width"));
    },

    startSelecting: function(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        //this.selecting = true;

        $('.pdf-viewer .page').unbind("click", this.startSelecting);

        var elem = $(evt.currentTarget);
        this.page = elem;
        this.savePageWidth();

        // Setup the marker
        this.$el.replaceWith($('<div class="marker"></div>'));
        this.$el.appendTo(this.pdfContainer);

        // Setup zindex so we can see it.

        this.$el.zIndex(this.pdfContainer.find('.page').zIndex() + 1);


        this.top = this.calculateDistance(elem, evt);
        this.model.set('top', top);


        this.$el.css("top", this.top + "px");

        this.updateSize();


        // Set up mouse tracking for resize

        this.pdfContainer.mousemove(this.handleMouseMove);

        // Lets make the marker resizable.
        this.$el.resizable({
            handles: "n, s",
            // Sync this.top when the element is resized upwards.
            resize: this.handleResizableResize

        });
        $(window).on("scalechange", this.handleRescale);

        this.pdfContainer.click(this.stopSelecting);
    },

    stopSelecting: function(evt) {
        this.pdfContainer.unbind('click', this.stopSelecting);
        this.pdfContainer.unbind('mousemove', this.handleMouseMove);

        //this.selecting = false;
        //TODO
        this.trigger("stopSelecting", this);
    },

    remove: function() {
        this.$el.remove();
    },

    scrollTo: function() {
        this.pdfContainer.scrollTop(this.model.get('top') - 150);
    },

    handleResizableResize:  function (evt, ui) {
        this.top = ui.position.top;
        this.model.set('top', this.top)
    },

    handleMouseMove: function(evt) {
        var elem = $(evt.currentTarget);

        bottom = this.calculateDistance(elem, evt, elem);

        if (bottom - this.top > 0) {
            this.marker.css("height", (bottom - this.top) + "px");
            this.model.set("height", bottom - this.top);
        }

       // console.log(bottom - this.top);
    },

    handlePositionTopClick: function (evt) {
        this.pdfContainer.find('.page').unbind('click', this.handlePositionTopClick);

        console.log("Hello")

    },

    handlePositionBottomClick: function (evt) {
        this.pdfContainer.find('.page').unbind('click', this.handlePositionBottomClick);

        console.log("World");

    },

    calculateAbsolutePosition: function (page, dist, callback) {
        var height = $('#pageContainer' + page).height();

        PDFView.getPage(page).then( function (page) {
            var trueHeight = page.getViewport(1).height;

            var result = 1 - (dist / height);
            console.log(result);
            callback(result);
        })
    },

    hitTest: function (dist, callback) {
        var borderSize = this.parse(this.page.css('border-top-width'));

        var resultElem, resultDist;

        this.pdfContainer.find('.page').each(function(index, _elem) {
            var elem = $(_elem);
            var size = parseFloat(elem.css('height').replace('px', ''));

            dist -= borderSize;

            if (dist < size && dist > 0) {
                resultElem = elem;
                resultDist = dist;
            }
            dist -= size;
        })

        var page = parseInt(resultElem.attr('id').replace('pageContainer', ''))

        this.calculateAbsolutePosition(page, resultDist, function(absolutePosition) {
            callback({
                page: page,
                absolutePosition: absolutePosition,
            })
        })

    },

    getPosition:  function (callback) {
        var height = this.parse(this.marker.css('height'));
        var _this = this;

        this.hitTest(this.top, function(start) {
            _this.hitTest(_this.top + height, function(end) {
                callback({
                    start: start,
                    end: end,
                })
            })
        })
    }

})

