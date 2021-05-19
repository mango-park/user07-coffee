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
public class PolicyHandler{

    @Autowired
    DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_WaitOrder(@Payload Ordered ordered){

        if(ordered.isMe()){
            System.out.println("##### listener WaitOrder : " + ordered.toJson());
            System.out.println();
            System.out.println();

//Edited Source
            // Optional<Delivery> deliveryCompleted = deliveryRepository.findByStatus("Completed");   
            // Long total_count = deliveryRepository.count();
//          Long completed_count = deliveryCompleted.
            // System.out.println(total_count);
            // System.out.println(deliveryCompleted);

  

            Delivery delivery = new Delivery();
            delivery.setOrderId(ordered.getId());
            delivery.setStatus("Waited");

            deliveryRepository.save(delivery);     

        }
    }

}
