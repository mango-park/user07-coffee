package coffee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class CustomerController {

    @Autowired
    CustomerRepository customerRepository;

    @RequestMapping(value = "/customers/checkAndModifyPoint", method = RequestMethod.GET)
    public boolean checkAndModifyPoint(@RequestParam("customerId") Long customerId,
                                       @RequestParam("price") Integer price) throws Exception {
        System.out.println("##### /customer/checkAndModifyPoint  called #####");
        // Edited Source
        boolean result = false;

        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        Customer customer = customerOptional.get();
        if (customer.getCustomerPoint() >= price) {
            result = true;
            customer.setCustomerPoint(customer.getCustomerPoint() - price);
            customerRepository.save(customer);
        }

        return result;
    }
}
