
package coffee.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product", url = "${feign.client.url.productUrl}")
public interface ProductService {

   @RequestMapping(method = RequestMethod.GET, path = "/products/checkProductStatus")
   public Integer checkProductStatus(@RequestParam("productId") Long productId);
}