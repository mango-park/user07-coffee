package coffee;

import coffee.config.kafka.KafkaProcessor;

import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler {
// Edited Source
    @Autowired
    OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderWaited_UpdateStatus(@Payload OrderWaited orderWaited){
// Edited Source
        if(orderWaited.isMe()){
            System.out.println("##### listener UpdateStatus : " + orderWaited.toJson());
            System.out.println();
            System.out.println();

            Optional<Order> orderOptional =  orderRepository.findById(orderWaited.getOrderId());
            Order order = orderOptional.get();
            order.setStatus(orderWaited.getStatus());
  
            orderRepository.save(order);            
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCompleted_UpdateStatus(@Payload DeliveryCompleted deliveryCompleted){

        if(deliveryCompleted.isMe()){
            System.out.println("##### listener UpdateStatus : " + deliveryCompleted.toJson());
            System.out.println();
            System.out.println();
// Edited Source
            Optional<Order> orderOptional =  orderRepository.findById(deliveryCompleted.getOrderId());
            Order order = orderOptional.get();
            order.setStatus(deliveryCompleted.getStatus());

            orderRepository.save(order);  
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderReceived_UpdateStatus(@Payload OrderReceived orderReceived){

        if(orderReceived.isMe()){
            System.out.println("##### listener UpdateStatus : " + orderReceived.toJson());
            System.out.println();
            System.out.println();

// Edited Source
            Optional<Order> orderOptional =  orderRepository.findById(orderReceived.getOrderId());
            Order order = orderOptional.get();
            order.setStatus(orderReceived.getStatus());

            orderRepository.save(order);  

        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverStatusUpdated_UpdateStatus(@Payload StatusUpdated statusUpdated){

        if(statusUpdated.isMe()){
            System.out.println("##### listener UpdateStatus : " + statusUpdated.toJson());
            System.out.println();
            System.out.println();

// Edited Source
            Optional<Order> orderOptional =  orderRepository.findById(statusUpdated.getOrderId());
            Order order = orderOptional.get();
            order.setStatus(statusUpdated.getStatus());

            orderRepository.save(order);  

        }
    }    
}
