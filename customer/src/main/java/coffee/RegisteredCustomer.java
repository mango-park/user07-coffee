package coffee;

public class RegisteredCustomer extends AbstractEvent {

    private Long id;
    private Integer customerPoint;

    public RegisteredCustomer(){
        super();
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
