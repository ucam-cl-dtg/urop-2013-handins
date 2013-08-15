var MarkerView = Backbone.View.extend({
    initialize: function() {
        this.render  = _.bind(this.render, this);
        this.collection.on('all', this.render);
    },
    render: function() {
        var html = shared.handins.generic.listPanel({
            elems: this.collection.toJSON()
        });
        this.$el.html(html);
        return this;
    }
})

