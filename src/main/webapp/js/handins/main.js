$(document).on("click", ".delete-submission", function() {
    var elem = $(this),
        deleteLink = elem.attr("delete-link");

    if (confirm("Are you sure you want to delete the submission?")) {
        $.ajax({
            url: deleteLink,
            type: 'DELETE',
            success: function(result) {
                elem.parents('.delete-me').first().fadeOut();
            },
            fail: function(err) {
                console.log(err);
            }
        });
    }
})

$(document).on("click", ".upload-work-for-bin", function() {
    var elem = $(this),
        bin = elem.attr("bin");

    loadModule($('.upload-work').first(), "bins/" + bin, "shared.handins.uploadForm", function() {
        this.slideToggle();
    });
})

$(".upload-work-form form").ajaxForm(uploadedSubmission);

function uploadedSubmission(data) {
    Backbone.history.fragment = null;
    router.navigate(window.location.hash, {trigger: true});
    showSelectingModal(data.bin, data.unmarkedSubmission.id);
}




$(document).on("click", ".expand-sub-list", function() {

  var elem = $(this).closest(".list-panel").siblings(".sublist-container");
  if (elem.attr("loaded") == "true") {
    elem.slideToggle();
    return;
  }

  var template;
  if ($(this).attr("template-name"))
    template = $(this).attr("template-name");
  else if ($(this).attr("template-function")) {
    template = getTemplate($(this).attr("template-function"));
  }

  loadModule(elem, $(this).attr("data-location"), template, function() {
    elem.slideToggle();
  });
  elem.attr("loaded", "true");
})

$(document).on("click", ".toggle-mark", function () {
    var $this = $(this),
        markLink = $this.attr("marking-link"),
        elem = $this.closest('li'),
        isSubPanel = elem.parents('.sublist-container').size() > 0,
        marked = $this.parents('.marked').size() > 0,
        updateUI = function () {
            var clone = elem.clone(),
                parentElem = marked ? $('.unmarked') : $('.marked'),
                container = parentElem.find(".panels");


            clone.find(".sublist-container").attr("loaded", false).css("display", "none");
            clone.css("display", "none");

            clone.prependTo(container);

            elem.fadeOut(function() {
                clone.fadeIn("slow");
            })
        },
        updateUISubPanel = function() {
            if (marked)
                elem.removeClass("marked").addClass("unmarked");
            else
                elem.removeClass("unmarked").addClass("marked");
        }
    if (isSubPanel) {
        $.post(markLink, updateUISubPanel).fail(updateUISubPanel);
    } else {
        $.post(markLink, updateUI).fail(updateUI);
    }
})

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
    PDFJS.disableWorker = true;
    PDFJS.getDocument("/submissions/" + submission + "/download").then(function (pdf){
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

$(document).on("click", ".delete-user-perm", function() {
    var user = $(this).attr("data-delete");
    $.ajax({
        url: "/bins/" + getRouteParams()[0] + "/permissions?users[]=" + user,
        method: 'DELETE'
    }).done(function() {
       asyncLoad($('.permissions-container.async-loader'));
    })
    $('.update-permissions form').ajaxSubmit();
})

$(document).on("click", ".tabs.magic-tabs .title a", function(evt){
    var link = $(this).attr("href");
    router.navigate(link);

    var  sublists = $(this).closest('section').find('.sublist-container');
    sublists.css("display", "none");

    var loaders = $(this).closest('section').find('.async-loader');
    asyncLoad(loaders);

})

moduleScripts['handins'] = {
    'marking': {
        'index': [function () {
            $(".upload-marked-work-form form").ajaxForm(function(data) {
                console.log(data);
                Backbone.history.fragment = null;
                router.navigate(window.location.hash, {trigger: true});
            })
        }]
    },
    'bin': {
        'create': [function() {
            $(".create-bin form").ajaxForm(function(data) {
                var id = data.id;
                router.navigate("/bins/" + id, { trigger: true });
            })
        }],
        'permissions': [function(){
            $("form").ajaxForm(function() {
               asyncLoad($('.container .async-loader'));
               $('.container form input[type="text"]').val("");
            });
        }]
    }
}
