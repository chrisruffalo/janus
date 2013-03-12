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
            
            // clear 'load more' link
            $('#loadMoreLinkTarget').empty();
            
            // clear div that shows no data warning
            hideNoData();
            
            // clear search term
            $('#term').val('');
          });

          // get individual template item of core type
          this.get('#/:type/get/:id', function(context) {
            var id = this.params['id'];
            var type = this.params['type'];
            var add = makeBoolean(context.params['add']);

            // clear search term
            $('#term').val('');

            requestBaseType(type, id);
          });

          // get books for core item
          this.get('#/:type/get/:id/:child', function(context) {
            var id = context.params['id'];
            var type = context.params['type'];
            var child = context.params['child'];
            var index = normalizeIndex(context.params['index']);
            var size = normalizeSize(context.params['size']);
            var add = makeBoolean(context.params['add']);
            
            // clear search term
            $('#term').val('');
            
            requestChildForType(type, id, child, index, size, add, function(newIndex, newSize){
                renderNextPageLink("#/" + type + "/get/" + id + "/" + child + "?index=" + newIndex + "&size=" + newSize + "&add=true");
            });
          });                                 
          
          // list items
          this.get('#/:type/list', function(context) {
            var type = context.params['type'];
            var index = normalizeIndex(context.params['index']);
            var size = normalizeSize(context.params['size']);
            var sort = context.params['sort'];
            var add = makeBoolean(context.params['add']);
            
            // clear search term
            $('#term').val('');
            
            if(!sort) {
                sort = "default";
            }
            
            // request list of given type
            requestTypeList(type, sort, index, size, add, function(newIndex, newSize){
                renderNextPageLink("#/" + type + "/list" + "?sort=" + sort + "&index=" + newIndex + "&size=" + newSize + "&add=true");
            });
          });
          
          // searching
          this.get('#/search/all', function(context) {
            var term = context.params['term'];
            var index = normalizeIndex(context.params['index']);
            var size = normalizeSize(context.params['size']);
            var add = makeBoolean(context.params['add']);

            // set ui component
            $('#term').val(term)
            
            // request list of given type
            searchAll(term, index, size, add, function(newIndex, newSize){
                renderNextPageLink("#/search/all" + "?term=" + term + "&index=" + newIndex + "&size=" + newSize + "&add=true");
            });
          });

        }
      );
      
      $(function() {
        app.run('#main');
      });
    
    })(jQuery);
}

function normalizeIndex(index) {
    if(!index || index < 0) {
        return 0;
    }
    return index;
}

function normalizeSize(size) {
    if(!size || size < 0) {
        return defaultPageSize;
    }
    return size;
}