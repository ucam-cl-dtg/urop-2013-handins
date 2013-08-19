Backbone.CustomModel = Backbone.Model.extend({
    sync: function(method, model, options) {
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

var Bin = Backbone.Model.extend({
    initialize: function() {
        this.set("questions", new QuestionCollection({
            bin: this,
        }));
        this.set("accessPermissions", new AccessPermissionCollection({
            bin: this,
        }));

        this.get('accessPermissions').fetch();
        this.get('questions').fetch();
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

var PermissionView = Backbone.View.extend({
    render: function() {
    }
})

function collectionToAutocomplete(collection) {
    return collection.map(function(elem) {
        return {
            label: elem.get('name'),
            value: elem.get('name'),
            cid: elem.cid,
        }
    })
}

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
        'click .delete-element': 'deleteElem'
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

var EditQuestionsView = Backbone.View.extend({
    initialize: function(options) {
        this.bin = options.bin;
        _.bindAll(this, 'addQuestion');
        this.render();

        this.questionsView = new GeneralListView({
            collection: this.bin.get("questions"),
            el: this.$('.question-container'),
            options: {
                'delete': ''
            }
        })
        this.addQuestionView = new GeneralAddView({
            el: this.$('.add-question-container')
        }).render();

        this.addQuestionView.on('add', this.addQuestion);
    },

    addQuestion: function(name) {
        this.bin.get('questions').create({
            name: name
        })
    },

    render: function() {
        this.$el.html('<div class="question-container"></div><div class="add-question-container"></div>')
        return this;
    }
});

var EditAccessPermissionsView = Backbone.View.extend({
    initialize: function(options) {
        this.bin = options.bin;
        _.bindAll(this, 'addPermission');
        this.render();

        this.questionsView = new GeneralListView({
            collection: this.bin.get("accessPermissions"),
            el: this.$('.access-permissions-container'),
            options: {
                'delete': ''
            }
        })
        this.addPermissionView = new GeneralAddView({
            el: this.$('.add-permission-container'),
            autocomplete: {
                source: function(request, response) {
                    $.get(prepareURL("hack/users"),request, function(data) {
                        _.each(data, function(data) {
                            data.label = data.value = data.crsid;
                        });
                        response(data);
                    });
                },
                minLength: 3,
                render: function( ul, item ) {
                    return $( "<li>" + "<a><div style='display: inline-block; padding-left: 10px;'><div class='full_name'>" + item.name + " (" + item.crsid + ")</div><div class='email'>" + item.crsid + "@cam.ac.uk</div></div></a></li>" ).appendTo(ul);
                }
            }
        }).render();

        this.addPermissionView.on('add', this.addPermission);
    },

    addPermission: function(user) {
        this.bin.get('accessPermissions').create({
            user: user.crsid,
            name: user.name
        })
    },

    render: function() {
        this.$el.html('<div class="access-permissions-container"></div><div class="add-permission-container"></div>')
        return this;
    }
})

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
        this.model.set("name", this.$('.bin-name').val());
        //this.bin.set("archived", this.$('.bin-name').val())

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

var GeneralAddView = Backbone.View.extend({
    initialize: function(options) {
        _.bindAll(this, 'selectElement', 'addElement');

        if (options.autocomplete) {
            this.autocomplete = _.defaults(options.autocomplete, {
                source: ['ana', 'are' ,'mere'],
                delay: 0,
                minLenght: 0,
                select: this.selectElement
            })
        }

    },

    selectElement: function(event, ui) {
        this.selected = ui.item;
    },

    events: {
        'click .add-element': 'addElement'
    },

    addElement: function() {
        if (this.selected)
            this.trigger('add', this.selected);
    },

    setupAutocomplete: function(){
        if (this.autocomplete == undefined)
            return ;
        var auto = this.$('.element-input').autocomplete(this.autocomplete);

        if (this.autocomplete.render)
            auto.data( "ui-autocomplete" )._renderItem = this.autocomplete.render ;
    },

    setupNormalInput: function() {
        if (this.autocomplete != undefined)
            return ;
        var _this = this;
        this.$('.element-input').change(function(){
            _this.selected = $(this).val();
        })
    },

    render: function() {
        this.$el.html(handins.bin.addElement());
        this.setupAutocomplete();
        this.setupNormalInput();
        return this;
    }
})
