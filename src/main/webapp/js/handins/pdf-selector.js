/**
* For the brave souls who get this far: You are the chosen ones,
* the valiant knights of programming who toil away, without rest,
* fixing our most awful code. To you, true saviors, kings of men,
* I say this: never gonna give you up, never gonna let you down,
* never gonna run around and desert you. Never gonna make you cry,
* never gonna say goodbye. Never gonna tell a lie and hurt you.
*/


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

    elem.prependTo($('body')).foundation().foundation("reveal","open", {closeOnBackgroundClick: false, closeOnEsc: false});
    elem.find('.pdf-viewer').css('height',elem.height() + "px");

    view = new SelectingView({
        bin: bin,
        submission: submission,
        el: elem
    });
}


var SelectingView = Backbone.View.extend({

    initialize: function(data) {
        this.selecting = false;
        this.markers = new MarkerCollection();
        this.questionsView = new MarkerView({
            el: this.$el.find('.questions-container'),
            collection: this.markers
        });
        this.questionsView.render();


        this.questions = new QuestionCollection([], {
            url: prepareURL('bins/'+ this.options.bin + '/questions')
        })

        this.questions.fetch();

        _.bindAll(this, 'newQuestion', 'deleteQuestion', 'selectQuestion', 'saveSelection', 'closeSelection');

        showPdf(this.options.submission)
    },

    events: {
        'click .select-question': 'newQuestion',
        'click .delete-element': 'deleteQuestion',
        'click .show-element': 'showQuestion',
        'click .save-selection': 'saveSelection',
        'click .close-selection': 'closeSelection'
    },

    selectQuestion: function(evt) {
        this.questionsSelectingView = new QuestionSelectorView({
            collection: this.questions
        })

        this.selecting = false;
        this.questionsSelectingView.marker = evt;
        this.questionsSelectingView.show();

    },


    // For acr31: Tick 8 * // Voodoo magic
    extractPositions: function(callback) {
        this.markers.foldl(function(stack, val) {
           return function (objs) {
                val.getPosition(function(obj) {
                    obj.id = val.get("question").get("id");
                    objs.push(obj);
                    stack(objs);
                })
           };
        }, callback) ( [] );

    },

    closeSelection: function() {
        this.$el.foundation("reveal", "close");
    },

    saveSelection: function(evt) {
        var _this = this;
        this.extractPositions(function(positions) {
            var id = [],
                startPage = [],
                endPage = [],
                startLoc = [],
                endLoc = [];
            positions = _.each(positions, function (elem) {
                id.push(elem.id);
                startPage.push(elem.start.page);
                endPage.push(elem.end.page);
                startLoc.push(elem.start.absolutePosition);
                endLoc.push(elem.end.absolutePosition);

            })

            var data = {
                id: id,
                startPage: startPage,
                endPage: endPage,
                startLoc: startLoc,
                endLoc: endLoc,
            }
            console.log(data);
            $.post(prepareURL("bins/" + _this.options.bin + "/submissions/" + _this.options.submission), data, function() {
            });

        })
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


    newQuestion: function() {
        if (this.selecting)
            return false;

        var marker = new Marker({
            delete: "",
            hidden: true,
            show: true
        });
        marker.on("stopSelecting", this.selectQuestion)
        marker.enableMarking();
        this.markers.add(marker);
        this.selecting = true;
        //this.mouseEffect();
    }

})



var QuestionSelectorView = Backbone.View.extend({
    initialize: function() {
        _.bindAll(this, "render", "selectQuestion", "close");
        this.collection.on('all', this.render);
    },

    show: function() {
        this.render();
        this.$el.appendTo($('body'));

        this.$el.dialog({
            modal: true,
            title: 'Please select the question',
            minWidth: 400,
            resizable: false,
            draggable: false
        })
    },

    close: function() {
        this.remove();
        if (this.selectedQuestionCID)  {
            this.marker.set("question", this.collection.get(this.selectedQuestionCID));
        }
    },

    events: {
        'click .save-question-selection': 'close'
    },

    selectQuestion: function(evt, ui) {
        this.selectedQuestionCID = ui.item.cid;
        $(evt.target).parent().next('input').focus();
    },

    render: function() {
        this.$el.html(shared.handins.questionSelect({
            questions: this.collection.toJSON()
        }))

        var autocompleteSrc = this.collection.map(function(elem) {
            return {
                label: elem.get('name'),
                value: elem.get('name'),
                cid: elem.cid
            }
        })

        this.$el.find('.question-input').autocomplete({
            source: autocompleteSrc,
            minLength: 0,
            delay: 0,
            select: this.selectQuestion,
        })

        $('.ui-autocomplete').zIndex(200);
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
