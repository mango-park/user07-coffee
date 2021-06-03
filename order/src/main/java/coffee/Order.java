package coffee;

import javax.persistence.*;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Date;

@Entity
@Table(name = "Order_table")
public class Order {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long customerId;
    private Long productId;
    @ColumnDefault("'Ordered'")
    private String status;
    private Integer waitingNumber;

    @PrePersist
    public void onPrePersist() throws Exception {
        this.waitingNumber = OrderApplication.applicationContext.getBean(coffee.OrderRepository.class)
                .countByStatus("Waited");
        System.out.println(this.waitingNumber);
    }

    @PostPersist
    public void onPostPersist() throws Exception {

//        Integer price = OrderApplication.applicationContext.getBean(coffee.external.ProductService.class)
//                .checkProductStatus(this.getProductId());
//
//        System.out.println("price: "+price);
//
//        if (price > 0) {
//
//
//            boolean benefitResult = OrderApplication.applicationContext.getBean(coffee.external.BenefitService.class)
//                    .checkAndUsed(this.getCustomerId());
//
//            System.out.println("checkAndUsed benefitResult: "+benefitResult);
//
//            if (benefitResult) {
//                boolean result = OrderApplication.applicationContext.getBean(coffee.external.CustomerService.class)
//                        .checkAndModifyPoint(this.getCustomerId(), price);
//
//                System.out.println("checkAndModifyPoint result: "+result);
//                if (result) {
//
//                    System.out.println("ordered.publishAfterCommit");
//                    Ordered ordered = new Ordered();
//                    BeanUtils.copyProperties(this, ordered);
//                    ordered.publishAfterCommit();
//
//                    //Following code causes dependency to external APIs
//                    // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.
//
//                } else
//                    throw new Exception("Customer Point - Exception Raised");
//            } else {
//                throw new Exception("Benefit Stamp - Exception Raised");
//            }
//        } else
//            throw new Exception("Product Sold Out - Exception Raised");

        System.out.println("ordered.publishAfterCommit");
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate() {
        Completed completed = new Completed();
        BeanUtils.copyProperties(this, completed);
        completed.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getWaitingNumber() {
        return waitingNumber;
    }

    public void setWaitingNumber(Integer waitingNumber) {
        this.waitingNumber = waitingNumber;
    }


}
