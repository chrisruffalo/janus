// helper functions for janus templates
helper = {
	
};

function JanusTemplate(url, cache)
{
	// explicit type conversion
	if(cache) {
		cache = true;
	} else {
		cache = false;
	}
	
	// load template from URL over ajax, sync
	var response = $.ajax(url, 
	    {
			'async': false,
			'cache': cache
	    }
	);
	
	var templateString = null;
	if(200 == response.status) {
		templateString = response.responseText;
		logger("[template] loaded template: " + url);
	} else {
		logger("[template] error! " + response.status);
		return;
	}
	
	// set source to be externally visible;
	this.source = templateString;
	
	// compile template once, with data scope
	var compiled = _.template(templateString, false, {variable: 'input'});
	
	// create render function
	var renderFunction = function(data) {
		if(!data) {
			logger("[template] error: data is null");
			return false;
		}
		if(!compiled) {
			logger("[template] error: compiled template is null");
			return false;
		}
		
		// try and parse the data in case a 
		// string response was given
		try {
			data = $.parseJSON(data);
		} catch (e) {
			// do nothing, everything
			// should be fine at this
			// point
		}
		
		var rendered = compiled(data);
		return rendered;
	};

	// make render function directly visible
	this.render = renderFunction;
	
	// update elements with template
	this.update = function(jQuerySelector, data) {
		if(!jQuerySelector || !data) {
			return false;
		}
		var result = renderFunction(data);
		if(result) {		
			$(jQuerySelector).html(result);
		} else {
			logger("[template] error: result from render is null");
		}
	};
}

// load and store templates
var templates = null;
function loadTemplates() {
	templates = {
		book: new JanusTemplate('templates/book.template'),
		generic: new JanusTemplate('templates/generic.template'),
		multi: new JanusTemplate('templates/multi-response.template')
	};
}



