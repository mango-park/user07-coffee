# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd customer
mvn spring-boot:run

cd order
mvn spring-boot:run 

cd product
mvn spring-boot:run  

cd delivery
mvn spring-boot:run  
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시 : order)
```
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

        Integer price = OrderApplication.applicationContext.getBean(coffee.external.ProductService.class)
                .checkProductStatus(this.getProductId());

        if (price > 0) {
            boolean result = OrderApplication.applicationContext.getBean(coffee.external.CustomerService.class)
                    .checkAndModifyPoint(this.getCustomerId(), price);

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

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package coffee;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
    public int countByStatus(String status);
}
```
- 적용 후 REST API 의 테스트
```
# 주문 처리
http POST http://localhost:8082/orders customerId=100 productId=100

# 배달 완료 처리
http PATCH http://localhost:8084/deliveries/1 status=Completed

# 주문 상태 확인
http GET http://localhost:8082/orders/1

```


## 폴리글랏 퍼시스턴스

//TODO 폴리그랏 작업 후 추가 영역
```
//소스
```

## 폴리글랏 프로그래밍

//TODO 폴리그랏 작업 후 추가 영역
```
//소스
```


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 주문(order)->고객(customer) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 고객 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
package coffee.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "customer", url = "${feign.client.url.customerUrl}")
public interface CustomerService {

    @RequestMapping(method = RequestMethod.GET, path = "/customers/checkAndModifyPoint")
    public boolean checkAndModifyPoint(@RequestParam("customerId") Long customerId,
            @RequestParam("price") Integer price);

}
```

- 주문 받은 즉시 고객 포인트를 차감하도록 구현
```
@RequestMapping(value = "/checkAndModifyPoint", method = RequestMethod.GET)
  public boolean checkAndModifyPoint(@RequestParam("customerId") Long customerId, @RequestParam("price") Integer price) throws Exception {
          System.out.println("##### /customer/checkAndModifyPoint  called #####");

          boolean result = false;

          Optional<Customer> customerOptional = customerRepository.findById(customerId);
          Customer customer = customerOptional.get();
          if (customer.getCustomerPoint() >= price) {
                  result = true;
                  customer.setCustomerPoint(customer.getCustomerPoint() - price);
                  customerRepository.save(customer);
          }

          return result;
  }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 고객 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 고객 (customer) 서비스를 잠시 내려놓음 (ctrl+c)

#주문처리 
http POST http://localhost:8082/orders customerId=100 productId=100   #Fail
http POST http://localhost:8082/orders customerId=101 productId=101   #Fail

#고객서비스 재기동
cd 결제
mvn spring-boot:run

#주문처리
http POST http://localhost:8082/orders customerId=100 productId=100   #Success
http POST http://localhost:8082/orders customerId=101 productId=101   #Success
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 (publish-subscribe)

배송이 완료된 후, 주문 시스템에게 이를 알려주는 행위는 동기식이 아닌 비동기식으로 처리한다.
- 이를 위하여 배송 상태가 Completed 된 후에 곧바로 배송완료 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package coffee;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name = "Delivery_table")
public class Delivery {

 ...
    @PostUpdate
    public void onPostUpdate() {
        StatusUpdated statusUpdated = new StatusUpdated();
        BeanUtils.copyProperties(this, statusUpdated);
        statusUpdated.publishAfterCommit();
    }

}
```
- 주문 서비스에서는 배송상태 업데이트 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package coffee;
...

@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverStatusUpdated_UpdateStatus(@Payload StatusUpdated statusUpdated) {

        if (statusUpdated.isMe()) {
            System.out.println("##### listener UpdateStatus : " + statusUpdated.toJson());

            Optional<Order> orderOptional = orderRepository.findById(statusUpdated.getOrderId());
            Order order = orderOptional.get();
            order.setStatus(statusUpdated.getStatus());

            orderRepository.save(order);
        }
    }

}

```

배송 시스템은 주문 시스템과 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 배송시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
# 배송 서비스 (delivery) 를 잠시 내려놓음 (ctrl+c)

#주문처리
http POST http://localhost:8082/orders customerId=100 productId=100   #Success
http POST http://localhost:8082/orders customerId=101 productId=101   #Success

#주문상태 확인
http POST http://localhost:8082/orders     # 주문상태 Ordered 확인

#배송 서비스 기동
cd delivery
mvn spring-boot:run

#주문상태 확인
http localhost:8082/orders     # 주문 상태 Waited로 변경 확인
```
