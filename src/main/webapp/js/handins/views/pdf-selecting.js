function showPdf(submission) {
    // Function located in viewer.js. That file contains magic, pure voodoo magic.
    loadPdf(prepareURL("/submissions/" + submission + "/download"));
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

    remove: function() {
        this.questionView.remove();

        Backbone.Model.prototype.remove.apply(this);
    },

    events: {
        'click .select-question': 'newQuestion',
        'click .delete-element': 'deleteQuestion',
        'click .show-element': 'showQuestion',
        'click .save-selection': 'saveSelection',
        'click .close-selection': 'closeSelection'
    },

    selectQuestion: function(evt) {
        if (this.questionsSelectingView)
            this.questionsSelectingView.remove();

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

        marker.trigger('scrollTo');
    },

    deleteQuestion: function(evt) {
        var index = $(evt.target).closest('li').prevAll().size();

        var marker = this.markers.at(index);

        marker.destroy();

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

        this.markers.add(marker);
        var overlay = new MarkerOverlay({
            model: marker,
        })

        marker.on("selected", this.selectQuestion)

        marker.set('selecting', true);
        this.selecting = true;
        //this.mouseEffect();
    }

})

