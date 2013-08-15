bindThis = function(fn, me) {
    return function() {
        return fn.apply(me, arguments);
    };
};


var Marker = Backbone.Model.extend({
    initialize: function() {
        _.bindAll(this, 'updateName');
        this.on('change:question', this.updateName);
    },

    updateName: function() {
        this.set('name', this.get('question').get('name'));
        this.set('hidden', false);
    }


})

var MarkerCollection = Backbone.Collection.extend({
    model: Marker,

});
