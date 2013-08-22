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
        $.ajax(prepareURL("/bins/" + getRouteParams()[0]), {
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
        elem.sublist = "bins/" + elem.id + "/submissions";
        elem.sublistTemplateFunction = "submissionSubList";
        elem.linkTo = "bins/" + elem.id + "/submissions";

        return elem;
    });
    return "shared.handins.generic.listPanel";
}

function markingList(json) {
    json.elems = json.bins;
    _.map(json.elems, function(elem) {
        elem.linkTo = "marking/bins/" + elem.id + "/students";
        return elem;
    });
    return "shared.handins.generic.listPanel";

}

function submissionSubList(json) {
    json.elems = json.submissions;
    json.subPanel = true;

    _.map(json.elems, function(elem) {
        elem.name = "Submission " + elem.id;
        elem.delete = "/submissions/" + elem.id;
        elem.download = prepareURL("submissions/" + elem.id + "/download");
    })

    return "shared.handins.generic.listPanel";
}

function submissionList(json) {
    json.elems = json.submissions;

    _.map(json.elems, function(elem) {
        elem.name = "Submission " + elem.id;
        elem.delete = "/submissions/" + elem.id;
        elem.sublist = "submissions/" + elem.id;
        elem.sublistTemplateFunction = "questionList";
        elem.download = prepareURL("submissions/" + elem.id + "/download");
    })
}

function questionList(json) {
    json.elems = json.answers;
    json.subPanel = true;

    _.map(json.elems, function(elem) {
        elem.name = elem.question;
        elem.download = prepareURL(elem.link);

    })

    return "shared.handins.generic.listPanel";
}


function combine(f1 , f2) {
    return function(json) {
        f1(json);
        return f2(json);
    }
}

markingStudents = combine(binInjector(), function (json) {

    _.each(json.students,  function(elem) {
        elem.name = elem.student;
        elem.download = prepareURL("/marking/bins/" + json.bin.id + "/students/" + elem.student + "/download");
        elem.sublist = "marking/bins/" + json.bin.id + "/students/" + elem.student;
        elem.sublistTemplateFunction = "markingStudentsQuestion"
        elem.marking = "/marking/bins/" + json.bin.id + "/students/" + elem.student;
    })

    json.unmarkedElems = _.filter(json.students, function(s) { return !s.isMarked; })
    json.markedElems = _.filter(json.students, function(s) { return s.isMarked; })

    return 'handins.marking.listElems';
})


function markingStudentsQuestion(json) {
    json.subPanel = true;

    var bin = getRouteParams()[0];

    json.elems = json.studentQuestions;
    _.each(json.elems, function(elem) {
        elem.id = elem.questionId;
        elem.name = elem.questionName;

        elem.download = prepareURL("marking/bins/" + bin + "/questions/" + elem.id + "/students/" + json.student + "/download");
        elem.marking = "/marking/bins/" + bin + "/questions/" + elem.id + "/students/" + json.student;
    })

    return 'shared.handins.generic.listPanel';
}

markingQuestions = combine(binInjector(), function(json) {

    _.each(json.questionList,  function(elem) {
        elem.name = elem.questionName;
        elem.id = elem.questionId;
        elem.download = prepareURL("marking/bins/" + json.bin.id + "/questions/" + elem.id + "/download");
        elem.sublist = "marking/bins/" + json.bin.id + "/questions/" + elem.id;
        elem.sublistTemplateFunction = "markingQuestionStudents";
        elem.marking= "/marking/bins/" + json.bin.id + "/questions/" + elem.id;
    })

    json.unmarkedElems = _.filter(json.questionList, function(s) { return !s.isMarked; })
    json.markedElems = _.filter(json.questionList, function(s) { return s.isMarked; })

    return 'handins.marking.listElems';
})

function markingQuestionStudents(json) {
    json.subPanel = true;
    json.elems = json.studentList;
    var bin = getRouteParams()[0];

    _.each(json.elems, function(elem){
        elem.name = elem.owner;
        elem.download = prepareURL("marking/bins/" + bin + "/questions/" + json.question + "/students/" + elem.owner + "/download");
        elem.marking = "/marking/bins/" + bin + "/questions/" + json.question + "/students/" + elem.owner;
    })
    return 'shared.handins.generic.listPanel';
}

function marking(students) {
    return function (json) {
        json.students = students;
        return "handins.marking.index";
    }
}


function view(view, template) {
    return function(json) {
        pageView = new view({
            el: $('.main'),
            data: json
        });
        return template;
    }
}
function homepage(json) {
    router.navigate("bins/create", {trigger: true})
    return 'handins.bin.create';
}
SOY_GLOBALS = {
    URLPrefix: CONTEXT_PATH
};
$(document).ready(function() {
    router = Router({
        "": homepage,
        "bins/:id/submissions": combine(submissionList, binInjector("handins.submission.index")),
        "bins": binList,
        "bins/:binId": "handins.bin.edit",
        "bins/create": "handins.bin.create",
        "marking/bins/:binId/students": combine(binInjector(), marking(true)),
        "marking/bins/:binId/questions": combine(binInjector(), marking(false)),
        "bins/marking": markingList,
        "test": "main.index2"

    })
})


