function initSammy() {
    // create 'rest-like' routes for application
    // and kick off #main route when complete
    (function($) {              
      var app = $.sammy( 
        function() {
          
          // no-op
          this.get("#", function(context) {
            // no-op
            logger("[warn] reached '#' which is an empty context.");
          });
          
          // main
          this.get('#main', function(context) {
            $('#target').empty(); // main screen doesn't need this
            $('#multi-header-target').empty(); // main screen doesn't need this
            $('#mainOptions').show(); // ensure main navigation options are visible
            
            // clear search term
            $('#term').val('');
          });

          // get individual template item of core type
          this.get('#/:type/get/:id', function(context) {
            var id = this.params['id'];
            var type = this.params['type'];

            // clear search term
            $('#term').val('');

            requestBaseType(type, id);
          });

          // get books for core item
          this.get('#/:type/get/:id/:child', function(context) {
            var id = context.params['id'];
            var type = context.params['type'];
            var child = context.params['child'];
            
            // clear search term
            $('#term').val('');
            
            requestChildForType(type, id, child);
          });                                 
          
          // list items
          this.get('#/:type/list', function(context) {
            var type = context.params['type'];
            var index = context.params['index'];
            var size = context.params['size'];
            var sort = context.params['sort'];
            
            // clear search term
            $('#term').val('');
            
            // request list of given type
            requestTypeList(type, sort, index, size);
          });
          
          // searching
          this.get('#/search/all', function(context) {
            var term = context.params['term'];

            // set ui component
            $('#term').val(term)
            
            // request list of given type
            searchAll(term);
          });

        }
      );
      
      $(function() {
        app.run('#main');
      });
    
    })(jQuery);
}