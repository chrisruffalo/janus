
function renderResponse(itemList, addToCurrent) {
  if(!itemList) {
    return false;
  }

  $('#mainOptions').hide(); // ensure main navigation is hidden
  
  if(!addToCurrent) {
    $('.renderTarget').remove();
  }        
  
  var response = false;
  if(itemList.type && "multientityresponse" == itemList.type) {
    response = renderMultiResponse(itemList, $('#target'));
  } else {
    // create new target for data
    var newRenderTarget = $("<div class='container renderTarget'></div>")
    newRenderTarget.hide();

    // clear multi-header when any response comes in
    $('#multi-header-target').empty();

    if(itemList.type && !itemList.length) {
      response = renderSingleItem(itemList, newRenderTarget, addToCurrent);
    } else {
      response = renderList(itemList, newRenderTarget, addToCurrent);
    }

    // append render target
    $('#target').append(newRenderTarget);
    
    // make visibile
    newRenderTarget.show();
  }
  
  // set up JAIL
  $("img.lazy").jail({
    offset : 200,
    event: 'scroll',
    speed: 0,
    loadHiddenImages: true
  });
  
  // log
  if(itemList.length) {
    logger("[info] rendered " + itemList.length + " items");
  }
  
  // if not additive, jump to top
  if(!addToCurrent) {
    jumpToTop();
  }
  
  return response;
}

function renderList(list, intoTarget, addToCurrent) {
  if(!addToCurrent) {
    intoTarget.empty();
  }
  
  if(!list || !list.length) {
    logger("[info] no items to render");
    return false;
  }
  
  for(var i = 0; i < list.length; i++) {
    var item = list[i];
    renderSingleItem(item, intoTarget);
  }
  
  logger("[info] rendered " + list.length + " items");
  
  return true;
}

function renderMultiResponse(multiResponse, intoTarget) {
  // clear multi-header when any response comes in
  $('#multi-header-target').empty();

  // leave if no items
  if(!multiResponse.books && !multiResponse.authors && !multiResponse.series && !multiResponse.tags) {
    return false;
  }        

  // create new targets
  var bookTarget = $("<div class='container renderTarget bookTarget'></div>")
  bookTarget.hide();
  intoTarget.append(bookTarget);
  
  var authorTarget = $("<div class='container renderTarget authorTarget'></div>")
  authorTarget.hide();
  intoTarget.append(authorTarget);

  var seriesTarget = $("<div class='container renderTarget seriesTarget'></div>")
  seriesTarget.hide();
  intoTarget.append(seriesTarget);
  
  var tagTarget = $("<div class='container renderTarget tagTarget'></div>")
  tagTarget.hide();
  intoTarget.append(tagTarget);
  
  // render portions
  var bookResponse = renderList(multiResponse.books, bookTarget);
  var authorResponse = renderList(multiResponse.authors, authorTarget);
  var seriesResponse = renderList(multiResponse.series, seriesTarget);
  var tagResponse = renderList(multiResponse.tags, tagTarget);
  
  // record overall response of rendering
  var response = bookResponse || authorResponse || seriesResponse || tagResponse;
  
  // leave now if no response
  if(!response) {
    logger("[warn] early return from mult-response with no rendering");
    return false;
  }
  
  // add header 
  var header = multi_template(multiResponse);
  $('#multi-header-target').append(header);
  
  // decide which should be shown first
  if(bookTarget && bookTarget.length) {
    bookTarget.show();
    bookTarget.addClass('activeTarget');
    $('.book_tab').addClass('active');
  } else if(authorTarget && authorTarget.length) {
    authorTarget.show();
    authorTarget.addClass('activeTarget');
    $('.author_tab').addClass('active');
  } else if(seriesTarget && seriesTarget.length) {
    seriesTarget.show();
    seriesTarget.addClass('activeTarget');
    $('.series_tab').addClass('active');
  } else if(tagTarget && tagTarget.length) {
    tagTarget.show();
    tagTarget.addClass('activeTarget');
    $('.tag_tab').addClass('active');
  }
  
  return response;
}

function renderSingleItem(item, intoTarget) {
  var html = false;
    
  if("book" == item.type) {
    html = book_template(item);
  } else if("author" == item.type) {
    html = author_template(item);
  } else if ("series" == item.type || "tag" == item.type) {
    html = generic_template(item);
  } else {
    logger("[warn] target render item has no type, no rendering performed");
    return false;
  }

  var response = false;
  if(html) {                                  
    intoTarget.append(html);
    response = true;
  }
  return response;
}