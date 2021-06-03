package sirenorder;

import javax.persistence.*;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Date;

@Entity
@Table(name = "Benefit_table")
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long stamp;
    private Long customerId;

    @PostPersist
    public void onPostPersist() {
        UsedBenefit usedBenefit = new UsedBenefit();
        BeanUtils.copyProperties(this, usedBenefit);
        usedBenefit.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStamp() {
        return stamp;
    }

    public void setStamp(Long stamp) {
        this.stamp = stamp;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
