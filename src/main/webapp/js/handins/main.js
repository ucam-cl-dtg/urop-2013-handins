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

    loadModule($('.upload-work').first(), "bin/" + bin, "shared.handins.uploadForm", function() {
        this.slideToggle();
    });
})

$(".upload-work-form form").ajaxForm(uploadedSubmission);

function uploadedSubmission(data) {
    console.log(data);
    Backbone.history.fragment = null;
    router.navigate(window.location.hash, {trigger: true});
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
