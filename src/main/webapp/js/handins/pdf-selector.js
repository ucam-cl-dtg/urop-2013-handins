/**
* For the brave souls who get this far: You are the chosen ones,
* the valiant knights of programming who toil away, without rest,
* fixing our most awful code. To you, true saviors, kings of men,
* I say this: never gonna give you up, never gonna let you down,
* never gonna run around and desert you. Never gonna make you cry,
* never gonna say goodbye. Never gonna tell a lie and hurt you.
*/

function addSelectingModal(bin, submission) {
    var html = shared.handins.selectingModal({bin: bin, submission: submission}),
        elem = $(html);

    elem.prependTo($('body')).foundation().foundation("reveal","open");
    asyncLoad(elem.find(".async-loader"));
    return elem;
}


function showSelectingModal(bin, submission) {
    $('#selectingModal').remove();
    addSelectingModal(bin, submission);
    showPdf(submission);
    $('#selectingModal form').ajaxForm(function(){
        $('#selectingModal').foundation("reveal", "close");
    });

    $('#selectingModal .more-inputs').click(function() {
        var id = $('#selectingModal select').val();
        var name = $('#selectingModal select option[value="' + id + '"]').text();
        var start = $('#selectingModal input[name="start"]').val();
        var end = $('#selectingModal input[name="end"]').val();

        var elem = $(shared.handins.inputLine({
            "question": { id: id, name: name},
            "start": start,
            "end": end
        }))
        elem.appendTo($('.input-line-container'))
    });

}

$(document).on("click", ".input-line a", function() {
    $(this).closest('.input-line').remove();
})

function showPdf(submission) {
    // Function located in viewer.js. That file contains magic, pure voodoo magic.
    loadPdf("/submissions/" + submission + "/download");
}

bindThis = function(fn, me) {
    return function() {
        return fn.apply(me, arguments);
    };
};
var Marker = (function MarkerClosure(){

    function Marker(pdfViewer) {
        this.pdfViewer = pdfViewer;
        this.pdfContainer = pdfViewer.find('#viewerContainer');

        // Bind all events handlers to the marker
        this.startSelecting = bindThis(this.startSelecting, this);
        this.handleMouseMove = bindThis(this.handleMouseMove, this);
        this.handleResizableResize = bindThis(this.handleResizableResize, this);
        this.handleRescale = bindThis(this.handleRescale, this);
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

    return Marker;
})();

