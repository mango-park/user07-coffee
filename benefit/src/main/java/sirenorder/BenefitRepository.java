package sirenorder;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="benefits", path="benefits")
public interface BenefitRepository extends PagingAndSortingRepository<Benefit, Long>{


}
