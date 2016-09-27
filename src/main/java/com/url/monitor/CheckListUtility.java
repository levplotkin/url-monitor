package com.url.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;

import static java.text.MessageFormat.format;

class CheckListUtility {

    private static Log log = LogFactory.getLog(CheckListUtility.class);

    static List<CheckListEntry> populateCheckList(String urls) {

        List<CheckListEntry> checkList;

        try {
            checkList = readUrlsFromConfiguration(urls);
        } catch (IOException e) {
            log.error(format("Failed get urls from config : {0}. {1}", urls, e.getMessage()));
            return Collections.emptyList();
        }

        // init checklist
        checkList.stream().parallel().map(
                updateCheckList(registryEntry -> null)
        ).forEach(
                checkListEntry -> checkListEntry.setNotified(false)
        );

        return checkList;
    }

    private static List<CheckListEntry> readUrlsFromConfiguration(String path) throws IOException {

        Scanner sc = new Scanner(new File(path));

        List<CheckListEntry> checkList = new ArrayList<>();

        while (sc.hasNextLine()) {

            URL url = new URL(sc.next());

            String host = sc.next();

            CheckListEntry checkListEntry = "*".equals(host) ?
                    new CheckListEntry(url, url, null, 0) :
                    new CheckListEntry(url, new URL(url.getProtocol(), host, url.getPort(), url.getFile()), null, 0);

            if (!checkList.contains(checkListEntry))
                checkList.add(checkListEntry);
        }
        sc.close();
        return checkList;
    }

    static Function<CheckListEntry, CheckListEntry> updateCheckList(Function<String, Void> notify) {
        return registryEntry -> {
            try {
                String content = getContentFromURL(registryEntry.getUrl().openConnection(), Configuration.CONNECT_TIMEOUT, Configuration.READ_TIMEOUT);
                String newSignature = getSignature(content, Configuration.DIGEST);

                if (newSignature.equals(registryEntry.getSignature())) {
                    registryEntry.setLastUpdate(System.currentTimeMillis());
                } else {
                    registryEntry.setLastUpdate(System.currentTimeMillis());
                    registryEntry.setSignature(newSignature);
                    notify.apply(format("content changed, url: {0}", registryEntry.getUrl()));
                    registryEntry.setNotified(true);
                }

            } catch (IOException e) {
                notify.apply(format("failed to update  {0},  last update {1}", e.getMessage(), new Date(registryEntry.getLastUpdate())));
            }
            return registryEntry;
        };
    }

    private static String getContentFromURL(URLConnection urlConnection, int connectTimeout, int readTimeout) throws IOException {

        InputStream inputStream = null;
        Scanner scanner = null;
        try {

            urlConnection.setConnectTimeout(connectTimeout);
            urlConnection.setReadTimeout(readTimeout);

            inputStream = urlConnection.getInputStream();

            scanner = new Scanner(inputStream).useDelimiter("\\A");

            return scanner.hasNext() ? scanner.next() : "";

        } finally {

            if (scanner != null)
                scanner.close();

            if (inputStream != null)
                inputStream.close();
        }
    }

    private static String getSignature(String content, MessageDigest digest) throws UnsupportedEncodingException {
        digest.update(content.getBytes());
        return javax.xml.bind.DatatypeConverter.printHexBinary(digest.digest());
    }
}
