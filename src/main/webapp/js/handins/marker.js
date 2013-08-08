bindThis = function(fn, me) {
    return function() {
        return fn.apply(me, arguments);
    };
};

var Marker = (function MarkerClosure(){

    function Marker(pdfViewer) {
        this.pdfViewer = pdfViewer;
        this.pdfContainer = pdfViewer.find('#viewerContainer');
        this.selecting = false;

        // Bind all events handlers to the marker
        this.setupBindings();
    }

    Marker.prototype.setupBindings = function () {
        this.startSelecting = bindThis(this.startSelecting, this);
        this.stopSelecting = bindThis(this.stopSelecting, this);

        this.handleMouseMove = bindThis(this.handleMouseMove, this);
        this.handleResizableResize = bindThis(this.handleResizableResize, this);
        this.handleRescale = bindThis(this.handleRescale, this);

        this.handlePositionTopClick = bindThis(this.handlePositionTopClick, this);
        this.handlePositionBottomClick = bindThis(this.handlePositionBottomClick, this);
    }

    Marker.prototype.enableMarking =  function () {
        $('.pdf-viewer .page').click(this.startSelecting);
    }

    Marker.prototype.calculateDistance = function (elem, evt, scrollParent) {
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
    }

    Marker.prototype.handleRescale = function(evt) {
        this.updateSize();
        this.updatePosition();
    }

    Marker.prototype.parse = function (value) {
        return parseFloat(value.replace('px', ''));
    }

    Marker.prototype.updatePosition = function() {
        var currentWidth = this.parse(this.page.css("width")),
            prevWidth = this.pageWidth,
            scaleFactor = currentWidth / prevWidth;
        this.top *= scaleFactor;
        this.marker.css("top", this.top + "px");
        this.savePageWidth();
    }
    Marker.prototype.updateSize = function () {
        var pageMargin = this.parse(this.page.css("margin-left"));
        var pageBorder = this.parse(this.page.css("border-left-width"));

        this.marker.css("margin-left", pageMargin + pageBorder + "px");

        // Now lets fix the width

        this.marker.css("width", this.page.css("width"))
    }

    Marker.prototype.savePageWidth = function () {
        this.pageWidth = this.parse(this.page.css("width"));
    }

    Marker.prototype.startSelecting =  function(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        this.selecting = true;

        $('.pdf-viewer .page').unbind("click", this.startSelecting);

        var elem = $(evt.currentTarget);
        this.page = elem;
        this.savePageWidth();

        // Setup the marker
        this.marker = $('<div class="marker"></div>');
        this.marker.appendTo(this.pdfContainer);

        // Setup zindex so we can see it.

        this.marker.zIndex(this.pdfContainer.find('.page').zIndex() + 1);


        this.top = this.calculateDistance(elem, evt);


        this.marker.css("top", this.top + "px");

        this.updateSize();


        // Set up mouse tracking for resize

        this.pdfContainer.mousemove(this.handleMouseMove);

        // Lets make the marker resizable.
        this.marker.resizable({
            handles: "n, s",
            // Sync this.top when the element is resized upwards.
            resize: this.handleResizableResize

        });
        $(window).on("scalechange", this.handleRescale);

        this.pdfContainer.click(this.stopSelecting);
    }

    Marker.prototype.stopSelecting = function(evt) {
        this.pdfContainer.unbind('click', this.stopSelecting);
        this.pdfContainer.unbind('mousemove', this.handleMouseMove);

        this.selecting = false;
    }

    Marker.prototype.handleResizableResize =  function (evt, ui) {
        this.top = ui.position.top;
    }

    Marker.prototype.handleMouseMove = function(evt) {
        var elem = $(evt.currentTarget);

        bottom = this.calculateDistance(elem, evt, elem);

        if (bottom - this.top > 0)
            this.marker.css("height", (bottom - this.top) + "px");

        console.log(bottom - this.top);
    }

    Marker.prototype.handlePositionTopClick = function (evt) {
        this.pdfContainer.find('.page').unbind('click', this.handlePositionTopClick);

        console.log("Hello")

    }

    Marker.prototype.handlePositionBottomClick = function (evt) {
        this.pdfContainer.find('.page').unbind('click', this.handlePositionBottomClick);

        console.log("World");

    }

    Marker.prototype.calculateAbsolutePosition = function (page, dist) {
        var height = $('#pageContainer' + page).height();

        PDFView.getPage(page).then( function (page) {
            var trueHeight = page.getViewport(1).height;

            console.log(trueHeight * dist / height);
        })
    }

    Marker.prototype.hitTest = function (dist) {
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

        return {
            page: page,
            dist: resultDist,
            absolutePosition: this.calculateAbsolutePosition(page, resultDist)
        }

    }

    Marker.prototype.getPosition =  function () {
        var height = this.parse(this.marker.css('height'));

        return {
            'start': this.hitTest(this.top),
            'end': this.hitTest(this.top + height)
        }
    }


    return Marker;
})();

var MarkerModel = Backbone.Model.extend({
    questions: [
        "Ana",
        "are",
        "mere"
    ],

    initialize: function() {
        this.marker = new Marker($('.pdf-viewer'));
    }

});

var MarkerCollection = Backbone.Collection.extend({
    model: MarkerModel,

});
