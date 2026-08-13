package com.coreflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test") // <-- Utilise la configuration de test
class CoreflowBackendApplicationTests {

	// Seuls les clients AWS nécessitent encore un Mock pour éviter l'appel HTTP vers LocalStack
	@Mock
	private S3Client s3Client;

	@Mock
	private CloudWatchLogsClient cloudWatchLogsClient;

	@Test
	void contextLoads() {
	}

}
