package com.janus.util;

public final class FileSizeUtil {

	private FileSizeUtil() {
		
	}
	
	/**
	 * see: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	 * 
	 * @param bytes
	 * @return
	 */
	public static String humanReadableByteCount(long bytes) {
	    int unit = 1000;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    char pre = "kMGTPE".charAt(exp-1);
	    return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
	}	
}
