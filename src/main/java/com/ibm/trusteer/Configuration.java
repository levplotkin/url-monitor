package com.ibm.trusteer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.text.MessageFormat.format;

class Configuration {

    private static Log log = LogFactory.getLog(Configuration.class);
    private static Properties properties = new Properties();

    static {
        try {
            FileInputStream inStream = new FileInputStream("config.properties");
            properties.load(inStream);
            inStream.close();
        } catch (IOException e) {
            log.fatal(format("failed to read properties file {0}", e.getMessage()));
        }
    }

    private static final String ALGORITHM = properties.getProperty("ALGORITHM");
    static final int CORE_POOL_SIZE = Integer.parseInt(properties.getProperty("CORE_POOL_SIZE"));
    static final int PERIOD = Integer.parseInt(properties.getProperty("PERIOD"));
    static final TimeUnit TIME_UNIT = TimeUnit.valueOf(properties.getProperty("TIME_UNIT"));
    static final int CONNECT_TIMEOUT = Integer.parseInt(properties.getProperty("CONNECT_TIMEOUT"));
    static final int READ_TIMEOUT = Integer.parseInt(properties.getProperty("READ_TIMEOUT"));
    static final int SMTP_SERVER_PORT = Integer.parseInt(properties.getProperty("SMTP_SERVER_PORT"));

    static final String TO = properties.getProperty("TO");
    static final String FROM = properties.getProperty("FROM");
    static final String SUBJECT = properties.getProperty("SUBJECT");

    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            log.fatal(format(ALGORITHM + "does not exist {0}", e.getMessage()));
        }
    }

    static final MessageDigest DIGEST = digest;
}
