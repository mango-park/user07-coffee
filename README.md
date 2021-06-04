# 개인 프로젝트 : SirenOrder 업그레이드 (+혜택 및 상점)

![image](https://user-images.githubusercontent.com/74900977/118920002-81cb6b80-b970-11eb-8ca7-a5e62d96a77e.png)

SirenOrder 서비스를 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 프로젝트임

- 체크포인트 : https://workflowy.com/s/assessment/qJn45fBdVZn4atl3


# Table of contents

- [예제 - SirenOrder](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [분석/설계](#분석설계)
    - [Event Storming 결과](#Event-Storming-결과)
    - [완성된 1차 모형](#완성된-1차-모형)
    - [바운디드 컨텍스트](#바운디드-컨텍스트)
    - [기능적 요구사항 검증](#기능적-요구사항을-커버하는지-검증)
    - [비기능적 요구사항 검증](#비기능-요구사항에-대한-검증)
    - [헥사고날 아키텍처 다이어그램 도출](#헥사고날-아키텍처-다이어그램-도출)
       
  - [구현:](#구현)
    - [DDD 의 적용](#ddd-의-적용)   
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#동기식-호출-과-Fallback-처리)
    
  - [운영](#운영)
    - [CI/CD 설정](#CICD-설정)
    - [Kubernetes 설정](#Kubernetes-설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출/서킷-브레이킹/장애격리)
    - [오토스케일 아웃](#Autoscale-HPA)
    - [무정지 재배포](#Zero-downtime-deploy)
    - [셀프힐링](#livenessProbe) 
 

# 서비스 시나리오

[ 기능적 요구사항 ]
1. 고객이 회원 가입을 한다
2. 신규 회원 가입을 한 고객에게 포인트를 적립해 준다
3. 고객이 주문하기 전에 주문 가능한 상품 메뉴를 선택한다
4. 고객이 선택한 메뉴에 대해서 주문을 한다
5. 주문이 되면 주문 내역이 Delivery 서비스에 전달되고, 고객 포인트를 적립한다
6. 접수된 주문은 Wating 상태로 접수가 되고, 고객한테 접수 대기 번호를 발송한다
7. 주문한 상품이 완료되면 고객한테 상품 주문 완료를 전달한다   
8. 상점 주인에게 주문/매출 정보를 조회할수 있는 Report 서비스를 제공한다.
    

[ 비기능적 요구사항 ]
1. 트랜잭션
    1. 판매가 가능한 상품 정보만 주문 메뉴에 노출한다  Sync 호출 
1. 장애격리
    1. Delivery 서비스가 중단되더라도 주문은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
    1. 주문이 완료된 상품이 Delivery 서비스가 과중되더라도 주문 완료 정보를 Delivery 서비스가 정상화 된 이후에 수신한다 Circuit breaker, fallback
1. 성능
    1. 상점 주인은 Report 서비스를 통해서 주문/매출 정보를 확인할 수 있어야 한다  CQRS
    1. 주문 접수 상태가 바뀔때마다 고객에게 알림을 줄 수 있어야 한다  Event driven

## 신규 요구사항 접수
[ 기능적 요구사항 ]
1. 고객은 스탬프를 통해 상품을 구매할 수 있다.
2. 주문이 완료되면 고객에게 스탬프를 부여한다.
3. 상점 주인은 직접 고객의 스탬프 적립 가능하다.
4. 상점 주인은 새로운 상점을 차릴 수 있다.

[ 비기능적 요구사항 ]
1. 트랜잭션
    1. 스탬프가 사용 가능할 때마 스탬프를 통한 상품 구매가 가능하다. Sync 호출
1. 장애격리
    1. Store 서비스가 중단되더라도 주문은 365일 24시간 받을 수 있어야 한다. Async (event-driven), Eventual Consistency
    2. 주문 완료시 혜택(benefit) 서비스가 과중되더라도 서비스 정상화 후, 스탬프 적립 처리를 한다. Circuit breaker, fallback
1. 성능
    1. 상점 주인은 Store 서비스를 통해서도 매출 합계 정보 확인이 가능하다. CQRS

# 분석/설계

## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/vMFi38LnlbRPvs4teOm6f35Ufm42/mine/724da5741331e4aaec07896247b703be

### 이벤트 도출
![image](https://user-images.githubusercontent.com/74900977/118924080-8ba49d00-b977-11eb-82f2-4db4f4be71fa.png)

### 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/74900977/118924474-1ab1b500-b978-11eb-8dd3-9fcd7a003a13.png)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - 주문시>메뉴카테고리선택됨, 주문시>메뉴검색됨, 주문후>포인트 조회함, 주문후>주문 상태값 조회됨
          :  UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외

### 바운디드 컨텍스트

![image](https://user-images.githubusercontent.com/74900977/118925812-4df54380-b97a-11eb-9591-a924fe52e9e0.png)

    - 도메인 서열 분리 
        - Core Domain:  Customer, Order, Product, Delivery : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기 : 1주일 1회 미만, Delivery 1개월 1회 미만
        - Supporting Domain: Report : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기 : 1주일 1회 이상을 기준 ( 각팀 배포 주기 Policy 적용 )

### 완성된 1차 모형

![image](https://user-images.githubusercontent.com/74900977/118931820-581b4000-b982-11eb-963a-a47b5f014844.png)


### 기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/74900977/118940019-425e4880-b98b-11eb-85ce-16375ba40f1e.png)

    - 고객이 회원 가입을 한다 (ok)
    - 신규 회원 가입을 한 고객에게 포인트를 적립해 준다(OK)
    - 고객이 주문하기 전에 주문 가능한 상품 메뉴를 선택한다 (ok)
    - 고객이 선택한 메뉴에 대해서 주문을 한다 (ok)
    - 주문이 되면 주문 내역이 Delivery 서비스에 전달되고, 고객 포인트를 적립한다 (ok)
    - 접수된 주문은 Wating 상태로 접수가 되고, 고객한테 접수 대기 번호를 발송한다 ( ok )
    - 주문한 상품이 완료되면 고객한테 상품 주문 완료를 전달한다 ( OK )
    - 상점 주인에게 주문/매출 정보를 조회할수 있는 Report 서비스를 제공한다 ( OK )


### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/74900977/118941404-a6cdd780-b98c-11eb-9d26-a17a83a5c9ee.png)


    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
    - 판매 가능 상품 :  판매가 가능한 상품만 주문 메뉴에 노출됨 , ACID 트랜잭션, Request-Response 방식 처리
    - 주문 완료시 상품 접수 및 Delivery:  Order 서비스에서 Delivery 마이크로서비스로 주문요청이 전달되는 과정에 있어서 Delivery 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
    - Product, Customer, Report MicroService 트랜잭션:  주문 접수 상태, 상품 준비 상태 등 모든 이벤트에 대해 Kafka를 통한 Async 방식 처리, 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.


## 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/74900977/118951124-c9182300-b995-11eb-8b4d-9107d3dcf501.png)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


## 신규 서비스 구성

![image](https://user-images.githubusercontent.com/20352446/120730894-730eb800-c51d-11eb-9ae2-5524ae515e86.png)

헥사고날 아키텍처 다이어그램의 경우, delivery와 product의 기능을 갖는 benefit과 delivery 구성과 유사한 store 다이어그램이 추가되었다.

### 기능적 요구사항에 대한 검증
    - 고객은 스탬프를 통해 상품을 구매할 수 있다 (ok)
    - 주문이 완료되면 고객에게 스탬프를 부여 (ok)
    - 상점 주인이 직접 고객의 스탬프를 적립할 수 있다 (ok)
### 비기능 요구사항에 대한 검증
    - 스탬프 서비스 미운영시 스탬프를 통한 상품 구매는 불가하다 (ok)    
    - Store 서비스는 주문 서비스에 영향을 주어서는 안된다.(ok)
    - 혜택 서비스 장애 발생시 조치가 완료되면 고객에게 정상적으로 스탬프를 자동 지급한다. (ok)



# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 docker화 한 후, k8s에 deploy하였다.

![image](https://user-images.githubusercontent.com/20352446/120641576-b8e06780-c4ae-11eb-870f-701360ff32e6.png)
![image](https://user-images.githubusercontent.com/20352446/120641750-f5ac5e80-c4ae-11eb-9564-0823f041c75e.png)

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다.
```
package coffee;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

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
```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
@RepositoryRestResource(collectionResourceRel = "benefits", path = "benefits")
public interface BenefitRepository extends PagingAndSortingRepository<Benefit, Long> {

    Optional<Benefit> findTop1ByCustomerId(Long customerId);
}
```
- 적용 후 REST API 의 테스트
```
# 주문 처리
curl --location --request GET 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/orders/order?customerId=100&productId=1&benefitUseYn=N'

# 배달 완료 처리
curl --location --request POST 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/deliveries' \
--header 'Content-Type: application/json' \
--data-raw '{
    "orderId": 2,
    "status": "Completed"
}'

# 주문 상태 확인
curl --location --request GET 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/orders'
```

## 동기식 호출 과 Fallback 처리

분석단계 조건 중 하나로 주문(order)시 스탬프 사용 여부를 결정하게 되고, 혜택(benefit)간의 호출은 동기식 API 호출로 처리했다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 혜택 서비스를 호출하기 위하여 Stub과 (FeignClient)를 이용하여 Service 대행 인터페이스 (Proxy)를 구현 

```
@FeignClient(name = "benefit", url = "${feign.client.url.benefitUrl}")
public interface BenefitService {

    @RequestMapping(method = RequestMethod.GET, path = "/benefits/checkAndUsed")
    public boolean checkAndUsed(@RequestParam("customerId") Long customerId);
}
```

- 스탬프 사용시 즉시 고객의 스탬프를 차감하도록 구현
```
    @RequestMapping(value = "/benefits/checkAndUsed", method = RequestMethod.GET)
    public boolean checkAndUsed(@RequestParam("customerId") Long customerId) throws Exception {
        System.out.println("##### /benefit/checkAndUsed  called #####");
        // Edited Source
        boolean result = false;

        Optional<Benefit> benefitOptional = benefitRepository.findTop1ByCustomerId(customerId);

        if (benefitOptional.isPresent()) {
            Benefit benefit = benefitOptional.get();
            if (benefit.getStamp() >= Long.parseLong(stampDeductionCount)) {
                result = true;
                benefit.setStamp(benefit.getStamp() - Long.parseLong(stampDeductionCount));
                benefitRepository.save(benefit);
            }
        }

        return result;
    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 혜택(benefit) microservice 장애시면 주문(order)도 불가함을 확인:






```
#혜택(benefit) microservice의 replicas 0 설정
```
![image](https://user-images.githubusercontent.com/20352446/120643002-8cc5e600-c4b0-11eb-82c8-d48986c077c1.png)
```
#주문처리 
curl --location --request GET 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/orders/order?customerId=100&productId=1&benefitUseYn=Y'

실패 및 응답 결과
{
    "timestamp": "2021-06-03T12:15:13.493+0000",
    "status": 500,
    "error": "Internal Server Error",
    "message": "Connection refused (Connection refused) executing GET http://benefit:8080/benefits/checkAndUsed?customerId=100",
    "path": "/orders/order"
}

#혜택(benefit) microservice의 replicas 원복

#주문처리
curl --location --request GET 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/orders/order?customerId=100&productId=1&benefitUseYn=Y'

성공 200 OK
```



## 비동기식 호출 publish-subscribe

배송이 완료되어 최종 주문(order)이 완료된 후, 상점(store)에 이를 알려주는 행위는 비동기식으로 처리한다.
- 이를 위하여 주문(order)이 최종 완료된 후 바로 주문 완료 되었다는 도메인 이벤트를 kafka로 송출한다(Publish)
 
```
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
```
- 상점(store) microservice에서는 주문(order) 완료 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCompleted_EarnMoney(@Payload Completed completed){

        System.out.println("\n\n##### listener EarnMoney : " + completed.toJson() + "\n\n");

        if (!completed.validate()) return;

        Optional<Store> storeOptional = storeRepository.findById(1L);

        if (storeOptional.isPresent()) {
            Store store = storeOptional.get();
            //TODO 상품ID로 상품 가격을 조회
            store.setMoney(store.getMoney() + 5000L);
            storeRepository.save(store);
        }
    }
```

상점(store) microservice는 주문(order) microservice와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 상점(store) 서비스가 유지보수로 인해 잠시 내려간 상태라도 주문 처리에 문제 없다:
```
#상점(store) microservice의 replicas 0 설정
```
![image](https://user-images.githubusercontent.com/20352446/120644757-a5cf9680-c4b2-11eb-8eae-b65c4e66dd73.png)
```
#주문처리
curl --location --request GET 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/orders/order?customerId=100&productId=1&benefitUseYn=Y'

성공 200 OK


#주문상태 확인
curl --location --request GET 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/stores'     

# 금액 0원 확인
"stores": [
    {
        "money": 0,
        "_links": {
            "self": {
                "href": "http://store:8080/stores/100"
            },
            "store": {
                "href": "http://store:8080/stores/100"
            }
        }
    }
]

#상점(store) microservice의 replicas 원복

#주문상태 확인
curl --location --request GET 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/stores'     

# 금액 5000원 증가 확인
"stores": [
    {
        "money": 5000,
        "_links": {
            "self": {
                "href": "http://store:8080/stores/100"
            },
            "store": {
                "href": "http://store:8080/stores/100"
            }
        }
    }
]

```


# 운영

## CICD 설정

사용한 CI/CD 도구는 AWS CodeBuild
![image](https://user-images.githubusercontent.com/20352446/120729917-4d80af00-c51b-11eb-84c1-0d04b833f4a2.png)
GitHub Webhook이 동작하여 Docker image가 자동 생성 및 ECR 업로드 된다.
(pipeline build script 는 benefit 폴더 이하에 buildspec.yaml 에 포함)
![image](https://user-images.githubusercontent.com/20352446/120729843-232ef180-c51b-11eb-9102-6c257eeefb13.png)
참고로 작업의 편의를 위해 대표로 하나의 git repository를 사용하였다.


## Kubernetes 설정
AWS EKS를 활용했으며, 추가한 namespace는 user07-coffee와 kafka로 아래와 같다.

###EKS Deployment

namespace: user07-coffee
![image](https://user-images.githubusercontent.com/20352446/120649987-4f655680-c4b8-11eb-8b1f-37c6e3cdc8b6.png)

namespace: kafka
![image](https://user-images.githubusercontent.com/20352446/120650086-65731700-c4b8-11eb-9bd7-8a4dd68262fa.png)


###EKS Service
gateway가 아래와 같이 LoadBalnacer 역할을 수행한다  
![image](https://user-images.githubusercontent.com/20352446/120650224-889dc680-c4b8-11eb-8ea7-6ceff6ed57cf.png)



## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 주문(order)-->혜택(benefit) 연결을 RestFul Request/Response 로 연동하여 구현하고, 주문이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml
hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```
- 혜택(benefit) 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
```
@RequestMapping(value = "/benefits/checkAndUsed", method = RequestMethod.GET)
public boolean checkAndUsed(@RequestParam("customerId") Long customerId) throws Exception {

    ...생략...

    //임의의 부하를 위한 강제 설정
    try {
        Thread.currentThread().sleep((long) (400 + Math.random() * 220));
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    return result;
}
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
siege -c100 -t60S -r10 --content-type "application/json" 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/orders/order?customerId=1000&productId=1&benefitUseYn=Y'

HTTP/1.1 200     5.73 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     5.73 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     5.82 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     5.93 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     6.02 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     6.06 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     6.06 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     4.84 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     6.14 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 200     5.03 secs:       0 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y

HTTP/1.1 500     0.18 secs:     177 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 500     0.18 secs:     177 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 500     0.26 secs:     177 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 500     0.19 secs:     177 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 500     0.18 secs:     177 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 500     0.17 secs:     177 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y
HTTP/1.1 500     0.13 secs:     177 bytes ==> GET  /orders/order?customerId=1000&productId=1&benefitUseYn=Y

Transactions:		         447 hits
Availability:		       28.47 %
Elapsed time:		       54.49 secs
Data transferred:	        0.19 MB
Response time:		       11.18 secs
Transaction rate:	        8.20 trans/sec
Throughput:		        0.00 MB/sec
Concurrency:		       91.68
Successful transactions:         447
Failed transactions:	        1123
Longest transaction:	       30.28
Shortest transaction:	        0.09

```
- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호. 
  시스템의 안정적인 운영을 위해 HPA 적용 필요.



### Autoscale HPA

- 혜택(benefit)서비스에 대해 HPA를 설정한다. 설정은 CPU 사용량이 10%를 넘어서면 pod를 최대 4개까지 생성한다.
```
➜  ~ kubectl autoscale deployment benefit -n user07-coffee --cpu-percent=10 --min=1 --max=4
horizontalpodautoscaler.autoscaling/benefit autoscaled

➜  ~ kubectl get hpa -n user07-coffee
NAME      REFERENCE            TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
benefit   Deployment/benefit   2%/10%    1         4         1          16s
```
- 부하를 1분간 유지한다.
```
➜  ~ siege -c50 -t60S -r10 --content-type "application/json" 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/orders/order?customerId=1000&productId=1&benefitUseYn=Y'
```
- 오토스케일이 어떻게 되고 있는지 확인한다.
```
➜  ~ kubectl get deploy -n user07-coffee
NAME       READY   UP-TO-DATE   AVAILABLE   AGE
benefit    1/1     1            1           5m57s
customer   1/1     1            1           21h
delivery   1/1     1            1           21h
gateway    1/1     1            1           21h
order      1/1     1            1           21h
product    1/1     1            1           21h
report     1/1     1            1           21h
store      1/1     1            1           15h
```
- 어느정도 시간이 흐르면 스케일 아웃이 동작하는 것을 확인
```
➜  ~ kubectl get deploy -n user07-coffee
NAME       READY   UP-TO-DATE   AVAILABLE   AGE
benefit    1/4     4            1           7m8s
customer   1/1     1            1           21h
delivery   1/1     1            1           21h
gateway    1/1     1            1           21h
order      1/1     1            1           21h
product    1/1     1            1           21h
report     1/1     1            1           21h
store      1/1     1            1           16h
```


##ConfigMap 설정
특정값을 k8s 설정으로 올리고 서비스를 기동 후, kafka 정상 접근 여부 확인한다.
```
➜  ~ kubectl describe cm benefit-config -n user07-coffee
Name:         benefit-config
Namespace:    user07-coffee
Labels:       <none>
Annotations:  <none>

Data
====
STAMP_DEDUCTION_COUNT:
----
2
Events:  <none>
```
관련된 혜택(benefit) application.yml 파일 및 활용 Class는 아래와 같다. 
```
test:
  configMap:
    stampDeductionCount: ${STAMP_DEDUCTION_COUNT}
```

```
@Value("${test.configMap.stampDeductionCount}")
public String stampDeductionCount;

    if (benefitOptional.isPresent()) {
        Benefit benefit = benefitOptional.get();
        if (benefit.getStamp() >= Long.parseLong(stampDeductionCount)) {
            result = true;
            benefit.setStamp(benefit.getStamp() - Long.parseLong(stampDeductionCount));
            benefitRepository.save(benefit);
        }
    }
```
ConfigMap에 세팅된 바와 같이 스탬프 사용 order(주문)시 스탬프 2개가 차감되는 것을 확인 가능하다.



## Zero-downtime deploy(Readiness)
k8s의 무중단 서비스 배포 기능을 점검한다.
```
➜  ~ kubectl describe deploy store -n user07-coffee
Name:                   store
Namespace:              user07-coffee
CreationTimestamp:      Thu, 03 Jun 2021 17:33:58 +0900
Labels:                 app=store
Annotations:            deployment.kubernetes.io/revision: 3
Selector:               app=store
Replicas:               1 desired | 1 updated | 1 total | 1 available | 0 unavailable
StrategyType:           RollingUpdate
MinReadySeconds:        0
RollingUpdateStrategy:  25% max unavailable, 25% max surge
Pod Template:
  Labels:  app=store
  Containers:
   store:
    Image:        879772956301.dkr.ecr.ap-northeast-1.amazonaws.com/user07-store:v3
    Port:         8080/TCP
    Host Port:    0/TCP
    Liveness:     http-get http://:8080/actuator/health delay=120s timeout=2s period=5s #success=1 #failure=5
    Readiness:    http-get http://:8080/actuator/health delay=10s timeout=2s period=5s #success=1 #failure=10
    Environment:  <none>
    Mounts:       <none>
  Volumes:        <none>
Conditions:
  Type           Status  Reason
  ----           ------  ------
  Progressing    True    NewReplicaSetAvailable
  Available      True    MinimumReplicasAvailable
OldReplicaSets:  <none>
NewReplicaSet:   store-74d8bd6d5f (1/1 replicas created)
Events:          <none>
```
기능 점검을 위해 store Deployment의 replicas를 4로 수정했다. 
그리고 위 Readiness와 RollingUpdateStrategy 설정이 정상 적용되는지 확인한다.
```
➜  ~ kubectl rollout status deploy/store -n user07-coffee
Waiting for deployment "store" rollout to finish: 2 of 4 updated replicas are available...
Waiting for deployment "store" rollout to finish: 3 of 4 updated replicas are available...
deployment "store" successfully rolled out

➜  ~ kubectl get po -n user07-coffee
NAME                        READY   STATUS              RESTARTS   AGE
benefit-69469cc7b6-6wrfs    1/1     Terminating         0          60s
benefit-69469cc7b6-9p4tk    1/1     Running             0          60s
benefit-69469cc7b6-jw5tk    1/1     Running             2          76m
benefit-69469cc7b6-n6z45    1/1     Running             0          60s
benefit-85d474c889-jfd4r    0/1     ContainerCreating   0          1s
benefit-85d474c889-tlsw4    0/1     ContainerCreating   0          1s
customer-675894fd5c-q464h   1/1     Running             0          86m
delivery-564b449ffb-4mgt7   1/1     Running             0          11h
gateway-85858bd5f6-fhlnf    1/1     Running             0          5h34m
order-7b95565555-4qs7z      1/1     Running             0          7h47m
product-5f69494d84-49qrp    1/1     Running             0          7h45m
report-7958759448-dlwfc     1/1     Running             0          11h
store-74d8bd6d5f-x5hvn      1/1     Running             0          71s
    
    
➜  ~ kubectl get po -n user07-coffee
NAME                        READY   STATUS    RESTARTS   AGE
benefit-85d474c889-4ztpb    1/1     Running   0          47s
benefit-85d474c889-5hbtm    1/1     Running   0          46s
benefit-85d474c889-jfd4r    1/1     Running   0          70s
benefit-85d474c889-tlsw4    1/1     Running   0          70s
customer-675894fd5c-q464h   1/1     Running   0          87m
delivery-564b449ffb-4mgt7   1/1     Running   0          11h
gateway-85858bd5f6-fhlnf    1/1     Running   0          5h36m
order-7b95565555-4qs7z      1/1     Running   0          7h48m
product-5f69494d84-49qrp    1/1     Running   0          7h47m
report-7958759448-dlwfc     1/1     Running   0          11h
store-74d8bd6d5f-x5hvn      1/1     Running   0          2m20s
```
배포시 pod는 위의 흐름과 같이 생성 및 종료되어 서비스의 무중단을 보장하며 모두 benefit-69469cc7b6에서 benefit-85d474c889로 넘어갔다.


## 셀프힐링 (livenessProbe 설정)
- benefit deployment livenessProbe
```
      livenessProbe:
        httpGet:
          path: '/actuator/health'
          port: 8080
        initialDelaySeconds: 120
        timeoutSeconds: 2
        periodSeconds: 5
        failureThreshold: 5
```
livenessProbe 기능 점검을 위해 HPA 제거한다.
```
➜  ~ kubectl get hpa -n user07-coffee
No resources found in coffee namespace.
```
Pod 의 변화를 살펴보기 위하여 watch
```
➜  ~ kubectl get -n user07-coffee po -w
NAME                        READY   STATUS    RESTARTS   AGE
benefit-85d474c889-jfd4r    1/1     Running   0          10h
customer-675894fd5c-q464h   1/1     Running   0          11h
delivery-564b449ffb-4mgt7   1/1     Running   0          21h
gateway-85858bd5f6-fhlnf    1/1     Running   0          15h
order-7b95565555-4qs7z      1/1     Running   0          17h
product-5f69494d84-49qrp    1/1     Running   0          17h
report-7958759448-dlwfc     1/1     Running   0          21h
store-74d8bd6d5f-x5hvn      1/1     Running   0          10h
```
benefit 서비스를 다운시키기 위한 부하 발생
```
➜  ~ siege -c100 -t60S -r10 --content-type "application/json" 'http://a4e9a4ceacc174813be5e3805fb26a68-396411177.ap-northeast-1.elb.amazonaws.com:8080/orders/order?customerId=1000&productId=1&benefitUseYn=Y'
```
benefit Pod의 liveness 조건에 의한 RESTARTS 횟수 증가 확인
```
➜  ~ kubectl get -n user07-coffee po -w
NAME                        READY   STATUS    RESTARTS   AGE
benefit-85d474c889-jfd4r    1/1     Running   1          10h
customer-675894fd5c-q464h   1/1     Running   0          11h
delivery-564b449ffb-4mgt7   1/1     Running   0          21h
gateway-85858bd5f6-fhlnf    1/1     Running   0          15h
order-7b95565555-4qs7z      1/1     Running   0          17h
product-5f69494d84-49qrp    1/1     Running   0          17h
report-7958759448-dlwfc     1/1     Running   0          21h
store-74d8bd6d5f-x5hvn      1/1     Running   0          10h
```