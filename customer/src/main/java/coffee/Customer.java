package coffee;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Customer_table")
public class Customer {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Integer customerPoint;

    @PostPersist
    public void onPostPersist(){
        RegisteredCustomer registeredCustomer = new RegisteredCustomer();
        BeanUtils.copyProperties(this, registeredCustomer);
        registeredCustomer.publishAfterCommit();

    }

    @PreUpdate
    public void onPreUpdate(){
        ModifiedPoint modifiedPoint = new ModifiedPoint();
        BeanUtils.copyProperties(this, modifiedPoint);
        modifiedPoint.publishAfterCommit();

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getCustomerPoint() {
        return customerPoint;
    }

    public void setCustomerPoint(Integer customerPoint) {
        this.customerPoint = customerPoint;
    }




}
