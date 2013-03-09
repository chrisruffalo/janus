// make a request in a standard way
function request(url, addToCurrent) {
  if(!url) {
    logger("[error] called request with bad url");
    return;
  }
    
  logger(url);

  $.ajax(url)
  .success(
    function(context) {
      var response = renderResponse(context, addToCurrent);
      logger("[info] response got content: " + response);
    }
  );          
}

function requestTypeList(type, sort, index, size, addToCurrent) {
  if(!sort) {
    sort = "default";
  }
  
  if(!type) {
    type = "book";            
  }
  
  // normalize paging values
  if(!size || size < 1) {
    size = defaultPageSize;
  }
  if(!index || index < 0) {
    index = 0;
  }
  
  var url = "s/" + type + "/list?sort=" + sort + "&index=" + index + "&size=" + size;
  request(url, addToCurrent);  
}

function requestBaseType(type, id, animate, addToCurrent) {
  var url = "s/" + type + "/" + id;
  request(url, addToCurrent);
}

function requestChildForType(type, id, child, animate, addToCurrent) {
  var url = "s/" + type + "/" + id + "/" + child;
  request(url, addToCurrent);
}

function searchAll(searchString, addToCurrent) {
  var url = "s/search/all/" + searchString;
  request(url, addToCurrent);        
}