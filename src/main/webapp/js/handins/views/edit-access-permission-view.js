var EditAccessPermissionsView = Backbone.View.extend({
    initialize: function(options) {
        this.bin = options.bin;
        _.bindAll(this, 'addPermission', 'deletePermission');
        this.render();

        this.questionsView = new GeneralListView({
            collection: this.bin.accessPermissions,
            el: this.$('.access-permissions-container'),
            options: {
                'delete': ''
            }
        });
        this.addPermissionView = new GeneralAddView({
            el: this.$('.add-permission-container'),
            text: "Add User",
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

    events: {
        'click .delete-elem': 'deletePermission'
    },

    deletePermission: function(evt) {
        console.log("debug");
    },
    addPermission: function(user) {
        var addedUser = this.bin.accessPermissions.findWhere({user: user.crsid});
        if (addedUser)
            return ;
        this.bin.accessPermissions.create({
            user: user.crsid,
            name: user.crsid,
            id: user.crsid
        });
    },

    render: function() {
        this.$el.html('<div class="access-permissions-container"></div><div class="add-permission-container"></div>');
        return this;
    }
});

