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
