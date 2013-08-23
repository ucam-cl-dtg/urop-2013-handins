Backbone.CustomModel = Backbone.Model.extend({
    sync: function(method, model, options) {
        if (method == 'create' || method == 'update')
            options.data = model.attributes;
        return Backbone.Model.prototype.sync.call(this, method, model, options);
    }

})
var AccessPermission = Backbone.CustomModel.extend({

})

Backbone.emulateJSON = true;

var  AccessPermissionCollection = Backbone.Collection.extend({
    model: AccessPermission,

    initialize: function (options) {
        if (!options.bin && !this.url)
            throw new Error("A questions Collection must be bound to a bin");

        this.bin = options.bin;
        if (!this.url)
            this.url = prepareURL("bins/" + this.bin.get("id") + "/permissions");
    },

    parse: function(response) {
        return _.map(response.users, function(user){
            return {
                user: user,
                name: user
            }
        });
    }
})

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