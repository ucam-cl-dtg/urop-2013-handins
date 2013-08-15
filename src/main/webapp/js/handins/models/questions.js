var Question = Backbone.Model.extend({

})

var QuestionCollection = Backbone.Collection.extend({
     model: Question,

     parse: function(response) {
        return response.questions;
     }
})
