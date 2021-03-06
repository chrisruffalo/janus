// turns on the book email modal
function toggleBookEmail(id, title) {
  logger("[email] opening email dialog");
	
  // empty any previous alerts
  $('#email-modal-error-container').empty();
  
  // enable all fields and buttons
  $('#email-text').removeAttr('disabled');
  $('#email-host').removeAttr('disabled');
  $('#email-tld').removeAttr('disabled');
  $('#email-file-type').removeAttr('disabled');
  $('#email-do-button').removeAttr('disabled');
	
  logger("[email] setting title");
  
  // set modal values
  $('#email-modal-label').html("Emailing <em>" + unescape(title) + "</em>");
  
  logger("[email] setting up 'loading'");
  
  // show 'loading' dialog bit	 
  $('#email-type-holder').empty();
  $('#email-type-holder').html('<em>loading available files...</em>');
  
  // read cookies...
  var emailName = getCookie(EMAIL_NAME_PROPERTY, "email");
  var emailHost = getCookie(EMAIL_HOST_PROPERTY, "kindle");
  var emailTld = getCookie(EMAIL_TLD_PROPERTY, "com");
  
  // set form values (in some cases from cookies) before showing form
  $('#email-id').val(id);
  $('#email-text').val(emailName);
  $('#email-host').val(emailHost);
  $('#email-tld').val(emailTld);
  
  logger("[email] starting request");
  
  // kick off request
  $.ajax({
	  url: 's/book/' + id + '/list',
	  cache: false
  })  
  .done(
    function(context) {
    	logger("[email] got response for types");
    	
    	// clear value list
    	$('#email-type-holder').empty();
    	
    	// context
    	if(context && context.length > 0) {
	    	// create string from options
	    	var fileTypes = "<select id='email-file-type'>";
	    	for(var i = 0; i < context.length; i++) {
	    		// get item
	    		var item = context[i];
	    		
	    		// if the item isn't available or doesn't have a type
	    		// then go-around
	    		if(!item || !item.type) {
	    			continue;
	    		}
	    		
	    		// option
	    		fileTypes += "<option value='" + item.type + "'>" + item.type + " (" + item.descriptiveSize + ")" + "</option>";
	    	}		    	
	    	// close available options
	    	fileTypes += "</select>";
	    	
	    	// add content to holder
	    	var content = $(fileTypes);
    		$('#email-type-holder').append(content);
    	} else {
    		$('#email-type-holder').html('<strong>no files available</strong>');
    	}
    }
  )
  .fail(
	function() {
		$('#email-type-holder').empty();
		$('#email-type-holder').html('<strong>no files available</strong>');
	}	  
  );
  
  // create modal
  $('#email-modal').modal();
}

function doEmail() {
    // empty any previous alerts
    $('#email-modal-error-container').empty();
	
    // lock form
    $('#email-text').attr('disabled', 'true');
    $('#email-host').attr('disabled', 'true');
    $('#email-tld').attr('disabled', 'true');
    $('#email-file-type').attr('disabled', 'true');
    
    // lock button
    $('#email-do-button').attr('disabled', 'true');
    
	// get values
    var id = $('#email-id').val();
    var name = $('#email-text').val();
    var host = $('#email-host').val();
    var tld = $('#email-tld').val();
    var type = $('#email-file-type').val();
	
    // concat email
    var email = name + "@" + host + "." + tld;    
    
	// post request for email to endpoint
    $.ajax({
    	url: 's/book/' + id + '/email/' + type + '?address=' + email,
    	cache: false,    	
    })
    // on success clean up form, show success message
    .done(
      function(context) {
    	  // empty other alerts
    	  $('#email-modal-error-container').empty();
    	  
    	  // show success message
    	  var successAlert = $('<div class="alert alert-success">'
    		  + '<button type="button" class="close" data-dismiss="alert">&times;</button>'
    		  + '<strong>Awesome!</strong> Your book is on the way.'
    		  + '</div>');
    	  $('#email-modal-error-container').append(successAlert);
    	  
    	  // save email values to cookies
    	  setCookie(EMAIL_NAME_PROPERTY, name);
    	  setCookie(EMAIL_HOST_PROPERTY, host);
    	  setCookie(EMAIL_TLD_PROPERTY, tld);
      }
    )
    // on error we need to show error message and
    // take other remedial actions
    .fail(
      function() {
    	  // empty other alerts
    	  $('#email-modal-error-container').empty();

    	  // show error message
    	  var errorAlert = $('<div class="alert alert-error">'
    		  + '<button type="button" class="close" data-dismiss="alert">&times;</button>'
    		  + '<strong>Oh no!</strong> An error occurred while sending your book.  Check your values and try again.'
    		  + '</div>');
    	  $('#email-modal-error-container').append(errorAlert);
      }
    )
    // always unlock the form
    .always(
      function() {
    	  $('#email-text').removeAttr('disabled');
    	  $('#email-host').removeAttr('disabled');
    	  $('#email-tld').removeAttr('disabled');
    	  $('#email-file-type').removeAttr('disabled');
    	  $('#email-do-button').removeAttr('disabled');
      }
    );
}