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

function markingStudents (json) {

    _.each(json.students,  function(elem) {
        elem.name = elem.student;
        elem.download = "/marking/bin/" + json.bin.id + "/student/" + elem.student + "/download";
        elem.sublist = "marking/bin/" + json.bin.id + "/student/" + elem.student;
        elem.sublistTemplateFunction = "markingStudentsQuestion"
        elem.marking = "bla";
    })

    json.unmarkedElems = _.filter(json.students, function(s) { return !s.isMarked; })
    json.markedElems = _.filter(json.students, function(s) { return s.isMarked; })

    return 'handins.marking.index';
}

function markingStudentsQuestion(json) {
    json.subPanel = true;

    var bin = getRouteParams()[0];

    json.elems = json.studentQuestions;
    _.each(json.elems, function(elem) {
        elem.id = elem.questionId;
        elem.name = elem.questionName;

        elem.download = "/marking/bin/" + bin + "/question/" + elem.id + "/student/" + json.student;
        elem.marking = "bla";
    })

    return 'shared.handins.generic.listPanel';
}

function markingQuestion(json) {

    _.each(json.questionList,  function(elem) {
        elem.name = elem.questionName;
        elem.id = elem.questionId;
        elem.download = "/marking/bin/" + json.bin.id + "/question/" + elem.id + "/download";
        elem.sublist = "marking/bin/" + json.bin.id + "/question/" + elem.id;
        elem.sublistTemplateFunction = "markingQuestionStudents";
        elem.marking= "bla";
    })

    json.unmarkedElems = _.filter(json.questionList, function(s) { return !s.isMarked; })
    json.markedElems = _.filter(json.questionList, function(s) { return s.isMarked; })

    return 'handins.marking.index';
}

function markingQuestionStudents(json) {
    json.subPanel = true;
    json.elems = json.studentList;
    var bin = getRouteParams()[0];

    _.each(json.elems, function(elem){
        elem.name = elem.owner;
        elem.download = "/marking/bin/" + bin + "/question/" + json.question + "/student/" + elem.owner;
        elem.marking = "bla";
    })
    return 'shared.handins.generic.listPanel';
}

$(document).ready(function() {
    router = Router({
        "submission/bin/:id": combine(submissionList, binInjector("handins.submission.index")),
        "bin": binList,
        "marking/bin/:binId/student": combine(binInjector(), markingStudents),
        "marking/bin/:binId/question": combine(binInjector(), markingQuestion)

    })
})


