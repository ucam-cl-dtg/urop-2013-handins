Backbone.CustomModel = Backbone.Model.extend({
    sync: function(method, model, options) {
        if (method == 'create' || method == 'update')
            options.data = model.attributes;
        return Backbone.Model.prototype.sync.call(this, method, model, options);
    }

})
Backbone.emulateJSON = true;

var Bin = Backbone.CustomModel.extend({
    initialize: function() {
        this.questions = new QuestionCollection({
            bin: this,
        });
        this.accessPermissions = new AccessPermissionCollection({
            bin: this,
        });

        this.accessPermissions.fetch();
        this.questions.fetch();
    },
    url: function() {
        return prepareURL("bins/" + this.get('id'));
    },
    parse: function(response) {
        if (response.bin)
            return response.bin;
        return response;
    }
});


var BinCollection = Backbone.Collection.extend({
    model: Bin,
    url: CONTEXT_PATH + '/api/bins',
    parse: function(response) {
        return response.bins;
    }
});