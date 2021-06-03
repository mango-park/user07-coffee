
package coffee.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@FeignClient(name="benefit", url="http://benefit:8080")
public interface BenefitService {


    @RequestMapping(method = RequestMethod.GET, path = "/benefits/checkAndUsed")
    public boolean checkAndUsed(@RequestParam("customerId") Long customerId);
}