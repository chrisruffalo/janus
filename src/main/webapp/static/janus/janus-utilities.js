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

function toggleQR(qrUrl, title) {
  // remove previous qr modals
  $('.qrmodal').remove();
	
  // create modal
  var modal = $("<div id='tempModal' class='qrmodal modal hide fade' tabindex='-1' role='dialog'>" +
	"<div class='modal-header'>" +
	"<button type='button' class='close' data-dismiss='modal' aria-hidden='true'><i class='icon-remove'></i></button>" +
	"<h3 id='modal-label'>" + unescape(title) + " </h3>" +
	"</div>" +
	"<div class='modal-body'>" +
	"<center><img src='" + qrUrl + "'></img></center>" +
	"</div>" +
	"<div class='modal-footer'>" +
	"<button class='btn' data-dismiss='modal' aria-hidden='true'>close</button>" +
	"</div>" +
	"</div>");	
  
  // append to body
  $("body").append(modal);
  
  // create modal
  $('#tempModal').modal();
}