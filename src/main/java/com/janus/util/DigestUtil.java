package com.janus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
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
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			DigestUtil.LOGGER.error("An error occurred while performing digest: {}", e1.getMessage());
			return "NO-DIGEST";
		}
		
		InputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			DigestUtil.LOGGER.error("An error occurred while performing digest: {}", e1.getMessage());
			return "NO-DIGEST";
		}		
		
		try {
		  is = new DigestInputStream(is, md);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				DigestUtil.LOGGER.error("An error occurred while closing the file after performing digest: {}", e.getMessage());
			}
		}
		
		// get digest
		byte[] digest = md.digest();
		
		// do better at producing string
		return Hex.encodeHexString(digest);
	}
	
}
