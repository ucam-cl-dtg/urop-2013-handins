var GeneralAddView = Backbone.View.extend({
    initialize: function(options) {
        _.bindAll(this, 'selectElement', 'addElement', 'handleFormSubmit');
        if (options.text)
            this.text = options.text;
        else
            this.text = "Add"

        if (options.autocomplete) {
            this.autocomplete = _.defaults(options.autocomplete, {
                source: ['ana', 'are' ,'mere'],
                delay: 0,
                minLenght: 0,
                select: this.selectElement,
            })
        }

    },

    selectElement: function(event, ui) {
        this.selected = ui.item;
        this.addElement();
    },

    /*events: {
        'click .add-element': 'addElement'
    },*/

    addElement: function() {
        if (this.selected) {
            this.trigger('add', this.selected);
            this.$('.element-input').val("");
        }
    },

    setupAutocomplete: function(){
        if (this.autocomplete == undefined)
            return ;
        this.auto = this.$('.element-input').autocomplete(this.autocomplete);

        if (this.autocomplete.render)
            this.auto.data( "ui-autocomplete" )._renderItem = this.autocomplete.render ;
    },

    setupNormalInput: function() {
        if (this.autocomplete != undefined)
            return ;
        var _this = this;
        this.$('.element-input').change(function(){
            _this.selected = $(this).val();
        })
    },

    handleFormSubmit: function(evt) {
        evt.stopPropagation();
        evt.preventDefault();

        this.addElement();
        return false;
    },


    render: function() {
        this.$el.html(handins.bin.addElement({
            text: this.text
        }));
        this.$('form.add-element').submit(this.handleFormSubmit)
        this.setupAutocomplete();
        this.setupNormalInput();
        return this;
    }
})
