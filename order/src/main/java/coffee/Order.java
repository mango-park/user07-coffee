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
        System.out.println("ordered.publishAfterCommit");
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate() {

        System.out.println("ordered.onPostUpdate:" + this);

        if ("Completed".equals(this.status)) {
            Completed completed = new Completed();
            BeanUtils.copyProperties(this, completed);
            completed.publishAfterCommit();
        } else {

            System.out.println("ordered.onPostUpdate:" + this);
            System.out.println("OrderWaited Nothing");
        }
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
