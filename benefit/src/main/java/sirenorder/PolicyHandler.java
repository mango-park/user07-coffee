package sirenorder;

import sirenorder.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired BenefitRepository benefitRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCompleted_EarnStamp(@Payload Completed completed){

        if(!completed.validate()) return;

        System.out.println("\n\n##### listener EarnStamp : " + completed.toJson() + "\n\n");

        // Sample Logic //
        Benefit benefit = new Benefit();
        benefitRepository.save(benefit);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
