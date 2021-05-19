
package coffee.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

//@FeignClient(name="customer", url="http://localhost:8081")
@FeignClient(name="customer", url="http://customer:8080")
public interface CustomerService {

//   @RequestMapping(method= RequestMethod.GET, path="/customers")
//  public boolean checkAndModifyPoint(@RequestBody Customer customer);
    @RequestMapping(method= RequestMethod.GET, path="/customers/checkAndModifyPoint")
    public boolean checkAndModifyPoint(
        @RequestParam("customerId") Long customerId,
        @RequestParam("price") Integer price);
//  @RequestMapping(method= RequestMethod.GET, path="/products")
//  public boolean checkProductStatus(@RequestParam("productId") Long productId);

}