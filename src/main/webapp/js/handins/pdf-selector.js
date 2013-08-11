/**
* For the brave souls who get this far: You are the chosen ones,
* the valiant knights of programming who toil away, without rest,
* fixing our most awful code. To you, true saviors, kings of men,
* I say this: never gonna give you up, never gonna let you down,
* never gonna run around and desert you. Never gonna make you cry,
* never gonna say goodbye. Never gonna tell a lie and hurt you.
*/


function addSelectingModal2(bin, submission) {
    var html = shared.handins.selectingModal({bin: bin, submission: submission});
    var elem = $(html);
    var selecting = false;

    elem.prependTo($('body')).foundation().foundation("reveal","open");
    elem.find('.pdf-viewer').css('height',elem.height() + "px");

    //asyncLoad(elem.find(".async-loader"));
    markers = new MarkerCollection();
    view = new MarkerView({
        el: elem.find('.questions-container'),
        collection: markers
    });
    view.render();

    elem.find('.select-question').click(function() {
        if (selecting)
            return ;

        selecting = true;
        var marker = new MarkerModel({name: "Works"});
        marker.marker.enableMarking();
        markers.add(marker);
    })
    return elem;
}

function showSelectingModal2(bin, submission) {
    $('#selectingModal').remove();
    addSelectingModal2(bin, submission);
    showPdf(submission);
}


function showPdf(submission) {
    // Function located in viewer.js. That file contains magic, pure voodoo magic.
    loadPdf(prepareURL("/submissions/" + submission + "/download"));
}



var MarkerView = Backbone.View.extend({
    initialize: function() {
        this.render  = _.bind(this.render, this);
        this.collection.on('all', this.render);
    },
    render: function() {
        var html = shared.handins.generic.listPanel({
            elems: this.collection.toJSON()
        });
        this.$el.html(html);
        return this;
    }
})


function showSelectingModal3(bin, submission) {
    $('#selectingModal').remove();
    var html = shared.handins.selectingModal({bin: this.bin, submission: this.submission});
    var elem = $(html);

    elem.prependTo($('body')).foundation().foundation("reveal","open");
    elem.find('.pdf-viewer').css('height',elem.height() + "px");

    view = new SelectingView({
        bin: bin,
        submission: submission,
        el: elem
    });
}


var SelectingView = Backbone.View.extend({

    initialize: function(data) {
        this.markers = new MarkerCollection();
        this.questionsView = new MarkerView({
            el: this.$el.find('.questions-container'),
            collection: this.markers
        });
        this.questionsView.render();


        this.questions = new QuestionCollection([], {
            url: prepareURL('bins/'+ this.options.bin + '/questions')
        })
        this.questionsSelectinbView = new QuestionSelectorView({
            collection: this.markers
        })

        this.questions.fetch();

        _.bindAll(this, 'newQuestion', 'deleteQuestion');

        showPdf(this.options.submission)
    },

    events: {
        'click .select-question': 'newQuestion',
        'click .delete-element': 'deleteQuestion',
        'click .show-element': 'showQuestion'
    },

    showQuestion: function(evt) {
        var index = $(evt.target).closest('li').prevAll().size();
        var marker = this.markers.at(index);

        marker.scrollTo();
    },

    deleteQuestion: function(evt) {
        var index = $(evt.target).closest('li').prevAll().size();

        var marker = this.markers.at(index);
        marker.remove();
        this.markers.remove(marker);

        evt.preventDefault();
        evt.stopPropagation();
    },

    mouseEffect: function() {
                var elem=$('<div class="magic-overlay"> </div>').prependTo($('body'));
    			var originalBGplaypen = elem.css("background-color"),
    			    x, y, xy, bgWebKit, bgMoz,
    			    //lightColor = "rgba(255,255,255,0.75)",
    			    lightColor = "rgba(255, 88, 0, 0.75)"
    			    gradientSize = 100;

    				// Basic Demo
    				elem.mousemove(function(e) {

    					x  = e.pageX - this.offsetLeft;
    					y  = e.pageY - this.offsetTop;
    					xy = x + " " + y;

    					bgWebKit = "-webkit-gradient(radial, " + xy + ", 0, " + xy + ", " + gradientSize + ", from(" + lightColor + "), to(rgba(255,255,255,0.0))), " + originalBGplaypen;
    					bgMoz    = "-moz-radial-gradient(" + x + "px " + y + "px 45deg, circle, " + lightColor + " 0%, " + originalBGplaypen + " " + gradientSize + "px)";

    					$(this)
    						.css({ background: bgWebKit })
    						.css({ background: bgMoz });

    				}).mouseleave(function() {
    					$(this).css({ background: originalBGplaypen });
    				});
    },

    mouseEffect2: function() {


    },

    counter: 0,

    newQuestion: function() {
        var marker = new Marker({
            name: "Works " + this.counter,
            delete: "",
            hidden: true,
            show: true
        });
        marker.enableMarking();
        this.markers.add(marker);
        this.counter ++;
        //this.mouseEffect();
    }

})


var QuestionSelectorView = Backbone.View.extend({
    initialize: function() {
        _.bindAll(this, "render");
        this.collection.on('all', this.render);
    },

    show: function() {
        this.$el.appendTo($('body'));
        this.$el.show();
    },

    hide: function() {
        this.$el.remove();
    },


    render: function() {
        this.$el.html(shared.handins.questionSelect(this.collection))
        return this;
    }
});


var Question = Backbone.Model.extend({

})

var QuestionCollection = Backbone.Collection.extend({
     model: Question,

     parse: function(response) {
        return response.questions;
     }
})


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
