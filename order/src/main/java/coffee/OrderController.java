package coffee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @RequestMapping(value = "/orders/order", method = RequestMethod.GET)
    public void order(@RequestParam("customerId") Long customerId, @RequestParam("productId") Long productId, @RequestParam("benefitUseYn") String benefitUseYn) throws Exception {

        Integer price = OrderApplication.applicationContext.getBean(coffee.external.ProductService.class)
                .checkProductStatus(productId);

        System.out.println("price: " + price);
        Order order = new Order();

        if (price > 0) {
            boolean benefitResult = false;
            if ("Y".equals(benefitUseYn)) {
                benefitResult = OrderApplication.applicationContext.getBean(coffee.external.BenefitService.class)
                        .checkAndUsed(customerId);

                System.out.println("checkAndUsed benefitResult: " + benefitResult);
            }

            if (benefitResult) {
                boolean result = OrderApplication.applicationContext.getBean(coffee.external.CustomerService.class)
                        .checkAndModifyPoint(customerId, price);

                System.out.println("checkAndModifyPoint result: " + result);
                if (result) {

                    order.setCustomerId(customerId);
                    order.setProductId(productId);

                    orderRepository.save(order);

//                    System.out.println("ordered.publishAfterCommit");
//                    Ordered ordered = new Ordered();
//                    //BeanUtils.copyProperties(this, ordered);
//                    ordered.publishAfterCommit();

                    //Following code causes dependency to external APIs
                    // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

                } else
                    throw new Exception("Customer Point - Exception Raised");
            } else {
                throw new Exception("Benefit Stamp - Exception Raised");
            }
        } else
            throw new Exception("Product Sold Out - Exception Raised");
    }
//    return order;
}
