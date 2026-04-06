package com.ticket_online.domain.payment.application;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ticket_online.domain.booking.dao.OrderRepository;
import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.catalog.dao.ShowRepository;
import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.domain.payment.domain.PayProvider;
import com.ticket_online.domain.payment.dto.PaymentUrlResponse;
import com.ticket_online.domain.payment.strategy.PaymentStrategy;
import com.ticket_online.domain.payment.strategy.PaymentStrategyFactory;
import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentServiceTest {
    @Autowired private PaymentService paymentService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ShowRepository showRepository;

    @MockBean(name = "vnpayStrategy")
    private PaymentStrategy vnpayStrategy;

    @Autowired private UserRepository userRepository;
    @MockBean private PaymentStrategyFactory paymentStrategyFactory;

    @Test
    void createPayment() {
        // given
        Show show = Show.createShow(LocalDateTime.now().plusDays(2), "Test Show", "100");
        User user = User.createUser("camdao", "");
        Order order = Order.createOrder(user, show, BigDecimal.valueOf(50000));
        userRepository.save(user);
        showRepository.save(show);
        orderRepository.save(order);
        when(paymentStrategyFactory.getPaymentStrategy(PayProvider.VNPAY.name()))
                .thenReturn(vnpayStrategy);

        when(vnpayStrategy.createPayment(any()))
                .thenReturn(new PaymentUrlResponse("http://test-url"));
        // when
        PaymentUrlResponse url = paymentService.createPayment(order);
        // then
        assertNotNull(url);
        assertEquals("http://test-url", url.paymentUrl());
    }
}
