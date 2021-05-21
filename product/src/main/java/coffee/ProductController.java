package coffee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
public class ProductController {

        @Autowired
        ProductRepository productRepository;

        @RequestMapping(value = "/products/checkProductStatus", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public Integer checkProductStatus(@RequestParam("productId") Long productId) throws Exception {
                System.out.println("##### /product/checkProductStatus  called #####");

                Integer price = 0;
                Optional<Product> productOptional = productRepository.findById(productId);
                Product product = productOptional.get();

                if (product.getPrice() > 0) {
                        price = product.getPrice();
                }

                //임의의 부하를 위한 강제 설정
                try {
                        Thread.currentThread().sleep((long) (400 + Math.random() * 220));
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }

                return price;
        }

}