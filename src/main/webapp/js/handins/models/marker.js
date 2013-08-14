bindThis = function(fn, me) {
    return function() {
        return fn.apply(me, arguments);
    };
};


var Marker = Backbone.Model.extend({

})

var MarkerCollection = Backbone.Collection.extend({
    model: Marker,

});
