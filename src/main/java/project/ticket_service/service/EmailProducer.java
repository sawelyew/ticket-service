package project.ticket_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import project.ticket_service.config.RabbitMQConfig;
import project.ticket_service.dto.EmailDtos.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailTask(String email, String otpCode) {
        EmailMessageDto message = new EmailMessageDto(email, otpCode);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );

        log.info("Email task for {} successfully queued to RabbitMQ", email);
    }
}