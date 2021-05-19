package coffee;

import java.util.Optional;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CustomerRepository extends PagingAndSortingRepository<Customer, Long>{

    Optional<Customer> findById(Long id);

}