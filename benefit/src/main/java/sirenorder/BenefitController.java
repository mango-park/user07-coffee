package sirenorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

 @RestController
 public class BenefitController {

     @Value("${test.configMap.stampDeductionCount}")
     public String stampDeductionCount;

     @Autowired
     BenefitRepository benefitRepository;

     @RequestMapping(value = "/benefits/checkAndUsed", method = RequestMethod.GET)
     public boolean checkAndUsed(@RequestParam("customerId") Long customerId) throws Exception {
         System.out.println("##### /benefit/checkAndUsed  called #####");
         // Edited Source
         boolean result = false;

         Optional<Benefit> benefitOptional = benefitRepository.findTop1ByCustomerId(customerId);
         Benefit benefit = benefitOptional.get();
         if (benefit.getStamp() >= Long.parseLong(stampDeductionCount)) {
             result = true;
             benefit.setStamp(benefit.getStamp() - Long.parseLong(stampDeductionCount));
             benefitRepository.save(benefit);
         }

         return result;
     }


@RequestMapping(value = "/",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8")

public void manualGrant(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        System.out.println("##### /benefit/manualGrant  called #####");
        }
 }
