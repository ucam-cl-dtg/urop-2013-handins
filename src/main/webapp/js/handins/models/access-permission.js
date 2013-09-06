var AccessPermission = Backbone.CustomModel.extend({

    destroy: function() {
        if (this.collection && this.collection.bin) {
            if (this.collection.bin.get("owner") == this.get("id")) {
                errorNotification("The owner of the bin must have access.");
                return false;
            }
        }
        return Backbone.CustomModel.prototype.destroy.apply(this);
    }
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

