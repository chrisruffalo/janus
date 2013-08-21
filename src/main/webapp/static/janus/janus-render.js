function renderResponse(itemList, addToCurrent, startEntry, endEntry) {
  if(!itemList) {
    return false;
  }
  
  // if items to render we need to ensure any book is closed
  closeBook();

  $('#mainOptions').hide(); // ensure main navigation is hidden
  
  if(!addToCurrent) {
    $('.renderTarget').remove();
  }        
  
  var response = false;
  if(itemList.type && "multientityresponse" == itemList.type) {
    response = renderMultiResponse(itemList, $('#target'), addToCurrent);
  } else {
    // create new target for data
    var newRenderTarget = $("<div class='container renderTarget'></div>");

    // clear response/multi-header when any response comes in
    // only clear if not empty
    if(!addToCurrent && !$('#target').is(':empty')) {
      $('#target').empty();
    }
    $('#multi-header-target').empty();

    if(itemList.type && !itemList.length) {
      response = renderSingleItem(itemList, newRenderTarget, addToCurrent);
      if (itemList.title) {
        document.title = itemList.title + ' | Janus'; 
      } else if(itemList.name) {
        document.title = itemList.name + ' | Janus'; 
      }
    } else {
      var first = itemList[0];
      var last = itemList[itemList.length - 1];
      if (first.title && last.title) {
        document.title = first.title + ' to ' + last.title + ' | Janus'; 
      } else if (first.name && last.name) {
        document.title = first.name + ' to ' + last.name + ' | Janus'; 
      }
      response = renderList(itemList, newRenderTarget, addToCurrent);
    }

    var milestone = null;
    // if the target for rendering is empty, and adding to current target
    // this prevents a 'milestone' element from being added to an empty page
    if(addToCurrent && !$('#target').is(':empty')){
    	// create milestone message
    	var message = null;
    	if(startEntry && endEntry) {
    		message = "loaded entries " + startEntry + " to " + endEntry + " (" + (endEntry - startEntry) + ")";
    	} else {
    		var length = 1;
    		if(itemList.length) {
    			length = itemList.length;
    		}
    		message = "loaded " + length + " additional entries";
    	}
    	
    	// build milestone element
    	milestone = $('<div class="container renderTarget"><div class="row"><div class="span6 milestoneEntry"><h6 class="center milestoneTitle">' + message + '</h6></div></div></div>');
    	
	    // append milestone to target if adding new
    	$('#target').append(milestone);
    }
    
    // append render target
    $('#target').append(newRenderTarget);
    
    // jump to milestone
    jumpToElement(milestone);
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
  
  // transform readlinks and then disables the 
  // class on them so that they aren't enabled 
  // twice
  $(".readLink").click(readBook);
  $(".readLink").removeClass(".readLink");
  
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
    var header = templates.multi.render(multiResponse);
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
    html = templates.book.render(item);
  } else if ("author" == item.type || "series" == item.type || "tag" == item.type) {
    html = templates.generic.render(item);
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
  // empty previous targets
  $('#loadMoreLinkTarget').empty();
  
  // create new link
  var createdLink = $('<div id="nextPageContainer" class="container renderTarget"><div class="row"><div class="span6 linkEntry"><h6 class="center milestoneTitle"><a id="nextPageLink" href="' + href + '">load more entires</a></h6></div></div></div>');
 
  // add link to body
  $('#loadMoreLinkTarget').append(createdLink);
}

function showNoData() {
  $('#noMoreEntries').show();
}

function hideNoData() {
  $('#noMoreEntries').hide();
}