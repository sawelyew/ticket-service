package project.ticket_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import project.ticket_service.config.RabbitMQConfig;
import project.ticket_service.dto.EmailDtos.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmailTask(EmailMessageDto message) {
        try {
            log.info("Processing email task for: {}", message.email());

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(message.email());
            mailMessage.setSubject("Код подтверждения регистрации");
            mailMessage.setText("Ваш код подтверждения: " + message.otpCode() + "\nКод действителен 5 минут.");

            mailSender.send(mailMessage);
            log.info("Email successfully sent to {}", message.email());

        } catch (Exception e) {
            log.error("Failed to process email task for {}: {}", message.email(), e.getMessage());
            throw e;
        }
    }
}