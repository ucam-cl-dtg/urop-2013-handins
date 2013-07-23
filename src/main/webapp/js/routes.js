/* Demo Routes:

The first term must be the route from where the template will get the data.
The second term must be either a string representing the template name or
a function that returns the template name. The function will receive the json returned
by the request as the first parameter.
*/

function binInjector(templateName) {
    var bin = null;
    isCached = function() {
        if (bin == null)
            return false;
        if (bin.id != getRouteParams()[0])
            return false;
        return true;
    }

    updateBin = function() {
        $.ajax("/bin/" + getRouteParams()[0], {
            async: false,
            success: function(res) {
               bin = res.bin;
            }
        });
        console.log(bin);

    }

    getBin = function() {
        return bin;
    }

    return function(json) {
        if (! isCached())
            updateBin();
        json.bin = getBin();

        return templateName;
    }
}

function binList (json) {
    json.elems = json.bins;
    _.map(json.elems, function(elem) {
        elem.uploadTo = elem.id;
        elem.sublist = "submission/bin/" + elem.id;
        elem.sublistTemplateFunction = "submissionSubList";

        return elem;
    });
    return "shared.handins.generic.listPanel";
}

function submissionSubList(json) {
    json.elems = json.submissions;
    json.subPanel = true;

    _.map(json.elems, function(elem) {
        elem.name = "Submission " + elem.id;
        elem.delete = "/submission/" + elem.id;
        elem.download = "/submission/" + elem.id;

    })

    return "shared.handins.generic.listPanel";
}

function submissionList(json) {
    json.elems = json.submissions;

    _.map(json.elems, function(elem) {
        elem.name = "Submission " + elem.id;
        elem.delete = "/submission/" + elem.id;
        elem.download = "/submission/" + elem.id;
    })
}

function combine(f1 , f2) {
    return function(json) {
        f1(json);
        return f2(json);
    }
}

$(document).ready(function() {
    router = Router({
        //"tester": function(json) { return json['isSupervisor'] ? "a" : "b";}
        // Use the last line to redirect unmatched routes to an error page
        "submission/bin/:id": combine(submissionList, binInjector("handins.submission.index")),

        //"bin": "handins.bin.index",
        "bin": binList,
        "marking/:binId/student": binInjector("handins.marking.student"),


        //"*undefined": "main.test"
    })
})


