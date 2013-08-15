var Bin = Backbone.Model.extend({

});


var BinCollection = Backbone.Collection.extend({
    model: Bin,
    url: CONTEXT_PATH + '/api/bins',
    parse: function(response) {
        return response.bins;
    }
});

var PermissionView = Backbone.View.extend({
    render: function() {

    }
})
var GeneralListView = Backbone.View.extend({

    initialize: function() {
        _.bindAll(this, "add", "remove");
        this._views = {};

        this.collection.each(this.add)
        this.collection.on("add", this.add);
        this.collection.on("remove", this.remove);
    }

    add: function() {
        this.collect
    }

})
