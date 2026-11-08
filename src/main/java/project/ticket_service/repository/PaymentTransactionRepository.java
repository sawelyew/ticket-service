package project.ticket_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ticket_service.model.PaymentTransaction;
import java.util.UUID;
import java.util.Optional;


@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByBookingId(Long bookingId);

    Optional<PaymentTransaction> findByProviderPaymentId(String providerPaymentId);
}