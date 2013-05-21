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
  $('#qr-modal-label').html(unescape(title));
  
  // set qr image in img item of modal
  $('#qr-modal-body').empty();
  var img = $('<img src="s/' + type +'/' + id + '/qr">');
  $('#qr-modal-body').append(img);
	
  // create modal
  $('#qr-modal').modal();
}

function toggleBookEmail(id, title) {
	  // set modal values
	  $('#email-modal-label').html(unescape(title));
	  
	  // kick off entity request to fill out form	 
	  
	  // create modal
	  $('#email-modal').modal();
	}