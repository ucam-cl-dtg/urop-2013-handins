var Question = Backbone.CustomModel.extend({

})

var QuestionCollection = Backbone.Collection.extend({
    model: Question,

    initialize: function (options) {
        if (!options.bin && !this.url)
            throw new Error("A questions Collection must be bound to a bin");

        this.bin = options.bin;
        if (!this.url)
            this.url = prepareURL("bins/" + this.bin.get("id") + "/questions");
    },

    parse: function(response) {
        return response.questions;
    },

})
