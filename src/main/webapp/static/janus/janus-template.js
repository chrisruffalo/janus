/**
 * Load templates
 *
 */

var book_source   = $("#book-template").html();
var book_template = Handlebars.compile(book_source);

var generic_source = $('#generic-template').html();
var generic_template = Handlebars.compile(generic_source);

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