package dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.paymentJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PaymentStatisticsJpaRepository {

    @Query("SELECT COUNT(p) FROM PaymentEntity p WHERE p.status.name = :status AND p.removed = false")
    long countByStatus(@Param("status") String status);

    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.status.name = 'COMPLETED' AND p.removed = false")
    Double getTotalCompletedAmount();

    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.status.name = 'COMPLETED' AND p.createdAt BETWEEN :start AND :end AND p.removed = false")
    Double getTotalCompletedAmountByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT CAST(p.createdAt AS date), COUNT(p), SUM(p.amount) FROM PaymentEntity p " +
            "WHERE p.status.name = 'COMPLETED' AND p.createdAt BETWEEN :start AND :end AND p.removed = false " +
            "GROUP BY CAST(p.createdAt AS date) ORDER BY CAST(p.createdAt AS date)")
    List<Object[]> getDailyPaymentStats(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT p.method.name, COUNT(p), SUM(p.amount) FROM PaymentEntity p " +
            "WHERE p.status.name = 'COMPLETED' AND p.removed = false GROUP BY p.method.name")
    List<Object[]> getPaymentStatsByMethod();
}