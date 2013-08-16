var Bin = Backbone.Model.extend({
    initialize: function() {
        this.set("questions", new QuestionCollection({
            bin: this,
        }));
        this.get('questions').fetch();
    },
    url: function() {
        return prepareURL("bins/" + this.get('id'));
    }
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

var EditQuestionsView = Backbone.View.extend({
    initialize: function(options) {
        this.bin = options.bin;
        this.render();

        this.questionsView = new GeneralListView({
            collection: this.bin.get("questions"),
            el: this.$('.question-container'),
            options: {
                'delete': ''
            }
        })
    },

    render: function() {
        this.$el.html('<div class="question-container"></div>')
        return this;
    }
});

var GeneralListView = Backbone.View.extend({

    initialize: function(options) {
        this.options = options.options;

        _.bindAll(this, "add", "remove");
        this._views = {};

        this.render();
        this.collection.on("add", this.add);
        this.collection.on("remove", this.remove);
    },


    add: function(model) {
        var elemView = new GeneralListElemView({
            model: model,
            options: this.options
        }).render();

        this._views[model.cid] = elemView;

        elemView.$el.appendTo(this.$('.panels'))
    },

    remove: function(model) {
        this._views[model.cid].remove();
        delete this._views[model.cid];
    },

    render: function() {
        var html = shared.handins.generic.bbListPanel();

        this.$el.html(html);
        this.collection.each(this.add);

        return this;
    }

})


var GeneralListElemView = Backbone.View.extend({
    initialize: function(options) {
        this.options = options.options;
        _.bindAll(this, 'render', 'deleteElem');
        this.model.on("change", this.render);
    },

    addOptions: function(data) {
        for (var opt in this.options) {
            data[opt] = this.options[opt];
        }
    },

    events: {
        'click .delete-me': 'deleteElem'
    },

    deleteElem: function(evt) {
        evt.stopPropagation();
        evt.preventDefault();
        this.model.destroy();
    },

    render: function() {
        var data = this.model.toJSON();
        this.addOptions(data);

        var html = shared.handins.generic.listElement({elem: data});

        this.$el.html(html);

        return this;
    }

})


var BinEditView = Backbone.View.extend({
    initialize: function() {
        this.bin = new Bin({
            id: getRouteParams()
        });

        this.editQuestionsView = new EditQuestionsView({
            el: this.$('section.questions .content'),
            bin: this.bin
        });

    },

    remove: function() {
        Backbone.View.prototype.remove.apply(this);
    },

});
