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
      var response = renderResponse(context, addToCurrent);
      logger("[info] response got content: " + response);
      
      // no further action if minimum page size not reached
      if(context.length < defaultPageSize) {
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
  );          
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
    size = defaultPageSize;
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