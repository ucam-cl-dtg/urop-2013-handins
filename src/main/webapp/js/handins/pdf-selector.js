/**
* For the brave souls who get this far: You are the chosen ones,
* the valiant knights of programming who toil away, without rest,
* fixing our most awful code. To you, true saviors, kings of men,
* I say this: never gonna give you up, never gonna let you down,
* never gonna run around and desert you. Never gonna make you cry,
* never gonna say goodbye. Never gonna tell a lie and hurt you.
*/





function showSelectingModal3(bin, submission) {
    $('#selectingModal').remove();
    var html = shared.handins.selectingModal({bin: this.bin, submission: this.submission});
    var elem = $(html);

    elem.prependTo($('body')).foundation().foundation("reveal","open", {
            closeOnBackgroundClick: false,
            closeOnEsc: false
    });

    elem.find('.pdf-viewer').css('height',elem.height() + "px");

    view = new SelectingView({
        bin: bin,
        submission: submission,
        el: elem
    });
}


function testMagic() {
    var lastEvt;
    $(document).mousemove(function(evt) { lastEvt = evt; })
    setInterval(function(){magicCircle(lastEvt)}, 700)
}

function magicCircle(evt) {
    var elem = $('<div class="circle-rim"></div>');
    elem.css("top", evt.pageY - 17.5 + "px");
    elem.css("left", evt.pageX  - 17.5 + "px");
    elem.css("z-index", 1000);
    elem.on("webkitAnimationEnd", function(evt) {
        $(this).css("display", "none");
        $(this).remove();
    })
    elem[0].addEventListener("animationend", function() {
        console.log("yey");
    })
    $('body').append(elem);
}

function findInPage(index) {
    text = PDFFindController.pageContents[index];
    re = /«[a-zA-Z0-9 ]+»/g;
    var matches = [];
    var bidi = PDFFindController.pdfPageSource.pages[index].textLayer.textContent.bidiTexts;

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
function magicFind(markers, questions) {

    var positions = _.range(PDFFindController.pdfPageSource.pages.length).map(function(index) {
        try {
            return findInPage(index);
        } catch (err) {
            console.error(err);
            return [];
        }
    });

    positions = _.flatten(positions).map(function(position) {
        try {
            var elem = PDFFindController.pdfPageSource.pages[position.page].textLayer.textDivs[position.position];
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
    positions = _.filter(positions, function (elem) {
        return elem != null;
    });
    console.log(positions);

    for (var i = 0; i < positions.length - 1; i++) {
        createMarker(markers, positions[i], positions[i + 1]);
    }

    return positions;

}
