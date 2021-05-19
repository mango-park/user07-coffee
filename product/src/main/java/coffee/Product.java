package coffee;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Product_table")
public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Integer price;
    private String productStatus;

    @PostPersist
    public void onPostPersist(){
        RegisteredProduct registeredProduct = new RegisteredProduct();
        BeanUtils.copyProperties(this, registeredProduct);
        registeredProduct.publishAfterCommit();


    }

    @PrePersist
    public void onPrePersist(){
        CheckedProductStatus checkedProductStatus = new CheckedProductStatus();
        BeanUtils.copyProperties(this, checkedProductStatus);
        checkedProductStatus.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }




}
