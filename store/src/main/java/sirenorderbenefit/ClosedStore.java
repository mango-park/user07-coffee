package sirenorderbenefit;

public class ClosedStore extends AbstractEvent {

    private Long id;

    public ClosedStore(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
