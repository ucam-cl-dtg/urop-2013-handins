function createMarker(markers, begin, end) {
    var marker = new Marker({
        delete: "",
        hidden: true,
        show: true
    });

    marker.set("question", begin.question);

    markers.add(marker);
    var overlay = new MarkerOverlay({
        model: marker
    });

    overlay.createFromPosition(begin, end);

    marker.trigger("selected", marker);

}

function findInPage(index, text) {
    re = /«[a-zA-Z0-9 ]+»/g;
    var matches = [];
    var bidi = PDFView.pages[index].textLayer.textContent.bidiTexts;

    while ((match = re.exec(text))!=null) {
        matches.push({
            index: match.index,
            value: match
        });
    }

//    console.log(matches);
    return _.chain(matches).map(function(matchedElem) {
        var i = 0;
        var match = matchedElem.index;
        while (match > 0) {
            match -= bidi[i].str.length;
            i++;
        }

        return {
            page: index,
            position: i,
            name: matchedElem.value[0].slice(1,matchedElem.value[0].length - 1)
        };
    }).value();
}

function magicFind(markers, questions, pageTexts) {

    var positions = _.range(PDFView.pages.length).map(function(index) {
        try {
            return findInPage(index, pageTexts[index]);
        } catch (err) {
            console.error(err);
            return [];
        }
    });

    console.log(positions);

    positions = _.flatten(positions).map(function(position) {
        try {
            var elem = PDFView.pages[position.page].textLayer.textDivs[position.position];
            var top = $(elem).css('top');
            position.position = parseFloat(top.replace('px', ''));
            position.question = questions.findWhere({name: position.name});
            if (position.question == null || position.question == undefined)
                return null;
            return position;
        } catch(err) {
            console.error(err);
            return null;
        }
    });

    console.log(positions);

    // Filter out the matches that weren't questions;
    positions = _.filter(positions, function (elem) {
        return elem != null;
    });

    // Add an end element;
    var lastPage = PDFView.pages.length;
    positions.push({
        position: $('#pageContainer' + lastPage).height(),
        page: lastPage - 1
    });


    for (var i = 0; i < positions.length - 1; i++) {
        createMarker(markers, positions[i], positions[i + 1]);
    }

    return positions;

}



function generatePageText() {
    var pagePromises = _.range(1, 6).map(function (index) {
        return PDFView.pdfDocument.getPage(index);
    });

    var contextPromises = _.map(pagePromises, function(pagePromise) {
        var contextPromise = new PDFJS.Promise();
        pagePromise.then(function (page) {
            page.getTextContent().then(function(context) {
                contextPromise.resolve(context);
            });
        });
        return contextPromise;
    });

    var result = new PDFJS.Promise();

    PDFJS.Promise.all(contextPromises).then(function(contexts) {
        var pageTexts = _.map(contexts, function (context) {
            return _.reduce(context.bidiTexts, function (value, bidi) {
                return value + bidi.str;
            }, "");
        });

        result.resolve(pageTexts);
    })

    return result;
}
function automark(markers, questions) {
    var promises = []
    promises[0]  = renderAllPages();
    promises[1]  = generatePageText();

    PDFJS.Promise.all(promises).then(function (data) {
        var pageTexts = data[1];
        magicFind(markers, questions, pageTexts)
        console.log("Find finished")
    })

}
function renderAllPages() {
    //Get all the pageViews that need rendering

    var pages = _.chain(_.range(0, 5)).map(function(index) {
        return PDFView.pages[index];
    }).filter(function(page) {
            return page.renderingState == RenderingStates.INITIAL;
        }).value();

    var result = new PDFJS.Promise();

    // Need to draw the pages async
    var drawGenerator = function(i) {
        return function() {
            if (i == pages.length) {
                result.resolve();
                return ;
            }

            PDFView.highestPriorityPage = 'page' + pages[i].id;
            pages[i].draw(drawGenerator(i + 1))
        }
    }

    drawGenerator(0)();

    return result;
}


