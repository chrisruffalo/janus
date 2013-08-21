// log level
var LOG_LEVELS = {
	error: {
		tag: "error",
		index: 5
	},
	warn : {
		tag: "warn",
		index: 4
	},
	info : {
		tag: "info",
		index: 3
	}, 
	debug: {
		tag: "debug",
		index: 2
	},
	trace: {
		tag: "trace",
		index: 1
	}
};
var DEFAULT_LOG_LEVEL = LOG_LEVELS.info;
var LOG_LEVEL = DEFAULT_LOG_LEVEL;

function Logger(name) {

	var log = function(level, message) {
		// if log level is higher than
		// that of the message, don't
		// print
		if(level.index < LOG_LEVEL.index) {
			return;
		}
		
		// otherwise, we can acutally log
		var logLine = level.tag.toUpperCase() + ": [" + name + "] " + message;
		
		// send logline to console if
		// the console is available
		if(console && console.log) {
			console.log(logLine);
		}
	};
	
	this.info = function(message) {
		log(LOG_LEVELS.info, message);
	};
	
	this.error = function(message) {
		log(LOG_LEVELS.error, message);
	};
	
	this.warn = function(message) {
		log(LOG_LEVELS.warn, message);
	};
	
	this.trace = function(message) {
		log(LOG_LEVELS.trace, message);
	};
	
	this.debug = function(message) {
		log(LOG_LEVELS.debug, message);
	};
}

// re-use loggers
var LOGGERS = {};
var LoggerFactory = {
	get : function(name) {
		if(LOGGERS[name]) {
			return LOGGERS[name];
		}
		var logger = new Logger(name);
		LOGGERS[name] = logger;
		return logger;
	}
};