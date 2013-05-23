// log stuff
function logger(logString) {
  if(console && console.log) {
    console.log(logString);
  }
}

// toggle helper for tabs
function toggleTarget(showTarget) {
  // hide and deactivate active status on currently active tab
  var currentActive = $('.activeTarget');
  currentActive.hide();
  currentActive.removeClass('activeTarget');
  
  // show new target and make active
  var target = $('.' + showTarget + 'Target');
  target.show();
  target.addClass('activeTarget');
  logger('showing ' + showTarget + ' related items');
  
  // change look of tab
  $('.active').removeClass('active');
  $('.'+showTarget+'_tab').addClass('active');
}
    
// jump to the top of the document  
function jumpToTop(){
  $("html, body").animate({ scrollTop: 0 }, 600);
  return false;
}

//jump to the top of the document  
function jumpToElement(element){
  if(!element) {
	  return false;
  }
  
  // try and do scroll to element
  try {
	  var top = element.offset().top;
	  // scroll to top minus modest offset (40px for top border, 5px for meeee)
	  var to = top - 45;
	  $("html, body").animate({ scrollTop: to }, 600);
  } catch (e) {
	  logger('[error] error while scrolling to given element: ' + e);
  }  
  return false;
}

// download file
function downloadFile(id) {
  var typeString = $('#download_box_'+id).val();
  var url = "s/book/" + id + "/file/" + typeString;
  window.location.assign(url);
}

function makeBoolean(value) {
  if(value && (value == "yes" || value=="true")) {
    return true; 
  }
  
  return false;
}

function toggleQR(type, id, title) {
  // set modal values
  $('#qr-modal-label').html("<em>" + unescape(title) + "</em>");
  
  // set qr image in img item of modal
  $('#qr-modal-body').empty();
  var img = $('<img src="s/' + type +'/' + id + '/qr">');
  $('#qr-modal-body').append(img);
	
  // create modal
  $('#qr-modal').modal();
}

function setCookie(name, value, days) {
	var expires = null;
	
	if(!days) { 
		days = 3650;
	}
	
	var date = new Date();
	date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
	expires = "; expires=" + date.toGMTString();

	document.cookie = name + "=" + value + expires + "; path=/";
}

function getCookie(name, defaultValue) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i = 0; i < ca.length; i++) {
		var c = ca[i];
		
		while (c.charAt(0) == ' ') {
			c = c.substring(1, c.length);
		}
		
		if (c.indexOf(nameEQ) == 0) {
			return c.substring(nameEQ.length, c.length);
		}
	}
	
	return defaultValue;
}

function deleteCookie(name) {
	setCookie(name, "", -1);
}

/**
 * Load user-specific configuration from cookies
 * 
 */
function loadConfigurationFromCookies() {
	// load page size as set from cookie (overrides default, uses default as default value)
	DEFAULT_PAGE_SIZE = getCookie(DEFAULT_PAGE_SIZE_PROPERTY, DEFAULT_PAGE_SIZE);
	logger("[debug] loaded default page size: " + DEFAULT_PAGE_SIZE);
}

/**
 * Public function for showing an error near the title bar
 * 
 */
function showErrorInContainer(title, message) {
	if(!message) {
		// don't show non-available message
		return;
	}
	
	// empty errors
	$('#error-div').empty();
	
	// create error html
    var error = $('<div class="alert alert-error">'
		  + '<button type="button" class="close" data-dismiss="alert">&times;</button>'
		  + '<strong>' + title + '</strong> ' + message
		  + '</div>');
    
    // put error in document
    $('#error-div').append(error);
}

function clearErrors() {
	// empty errors
	$('#error-div').empty();	
}