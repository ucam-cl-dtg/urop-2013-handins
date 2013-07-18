$(document).on("click", ".delete-submission", function() {
    var elem = $(this),
        deleteId = elem.attr("delete-id"),
        binId = getRouteParams()[0],
        deleteLink = "/submission/" + binId + "/" + deleteId;

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

    loadModule($('.upload-work'), "bin/" + bin, "handins.bin.uploadForm");
})

function uploadedSubmission(data) {
    console.log(data);
}


