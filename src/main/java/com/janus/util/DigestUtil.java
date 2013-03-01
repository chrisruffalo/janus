package com.janus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DigestUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(DigestUtil.class);
	
	private DigestUtil() {
		
	}
	
	/**
	 * Return the MD5 digest of a given file
	 * 
	 * @param file
	 * @return
	 */
	public static String fileDigest(File file) {
		
		InputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			DigestUtil.LOGGER.error("An error occurred while performing digest: {}", e.getMessage());
			return "NO-DIGEST";
		}		
				
		// get digest
		String digest;
		try {
			digest = DigestUtils.md5Hex(is);
		} catch (IOException e) {
			DigestUtil.LOGGER.error("An error occurred while performing digest: {}", e.getMessage());
			return "NO-DIGEST";
		}
		
		// produce hex string from byte array
		return digest;
	}
	
	/**
	 * 
	 * 
	 * @param password
	 * @return
	 */
	public static String passwordDigest(String salt, String password) {
		// return hex encoded digest
		return DigestUtils.sha512Hex(salt + password);
	}
	
}
