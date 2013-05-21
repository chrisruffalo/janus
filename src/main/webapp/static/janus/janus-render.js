
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
    response = renderMultiResponse(itemList, $('#target'), addToCurrent);
  } else {
    // create new target for data
    var newRenderTarget = $("<div class='container renderTarget'></div>")
    newRenderTarget.hide();

    // clear response/multi-header when any response comes in
    if(!addToCurrent) {
      $('#target').empty();
    }
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
  
  // set up JAIL to load images async
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
  // no target means no rendering
  if(!intoTarget) {
    return false;
  }
  
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

function renderMultiResponse(multiResponse, intoTarget, addToCurrent) {
  // no target means no rendering
  if(!intoTarget) {
    return false;
  }
  
  // clear multi-header when any response comes in
  $('#multi-header-target').empty();

  // leave if no items
  if(!multiResponse.books && !multiResponse.authors && !multiResponse.series && !multiResponse.tags) {
    return false;
  }        

  // create new targets
  var bookTarget = createOrUpdateTarget('bookTarget', addToCurrent, intoTarget);
  var authorTarget = createOrUpdateTarget('authorTarget', addToCurrent, intoTarget);
  var seriesTarget = createOrUpdateTarget('seriesTarget', addToCurrent, intoTarget);
  var tagTarget = createOrUpdateTarget('tagTarget', addToCurrent, intoTarget);
  
  // render portions
  var bookResponse = renderList(multiResponse.books, bookTarget, addToCurrent);
  var authorResponse = renderList(multiResponse.authors, authorTarget, addToCurrent);
  var seriesResponse = renderList(multiResponse.series, seriesTarget, addToCurrent);
  var tagResponse = renderList(multiResponse.tags, tagTarget, addToCurrent);
  
  // record overall response of rendering
  var response = bookResponse || authorResponse || seriesResponse || tagResponse;
  
  // leave now if no response
  if(!response) {
    logger("[warn] early return from mult-response with no rendering");
    return false;
  }
  
  // add header if not already added
  if(!addToCurrent) {
    var header = multi_template(multiResponse);
    $('#multi-header-target').append(header);
  }
  
  
  var activeTarget = null;
  var targetHeaderClass = null;
  
  // decide which should be shown first
  if(bookTarget && bookTarget.length) {
    activeTarget = bookTarget;
    targetHeaderClass = 'book_tab';
  } else if(authorTarget && authorTarget.length) {
    activeTarget = bookTarget;
    targetHeaderClass = 'author_tab';
  } else if(seriesTarget && seriesTarget.length) {
    activeTarget = bookTarget;
    targetHeaderClass = 'series_tab';
  } else if(tagTarget && tagTarget.length) {
    activeTarget = bookTarget;
    targetHeaderClass = 'tag_tab';
  }
  
  // actual show logic
  if(activeTarget) {
    activeTarget.show();
    activeTarget.addClass('activeTarget');
    $('#' + targetHeaderClass).addClass('active'); 
  }
  
  return response;
}

function createOrUpdateTarget(specificTargetClass, addToCurrent, intoTargetElement) {
  var target = null;
  if(addToCurrent) {
    target = $(".renderTarget ." + specificTargetClass);
  } else {
    target = $("<div class='container renderTarget" + specificTargetClass + "'></div>");
    intoTargetElement.append(target);
  }
  target.hide();
  
  // be sure to return target element, not just create
  return target;
}

function renderSingleItem(item, intoTarget) {
  // no target means no rendering
  if(!intoTarget) {
    return false;
  }
  
  var html = false;
    
  if("book" == item.type) {
    html = book_template(item);
  } else if ("author" == item.type || "series" == item.type || "tag" == item.type) {
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

// render next page link
function renderNextPageLink(href) {
  $('#loadMoreLinkTarget').empty();
  var createdLink = $("<a href='" + href +"'>load more entires</a>");
  $('#loadMoreLinkTarget').append(createdLink);
}

function showNoData() {
  $('#noMoreEntries').show();
}

function hideNoData() {
  $('#noMoreEntries').hide();
}