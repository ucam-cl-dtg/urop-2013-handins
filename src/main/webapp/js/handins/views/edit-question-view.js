var EditQuestionsView = Backbone.View.extend({
    initialize: function(options) {
        this.bin = options.bin;
        _.bindAll(this, 'addQuestion');
        this.render();

        this.questionsView = new GeneralListView({
            collection: this.bin.questions,
            el: this.$('.question-container'),
            options: {
                'delete': ''
            }
        })
        this.addQuestionView = new GeneralAddView({
            el: this.$('.add-question-container'),
            text: "Add Question"
        }).render();

        this.addQuestionView.on('add', this.addQuestion);
    },

    addQuestion: function(name) {
        this.bin.questions.create({
            name: name
        }, {
            error: function (model, resp) {
                model.destroy();
                var msg = JSON.parse(resp.responseText).message;
                errorNotification(msg);
            }
        })
    },

    render: function() {
        this.$el.html('<div class="question-container"></div><div class="add-question-container"></div>')
        return this;
    }
});
