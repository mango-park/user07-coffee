package sirenorder;

public class UsedBenefit extends AbstractEvent {

    private Long id;

    public UsedBenefit() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
