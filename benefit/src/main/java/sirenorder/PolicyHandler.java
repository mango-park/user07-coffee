package sirenorder;

import sirenorder.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PolicyHandler {
    @Autowired
    BenefitRepository benefitRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCompleted_EarnStamp(@Payload Completed completed) {

        System.out.println("\n\n##### listener EarnStamp : " + completed.toJson() + "\n\n");

        if (!completed.validate()) return;

        Optional<Benefit> benefitOptional = benefitRepository.findTop1ByCustomerId(completed.getCustomerId());

        if (benefitOptional.isPresent()) {
            Benefit benefit = benefitOptional.get();
            benefit.setStamp(benefit.getStamp() + 1L);
            benefitRepository.save(benefit);
        } else {
            // Sample Logic //
            Benefit benefit = new Benefit();
            benefit.setCustomerId(completed.getCustomerId());
            benefit.setStamp(1L);
            benefitRepository.save(benefit);
        }

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {
    }


}
