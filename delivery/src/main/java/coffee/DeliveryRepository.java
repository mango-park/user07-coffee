package coffee;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface DeliveryRepository extends PagingAndSortingRepository<Delivery, Long>{

    Optional<Delivery> findByStatus(String Status);

}