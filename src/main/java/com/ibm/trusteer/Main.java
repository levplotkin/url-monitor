package com.ibm.trusteer;

import com.dumbster.smtp.SimpleSmtpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.ibm.trusteer.CheckListUtility.populateCheckList;
import static com.ibm.trusteer.CheckListUtility.updateCheckList;
import static com.ibm.trusteer.Configuration.*;

public class Main {




    private static Log log = LogFactory.getLog(Configuration.class);
    private static List<CheckListEntry> checkList;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        if (args.length != 1) {
            log.info("the application expects path to file with list of urls to check");
            return;
        }

        SimpleSmtpServer fakeSmtpServer = SimpleSmtpServer.start(SMTP_SERVER_PORT);

        String urls = args[0];

        checkList = populateCheckList(urls);

        log.info(">>> Start scheduler");
        // run scheduled job
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

        scheduledExecutorService.
                scheduleAtFixedRate(() ->
                                checkList.stream().
                                        filter(checkListEntry -> !checkListEntry.isNotified()).
                                        parallel().
                                        map(
                                                updateCheckList(
                                                        message -> {
                                                            log.info(message);
                                                            MailClient.sendMessage(SMTP_SERVER_PORT, FROM, SUBJECT, message, TO);
                                                            return null;
                                                        }
                                                )
                                        ).count()
                        , 0, PERIOD, TIME_UNIT);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                log.info(">>>> " + fakeSmtpServer.getReceivedEmailSize() + " notifications was sent. Shutdown scheduler");

                fakeSmtpServer.stop();
                scheduledExecutorService.shutdown();
            }
        });
    }

}
