// make a request in a standard way
function request(baseUrl, index, size, addToCurrent, doAfterSuccessfulResponse) {
  // clear 'load more' link
  $('#loadMoreLinkTarget').empty();
  
  // clear div that shows no data warning
  hideNoData();
  
  // create url
  var url = pagedUrl(baseUrl, index, size);
  
  if(!url) {
    logger("[error] called request with bad url");
    return;
  }
    
  logger(url);

  $.ajax({
    "url": url,
    "cache": false
  })
  .done(
    function(context) {
      // we no we got *something* (in the 200 range) so clear (all) errors
      clearErrors();
      
      // then render response
      var response = renderResponse(context, addToCurrent);
      logger("[info] response got content: " + response);
      
      // no further action if minimum page size not reached
      if(context.length < DEFAULT_PAGE_SIZE) {
    	  return;
      }
      
      // if some content was rendered
      if(response && doAfterSuccessfulResponse) {
        doAfterSuccessfulResponse(Number(index) + Number(size), size);
      } else if(doAfterSuccessfulResponse) { // when no after-action instruction is given
                                             // it implies that no action was expected and
                                             // that this message is not needed
        showNoData();
      }
    }
  )
  .fail(
    function(jqXHR, textStatus, errorThrown) {
    	if(textStatus == "503") {
    		showErrorInContainer('Uh oh!', 'There was an error accessing Janus.  It is likely, at this time, that the Janus server is reindexing content.  Please try again later.');
    	} else {
    		showErrorInContainer('Oh man!', 'An unspecified error has occurred, it would be best to contact someone who knows what is going on.');
    	}
    }
  )
  ;          
}

// request a list of elements of the given type sorted as
// specified
function requestTypeList(type, sort, index, size, addToCurrent, doAfterSuccessfulResponse) {
  if(!sort) {
    sort = "default";
  }
  
  if(!type) {
    type = "book";            
  }
  
  var url = "s/" + type + "/list?sort=" + sort;
  request(url, index, size, addToCurrent, doAfterSuccessfulResponse);  
}

// request elements of a given base type
function requestBaseType(type, id, addToCurrent, doAfterSuccessfulResponse) {
  var url = "s/" + type + "/" + id;
  request(url, 0, 0, addToCurrent, doAfterSuccessfulResponse);
}

// request child elements for given type
function requestChildForType(type, id, child, index, size, addToCurrent, doAfterSuccessfulResponse) {
  var url = "s/" + type + "/" + id + "/" + child;
  request(url, index, size, addToCurrent, doAfterSuccessfulResponse);
}

// search a term against all categories
function searchAll(searchString, index, size, addToCurrent, doAfterSuccessfulResponse) {
  var url = "s/search/all/" + searchString;
  request(url, index, size, addToCurrent, doAfterSuccessfulResponse);        
}

// modify a base url for paging 
function pagedUrl(baseUrl, index, size) {
  // normalize paging values
  if(!size || size < 1) {
    size = DEFAULT_PAGE_SIZE;
  }
  if(!index || index < 0) {
    index = 0;
  }  
  
  // decide on query string size
  var start = "?";
  if(baseUrl.indexOf('?') >= 0) {
    start = "&";
  }
  
  // modify base url
  baseUrl = baseUrl + start + "index=" + index + "&size=" + size;
  
  return baseUrl;
}