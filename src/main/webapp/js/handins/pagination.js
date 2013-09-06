console.log("hello")

var PAGES_TO_DISPLAY = 10;


function generatePaginator(elem, current, pages) {
    var before = PAGES_TO_DISPLAY / 2;
    if (before >= current)
        before = current - 1;

    var after = PAGES_TO_DISPLAY - before - 1;

    if (current + after > pages) {
        after = pages - current;
    }

    var html = handins.bin.paginator({
        pages: _.range(current - before, current + after + 1),
        current: current
    })

    elem.html(html);
}

function updateQuery(uri, key, value) {
    var re = new RegExp("([?|&])" + key + "=.*?(&|$)", "i");
    separator = uri.indexOf('?') !== -1 ? "&" : "?";
    if (uri.match(re)) {
        return uri.replace(re, '$1' + key + "=" + value + '$2');
    }
    else {
        return uri + separator + key + "=" + value;
    }
}

function paginate(elem) {
    var items = parseInt(elem.data("count"));
    var offset = parseInt(elem.data("offset"));
    var PAGE_SIZE = parseInt(elem.data("limit"));

    var pages = Math.ceil(items / PAGE_SIZE);
    var page = 1 + Math.floor(offset / PAGE_SIZE);


    generatePaginator(elem, page, pages);

    function changePage(page) {
        var fragment = Backbone.history.fragment;
        if (fragment.indexOf("?") == -1)
            fragment += window.location.search;
        fragment = updateQuery(fragment, "offset", (page - 1) * PAGE_SIZE );
        fragment = updateQuery(fragment, "limit", PAGE_SIZE);

        router.navigate(fragment, {trigger: true});
    }

    elem.find("a.page").click(function(evt) {
        evt.preventDefault();
        evt.stopPropagation();

        var page = parseInt($(evt.target).data("page"));
        changePage(page)
    })

    elem.find(".first a").click(function(evt) {
        evt.preventDefault();
        evt.stopPropagation();

        changePage(1);
    })

    elem.find(".last a").click(function(evt) {
        evt.preventDefault();
        evt.stopPropagation();

        changePage(pages);
    })
}
