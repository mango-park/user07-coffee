
package coffee.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "customer", url = "${feign.client.url.customerUrl}")
public interface CustomerService {

    @RequestMapping(method = RequestMethod.GET, path = "/customers/checkAndModifyPoint")
    public boolean checkAndModifyPoint(@RequestParam("customerId") Long customerId,
            @RequestParam("price") Integer price);

}