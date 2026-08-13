package com.coreflow;

import com.coreflow.order.event.OrderProducer;
import com.coreflow.order.repository.OrderRepository;
import com.coreflow.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test") // <-- Utilise la configuration de test
class CoreflowBackendApplicationTests {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderProducer orderProducer;

	@InjectMocks
	private OrderService orderService;

	@Test
	void contextLoads() {
	}

}
