function readBook(event) {
	// hide listing stuff
	$('body').children(":visible").children(":not(.reading)").addClass("listing");
	$('.listing').hide();
	
	// show reading stuff
	$('body').children(".reading").show();
	
	// change body style
	$('body').addClass('reading-body');
	
	// get path to book
	var href = this.href;
	
    FP.VERSION = "0.1.6";
    
    FP.filePath = "static/fpjs/dist/";
    fileStorage.filePath = FP.filePath + "libs/";

    // log
    logger("loading book at: " + href);
    
    startBook(href);
    
    event.preventDefault();
    event.stopPropagation();
    
    // set up close click
    $("#close").click(function() {
    	closeBook();
    });
    
    // do not complete action
    return false;
}

function startBook(bookUrl) {
	// init
    FPR.app.init(bookUrl);
}

function closeBook() {
	// hide listing stuff
	$('.listing').show();
	$('.listing').removeClass("listing");
	
	// show reading stuff
	$('body').children(".reading").hide();
	
	$('body').removeClass('reading-body');
	
	// empty book render area to cut down on DOM use
	$("#area").empty();
	
	// fix document title
	document.title = 'Janus';
}