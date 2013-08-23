var GeneralAddView = Backbone.View.extend({
    initialize: function(options) {
        _.bindAll(this, 'selectElement', 'addElement');
        if (options.text)
            this.text = options.text;
        else
            this.text = "Add"

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
        this.$el.html(handins.bin.addElement({
            text: this.text
        }));
        this.setupAutocomplete();
        this.setupNormalInput();
        return this;
    }
})
