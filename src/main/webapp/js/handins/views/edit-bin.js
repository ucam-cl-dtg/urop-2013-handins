var EditGeneralBinView = Backbone.View.extend({
    initialize: function(options) {
        _.bindAll(this, "render", "saveBin", "deleteBin");
        this.render();

        this.model.on('change', this.render);
    },

    render: function() {
        var html = handins.bin.editGeneral({
            bin: this.model.toJSON()
        });
        this.$el.html(html);
    },

    events: {
        'click .save-bin': 'saveBin',
        'click .delete-bin': 'deleteBin'
    },

    saveBin: function() {
        var name = this.$('.bin-name').val();
        var archived = this.$('.archived input:checked').attr('id') == 'archived';
        var peerMarking = this.$('.peer-marking input:checked').attr('id') == 'peer-marked';
        this.model.set("questionSetName", name, {silent: true}) ;
        this.model.set("name", name, {silent: true});
        this.model.set("archived", archived, {silent: true});
        this.model.set("peerMarking", peerMarking, {silent: true})

        this.model.save();
    },

    deleteBin: function() {
        if (confirm("Are you sure you want to delete this Bin?"))
            this.model.destroy();
    }

})



var BinEditView = Backbone.View.extend({
    initialize: function() {
        this.bin = new Bin({
            id: getRouteParams()
        });
        this.bin.fetch();

        this.editQuestionsView = new EditQuestionsView({
            el: this.$('section.questions .content'),
            bin: this.bin
        });

        this.editAccessPermissionsView = new EditAccessPermissionsView({
            el: this.$('section.access-permissions .content'),
            bin: this.bin
        });

        this.editGeneralBinView = new EditGeneralBinView({
            el: this.$('section.general-bin .content'),
            model: this.bin
        })

    },

    remove: function() {
        Backbone.View.prototype.remove.apply(this);
    },

});
