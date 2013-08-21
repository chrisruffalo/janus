/**
 * Load templates
 *
 */

var multi_source = $('#multi-response-header-template').html();
var multi_template = Handlebars.compile(multi_source);

/** 
 * Load Helper functions
 *
 */
Handlebars.registerHelper('inJavascriptEscape', function(text) {
	text = escape(text);
	return text;
});