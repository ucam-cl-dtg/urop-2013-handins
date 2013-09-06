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
    var height = $(document).height();

    elem.prependTo($('.main'))
    elem.foundation('reveal',{
        opened: function() {
            elem.css('top', height * 0.05);
            var questionsContainer = elem.find('.questions-container');

            var availableHeight = height * 0.9 - elem.find('.save-container').height() - elem.find('.select-container').height() - 10;

            questionsContainer.css("max-height", availableHeight + "px");
        }
    });

    $('.main').foundation();

    elem.foundation("reveal","open", {
            closeOnBackgroundClick: false,
            closeOnEsc: false,
    });

    elem.height(height * 0.9);
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

