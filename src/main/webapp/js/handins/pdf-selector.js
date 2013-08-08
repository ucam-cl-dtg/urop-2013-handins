/**
* For the brave souls who get this far: You are the chosen ones,
* the valiant knights of programming who toil away, without rest,
* fixing our most awful code. To you, true saviors, kings of men,
* I say this: never gonna give you up, never gonna let you down,
* never gonna run around and desert you. Never gonna make you cry,
* never gonna say goodbye. Never gonna tell a lie and hurt you.
*/


function addSelectingModal2(bin, submission) {
    var html = shared.handins.selectingModal({bin: bin, submission: submission});
    var elem = $(html);
    var selecting = false;

    elem.prependTo($('body')).foundation().foundation("reveal","open");
    elem.find('.pdf-viewer').css('height',elem.height() + "px");

    //asyncLoad(elem.find(".async-loader"));
    markers = new MarkerCollection();
    view = new MarkerView({
        el: elem.find('.questions-container'),
        collection: markers
    });
    view.render();

    elem.find('.select-question').click(function() {
        if (selecting)
            return ;

        selecting = true;
        var marker = new MarkerModel({name: "Works"});
        marker.marker.enableMarking();
        markers.add(marker);
    })
    return elem;
}

function showSelectingModal2(bin, submission) {
    $('#selectingModal').remove();
    addSelectingModal2(bin, submission);
    showPdf(submission);
}


function showPdf(submission) {
    // Function located in viewer.js. That file contains magic, pure voodoo magic.
    loadPdf(prepareURL("/submissions/" + submission + "/download"));
}



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


function showSelectingModal3(bin, submission) {
    $('#selectingModal').remove();
    var html = shared.handins.selectingModal({bin: this.bin, submission: this.submission});
    var elem = $(html);

    elem.prependTo($('body')).foundation().foundation("reveal","open");
    elem.find('.pdf-viewer').css('height',elem.height() + "px");

    var view = new SelectingView({
        bin: bin,
        submission: submission,
        el: elem
    });
}


var SelectingView = Backbone.View.extend({

    initialize: function(data) {
        this.markers = new MarkerCollection();
        view = new MarkerView({
            el: this.$el.find('.questions-container'),
            collection: this.markers
        });
        view.render();

        _.bindAll(this, 'newQuestion');

        showPdf(this.options.submission)
    },

    events: {
        'click .select-question': 'newQuestion'
    },

    newQuestion: function() {
        var marker = new MarkerModel({name: "Works"});
        marker.marker.enableMarking();
        this.markers.add(marker);
    }

})
