$(document).on("click", ".delete-element", function() {
    var elem = $(this),
        deleteLink = elem.attr("delete-link");

    if (confirm("Are you sure you want to delete?")) {
        $.ajax({
            url: prepareURL(deleteLink),
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
    var fragment = Backbone.history.fragment;
    Backbone.history.fragment = null;
    router.navigate(fragment, {trigger: true});
    showSelectingModal3(data.bin, data.unmarkedSubmission.id);
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
/*
$(document).on("click", ".toggle-mark", function () {
    var $this = $(this),
        markLink = $this.attr("marking-link"),
        elem = $this.closest('li'),
        isSubPanel = elem.parents('.sublist-container').size() > 0,
        marked = $this.parents('.marked').size() > 0,
        updateUI = function () {
            var clone = elem.clone().removeClass("marked").removeClass("unmarked"),
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
            if (marked) {
                elem.removeClass("marked").addClass("unmarked");
            }
            else {
                elem.removeClass("unmarked").addClass("marked");
            }
        }
    if (isSubPanel) {
        $.post(markLink, updateUISubPanel).fail(updateUISubPanel);
    } else {
        $.post(markLink, updateUI).fail(updateUI);
    }
})*/

var isTogglingMark = false;

function toggleMarkPanel(elem, marked) {

    var clone = elem.clone().removeClass("marked").removeClass("unmarked"),
        parentSection = elem.closest('section'),
        parentElem = marked ? parentSection.find('.unmarked-top') : parentSection.find('.marked-top'),
        container = parentElem.find(".panels");


    clone.find(".sublist-container").attr("loaded", false).css("display", "none");
    clone.css("display", "none");


    var toggleButton = clone.find('.list-panel .toggle-mark');
    if (marked) {
        toggleButton.text('.');
    } else {
        toggleButton.text("'");
    }

    clone.prependTo(container);



    elem.fadeOut(function() {
        clone.fadeIn("slow", function() {
            isTogglingMark = false;
        });
    })
}

function toggleMarkSubPanel(elem, marked) {
    var toggleButton = elem.find('.list-inner-sub-panel .toggle-mark');
    if (marked) {
        elem.removeClass("marked").addClass("unmarked");
        toggleButton.text('.');
    }
    else {
        elem.removeClass("unmarked").addClass("marked");
        toggleButton.text("'");
    }

    var parentElem = elem.parents('li').first();
    var shouldUpdateParent = false;
    if (marked) {
        shouldUpdateParent = parentElem.parents('.marked').size() != 0;
    } else {
        shouldUpdateParent = parentElem.find('.unmarked').size() == 0;
    }

    if (shouldUpdateParent) {
        toggleMarkPanel(parentElem, marked);
    } else {
        isTogglingMark = false;
    }


}


$(document).on("click", ".toggle-mark", function () {
    var $this = $(this),
        markLink = prepareURL($this.attr("marking-link")),
        elem = $this.closest('li'),
        isSubPanel = elem.parents('.sublist-container').size() > 0,
        marked = $this.parents('.marked').size() > 0;

    if (isTogglingMark) {
        return false;
    }
    isTogglingMark = true;
    if (isSubPanel) {
        $.post(markLink, function() { toggleMarkSubPanel(elem, marked);})
    } else {
        $.post(markLink, function() { toggleMarkPanel(elem, marked);})
    }
})


$(document).on("click", ".delete-user-perm", function() {
    var user = $(this).attr("data-delete");
    $.ajax({
        url: prepareURL("/bins/" + getRouteParams()[0] + "/permissions?users[]=" + user),
        method: 'DELETE'
    }).done(function() {
       asyncLoad($('.permissions-container.async-loader'));
    })
})

$(document).on("click", ".tabs.magic-tabs .title a", function(evt){
    var link = $(this).attr("href");
    router.navigate(link);

    var  sublists = $(this).closest('section').find('.sublist-container');
    sublists.css("display", "none");

    var loaders = $(this).closest('section').find('.async-loader');
    asyncLoad(loaders);

})

function setupAutocomplete() {

    $('.token-user-input').autocomplete({
        source: prepareURL("hack/users"),
        minLength: 2,
        focus: function( event, ui) {
            console.log(ui);

        },
        select: function(event, ui) {
            console.log(ui);
        }
    }).data( "ui-autocomplete" )._renderItem = function( ul, item ) {
        return $( "<li>" + "<a><div style='display: inline-block; padding-left: 10px;'><div class='full_name'>" + item.name + " (" + item.crsid + ")</div><div class='email'>" + item.crsid + "@cam.ac.uk</div></div></a></li>" ).appendTo(ul);
    }

}

moduleScripts['handins'] = {
    'marking': {
        'index': [function () {
            $(".upload-marked-work-form form").ajaxForm(function(data) {
                console.log(data);
                var fragment = Backbone.history.fragment;
                Backbone.history.fragment = null;
                router.navigate(fragment, {trigger: true});
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
        }, setupAutocomplete]
    },
    'submission': {
        'index': []
    }
}
