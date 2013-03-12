// log stuff
function logger(logString) {
  if(console && console.log) {
    console.log(logString);
  }
}

// toggle helper for tabs
function toggleTarget(showTarget) {
  // hide and deactivate active status on currently active tab
  $('.activeTarget').hide();
  $('.activeTarget').removeClass('.activeTarget');
  
  // show new target and make active
  $('.'+showTarget+'Target').show();
  $('.'+showTarget+'Target').addClass('activeTarget');
  
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