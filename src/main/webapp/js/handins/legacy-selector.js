
function addSelectingModal(bin, submission) {
    var html = shared.handins.selectingModal2({bin: bin, submission: submission}),
        elem = $(html);

    elem.prependTo($('body')).foundation().foundation("reveal","open");
    asyncLoad(elem.find(".async-loader"));
    return elem;
}

$(document).on("click", ".input-line a", function() {
    $(this).closest('.input-line').remove();
})

function showPdf2(submission) {
    PDFJS.disableWorker = true;
    PDFJS.getDocument("/api/submissions/" + submission + "/download").then(function (pdf){
        var numPages = pdf.numPages;

        for (var i=0; i < numPages; i++) {
            $('<div class="pdf-page page-' + i + '"><canvas></canvas></div>' ).appendTo($('.pdf-container'));

            pdf.getPage(i).then(function(i){ return function(page) {
                var scale = 1;
                var viewport = page.getViewport(scale);

                var canvas = $(".page-" + i +" canvas")[0];
                var context = canvas.getContext('2d');

                canvas.height = viewport.height;
                canvas.width = viewport.width;

                page.render({canvasContext: context, viewport: viewport});

            }}(i))

        }

    })
}

function showSelectingModal(bin, submission) {
    $('#selectingModal').remove();
    addSelectingModal(bin, submission);
    showPdf2(submission);
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
