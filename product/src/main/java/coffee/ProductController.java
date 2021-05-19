package coffee;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.util.List;
import java.util.Optional;

 @RestController
 public class ProductController {

        @Autowired 
        ProductRepository productRepository;

        @RequestMapping(value = "/products/checkProductStatus",
                method = RequestMethod.GET,
                produces = "application/json;charset=UTF-8")

// public boolean checkAndModifyPoint(HttpServletRequest request, HttpServletResponse response)
public Integer checkProductStatus(@RequestParam("productId") Long productId)
        throws Exception {
        System.out.println("##### /product/checkProductStatus  called #####");
//Edited Source
                Integer price = 0;
                Optional<Product> productOptional = productRepository.findById(productId);
                Product product = productOptional.get();
                // msg = Integer.toString(product.getPrice());  
                // System.out.println("***********************" + msg);
                if (product.getPrice() > 0) {
                        price = product.getPrice();
                } 
           return price;
        }

 }