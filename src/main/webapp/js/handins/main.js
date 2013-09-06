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
            error: function(req) {
                var msg;
                try {
                    msg =JSON.parse(req.responseText).message;
                } catch (err) {
                }
                if (msg == undefined)
                    errorNotification("Couldn't delete");
                else
                    errorNotification(msg);
                console.log(req);
            }
        });
    }
})

$(document).on("click", ".upload-work-for-bin", function() {
    var elem = $(this),
        bin = elem.attr("bin");

    /*loadModule($('.upload-work').first(), "bins/" + bin, "shared.handins.uploadForm", function() {
        this.slideDown();
    });*/
    router.navigate("bins/" + bin + "/submissions", {trigger: true});
})

$(".upload-work-form form").ajaxForm(uploadedSubmission);

function uploadedSubmission(data) {
    showSelectingModal3(data.bin, data.unmarkedSubmission.id);
}


$(document).on("click", ".bin .edit-element", function(evt) {
    var id = $(this).closest('li').data('id');
    console.log(id);
    router.navigate("bins/" + id, {trigger: true});
})

$(document).on("click", ".submission .edit-element", function(evt) {
    var bin = parseInt(getRouteParams());
    var submission = parseInt($(this).closest('li').data('id'));

    showSelectingModal3(bin, submission);
});

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
        source: function(req, resp) {
            $.get(prepareURL("hack/users"), req, function(data){
                _.map(data, function(elem) {
                    elem.label = elem.value = elem.crsid;
                })
                resp(data);
            })
        },
        minLength: 3,
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
function searchForm(evt) {
    evt.preventDefault();
    var form = $(this);
    var inputs = form.find('input[type!="submit"]');
    var selector = "?";
    var query = "";
    var base = Backbone.history.fragment;
    if (base.indexOf("?") != -1)
        base = base.slice(0, base.indexOf("?"));

    _.each(inputs, function(input) {
        var val = $(input).val();
        if (val == undefined || val == "")
            return;

        if ($(input).attr('type') == 'radio' && !input.checked)
            return;

        query += selector + $(input).attr('name') + "=" + escape(val);
        selector = "&";
    });

    router.navigate(base + query, {trigger: true})

}

function basicSearch() {
    $('a.toggle-search').data('type', 'basic').text("Advance Search");
    $('.advance-search').fadeOut(function() {
        $('.name-row').removeClass("large-12").addClass("large-9");
    });
    $('.main input[name="type"]').val("basic");
}

function advanceSearch() {
    $('a.toggle-search').data('type', 'advance').text("Basic Search");
    $('.advance-search').fadeIn(function() {
    });
    $('.main input[name="type"]').val("advance");
    $('.name-row').removeClass("large-9").addClass("large-12");
}

function updateSearch() {
    var type = $('.main input[name="type"]').val();
    if (type == "basic")
        basicSearch();
    else
        advanceSearch();
}

function toggleSearch(evt) {
    evt.preventDefault();
    evt.stopPropagation();

    var type = $('.main input[name="type"]').val();
    if (type == "basic") {
        $('.main input[name="type"]').val("advance");
    } else {
        $('.main input[name="type"]').val("basic");
    }

    updateSearch();
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
        'index': [function() {
            paginate($('.pagination'));
            $('.main input.date').datepicker({
                dateFormat: "dd/mm/yy"
            });
            $('.main form').submit(searchForm);
            $('.main a.toggle-search').click(toggleSearch);
            updateSearch();
        }],
        'create': [function() {
            $(".create-bin form").ajaxForm(function(data) {
                var id = data.id;
                router.navigate("/bins/" + id, { trigger: true });
            })
        }],
        'edit': [function() {
            pageView = new BinEditView({
                el: $('.main')
            })
            pageView.render()
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
