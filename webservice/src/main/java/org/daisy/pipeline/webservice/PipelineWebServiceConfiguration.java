package org.daisy.pipeline.webservice;

import java.io.File;

import org.daisy.pipeline.webserviceutils.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineWebServiceConfiguration {

	
	private boolean usesAuthentication = true;
	private long maxRequestTime = 600000; // 10 minutes in ms
	private String tmpDir=System.getProperty("java.io.tmpdir","/tmp");
	private boolean ssl=false;

	private String sslKeystore="";
	private String sslKeystorePassword="";
	private String sslKeyPassword="";

	private String clientKey=null;
	private String clientSecret=null;
	private static Logger logger = LoggerFactory.getLogger(PipelineWebServiceConfiguration.class);

	/**
	 * Constructs a new instance.
	 */
	public PipelineWebServiceConfiguration() {
		readOptions();
	}

	private void readOptions() {
		//Authentication	
		String authentication = System.getProperty(Properties.AUTHENTICATION);
		if (authentication != null) {
			if (authentication.equalsIgnoreCase("true")) {
				usesAuthentication = true;
			}
			else if (authentication.equalsIgnoreCase("false")) {
				usesAuthentication = false;
				logger.info("Web service authentication is OFF");
			}
			else {
				logger.error(String.format(
						"Value specified in option %s (%s) is not valid. Using default value of %s.",
						Properties.AUTHENTICATION, authentication, usesAuthentication));
			}
		}
		//Temporal directory
		String tmp = System.getProperty(Properties.TMPDIR);
		if (tmp != null) {
			File f = new File(tmp);
			if (f.exists()) {
				tmpDir = tmp;
			}
			else {
				logger.error(String.format(
						"Value specified in option %s (%s) is not valid. Using default value of %s.",
						Properties.TMPDIR, tmp, tmpDir));
			}
		}
			
		

		//Max req time
		String maxrequesttime = System.getProperty(Properties.MAX_REQUEST_TIME);
		if (maxrequesttime != null) {
			try {
				long ms = Long.parseLong(maxrequesttime);
				maxRequestTime = ms;
			} catch(NumberFormatException e) {
				logger.error(String.format(
						"Value specified in option %s (%s) is not a valid numeric value. Using default value of %d.",
						Properties.MAX_REQUEST_TIME, maxrequesttime, maxRequestTime));
			}
		}
		//ssl related stuff
		ssl=System.getProperty(Properties.SSL)!=null&&System.getProperty(Properties.SSL).equalsIgnoreCase("true");
		sslKeystore=System.getProperty(Properties.SSL_KEYSTORE);
		sslKeystorePassword=System.getProperty(Properties.SSL_KEYSTOREPASSWORD);
		sslKeyPassword=System.getProperty(Properties.SSL_KEYPASSWORD);


		clientKey=System.getProperty(Properties.CLIENT_KEY);
		clientSecret=System.getProperty(Properties.CLIENT_SECRET);

	}

	public String getTmpDir() {
		return tmpDir;
	}

	/**
	 * Determines if this instance is ssl.
	 *
	 * @return The ssl.
	 */
	public boolean isSsl() {
		return this.ssl;
	}

	/**
	 * Gets the sslKeystore for this instance.
	 *
	 * @return The sslKeystore.
	 */
	public String getSslKeystore() {
		return this.sslKeystore;
	}

	/**
	 * Gets the sslKeystorePassword for this instance.
	 *
	 * @return The sslKeystorePassword.
	 */
	public String getSslKeystorePassword() {
		return this.sslKeystorePassword;
	}

	/**
	 * Gets the sslKeyPassword for this instance.
	 *
	 * @return The sslKeyPassword.
	 */
	public String getSslKeyPassword() {
		return this.sslKeyPassword;
	}

	/**
	 * Gets the clientKey for this instance.
	 *
	 * @return The clientKey.
	 */
	public String getClientKey() {
		return this.clientKey;
	}

	/**
	 * Gets the clientSecret for this instance.
	 *
	 * @return The clientSecret.
	 */
	public String getClientSecret() {
		return this.clientSecret;
	}

	public boolean isAuthenticationEnabled() {
		return usesAuthentication;
	}

	
	public boolean isLocal() {
		return Boolean.valueOf(System.getProperty(Properties.LOCAL_MODE));
	}

	// the length of time in ms that a request is valid for, counting from its timestamp value
	public long getMaxRequestTime() {
		return maxRequestTime;
	}
}

