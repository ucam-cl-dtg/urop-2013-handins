var MarkerOverlay = Backbone.View.extend({

    initialize: function() {
        this.pdfViewer = $('.pdf-viewer');
        this.pdfContainer = this.pdfViewer.find('#viewerContainer');

        // Bind all events handlers to the marker
        _.bindAll(this,
            'enableMarking',
            'startSelecting',
            'stopSelecting',
            'handleMouseMove',
            'handleResizableResize',
            'handleRescale',
            'updateHeight',
            'updateTop',
            'updateWidth',
            'updateScale'
        );

        /*this.model.on("change:question", function(evt) {
            this.set("hidden", false);
            this.set("name", this.get("question").get("name"))
        })*/

        this.model.on("change:selecting", this.enableMarking);
        this.model.on("change:top", this.updateTop);
        this.model.on("change:height", this.updateHeight);
        this.model.on("change:width", this.updateWidth);
        this.model.on("change:scale", this.updateScale);

    },

    updateScale: function() {
        var prevScale = this.model.previous('scale'),
            curScale = this.model.get('scale');

        if (prevScale === undefined || prevScale === null)
            return ; // First time the scale is set we don't do anything

        var scaleFactor = curScale / prevScale;
        this.model.set('top', this.model.get('top') * scaleFactor);
        this.model.set('height', this.model.get('height') * scaleFactor);
        this.model.set('width', this.model.get('width') * scaleFactor);
    },
    updateHeight: function() {
        this.$el.height(this.model.get('height'));
    },

    updateTop: function() {
        this.$el.css('top', this.model.get('top') + "px");
    },

    updateWidth: function() {
        var pageMargin = this.parse(this.page.css("margin-left"));
        var pageBorder = this.parse(this.page.css("border-left-width"));

        this.$el.css("margin-left", pageMargin + pageBorder + "px");

        // Now lets fix the width

        this.$el.css("width", this.model.get('width') + 'px');
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
        this.model.set('scale', PDFView.currentScale);
    },

    parse: function (value) {
        return parseFloat(value.replace('px', ''));
    },


    startSelecting: function(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        //this.selecting = true;

        $('.pdf-viewer .page').unbind("click", this.startSelecting);

        var elem = $(evt.currentTarget);
        this.page = elem;

        // Setup the marker
        this.$el.addClass("marker");
        this.$el.appendTo(this.pdfContainer);

        // Setup zindex so we can see it.

        this.$el.zIndex(this.pdfContainer.find('.page').zIndex() + 1);


        this.model.set('top', this.calculateDistance(elem, evt));
        this.model.set('width', this.parse(this.page.css("width")));
        this.model.set('scale', PDFView.currentScale);


        // Set up mouse tracking for resize

        this.pdfContainer.mousemove(this.handleMouseMove);

        // Lets make the marker resizable.
        this.$el.resizable({
            handles: "n, s",
            // Sync this.top when the element is resized upwards.
            resize: this.handleResizableResize,
            containment: this.pdfContainer

        });
        $(window).on("scalechange", this.handleRescale);

        this.pdfContainer.click(this.stopSelecting);
    },

    stopSelecting: function(evt) {
        this.pdfContainer.unbind('click', this.stopSelecting);
        this.pdfContainer.unbind('mousemove', this.handleMouseMove);

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
        this.model.set('top', ui.position.top);
    },

    handleMouseMove: function(evt) {
        var elem = $(evt.currentTarget);
        var top = this.model.get('top');

        bottom = this.calculateDistance(elem, evt, elem);

        if (bottom - top > 0) {
            this.model.set("height", bottom - top);
        }

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
        var height = this.parse(this.$el.css('height'));
        var _this = this;

        this.hitTest(this.model.get('top'), function(start) {
            _this.hitTest(_this.model.get('top') + height, function(end) {
                callback({
                    start: start,
                    end: end,
                })
            })
        })
    }

})

