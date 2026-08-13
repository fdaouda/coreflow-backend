package com.coreflow.common;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.util.Collections;

@Service
public class CloudWatchAuditService {

    private final CloudWatchLogsClient logsClient;
    private static final String LOG_GROUP_NAME = "/aws/coreflow/audit-logs";
    private static final String LOG_STREAM_NAME = "application-stream";

    public CloudWatchAuditService(CloudWatchLogsClient logsClient) {
        this.logsClient = logsClient;
    }

    @PostConstruct
    public void initCloudWatchResources() {
        try {
            // 1. Créer le Log Group s'il n'existe pas
            logsClient.createLogGroup(CreateLogGroupRequest.builder().logGroupName(LOG_GROUP_NAME).build());
            System.out.println("✅ Log Group CloudWatch créé : " + LOG_GROUP_NAME);
        } catch (ResourceAlreadyExistsException e) {
            // Le groupe existe déjà, c'est normal
        } catch (Exception e) {
            System.err.println("⚠️ Erreur création Log Group : " + e.getMessage());
        }

        try {
            // 2. Créer le Log Stream
            logsClient.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(LOG_GROUP_NAME)
                    .logStreamName(LOG_STREAM_NAME)
                    .build());
        } catch (ResourceAlreadyExistsException e) {
            // Le stream existe déjà
        } catch (Exception e) {
            System.err.println("⚠️ Erreur création Log Stream : " + e.getMessage());
        }
    }

    /**
     * Envoie un événement de log/audit vers AWS CloudWatch
     */
    public void sendAuditLog(String message) {
        InputLogEvent logEvent = InputLogEvent.builder()
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();

        PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                .logGroupName(LOG_GROUP_NAME)
                .logStreamName(LOG_STREAM_NAME)
                .logEvents(Collections.singletonList(logEvent))
                .build();

        logsClient.putLogEvents(putLogEventsRequest);
        System.out.println("☁️ Log envoyé à CloudWatch : " + message);
    }
}
