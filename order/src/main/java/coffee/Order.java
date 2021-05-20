package coffee;

import javax.persistence.*;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.beans.BeanUtils;

@Entity
@DynamicInsert
@Table(name = "Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long customerId;
    private Long productId;
    @ColumnDefault("'ordered'")
    private String status;
    private Integer waitingNumber;

    @PrePersist
    public void onPrePersist() throws Exception {
        this.waitingNumber = OrderApplication.applicationContext.getBean(coffee.OrderRepository.class)
                .countByStatus("waited");
        System.out.println(this.waitingNumber);
    }

    @PostPersist
    public void onPostPersist() throws Exception {

        Integer price = OrderApplication.applicationContext.getBean(coffee.external.ProductService.class)
                .checkProductStatus(this.getProductId());

        if (price > 0) {
            boolean result = OrderApplication.applicationContext.getBean(coffee.external.CustomerService.class)
                    .checkAndModifyPoint(this.getCustomerId(), 100);

            if (result) {

                Ordered ordered = new Ordered();
                BeanUtils.copyProperties(this, ordered);
                ordered.publishAfterCommit();

            } else
                throw new Exception("Customer Point - Exception Raised");
        } else
            throw new Exception("Product Sold Out - Exception Raised");
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
