var AccessPermission = Backbone.CustomModel.extend({


})


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
                name: user,
                id: user
            }
        });
    }
})

