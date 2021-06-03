package sirenorderbenefit;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Store_table")
public class Store {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long money;

    @PostPersist
    public void onPostPersist(){
        ClosedStore closedStore = new ClosedStore();
        BeanUtils.copyProperties(this, closedStore);
        closedStore.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getMoney() {
        return money;
    }

    public void setMoney(Long money) {
        this.money = money;
    }




}
