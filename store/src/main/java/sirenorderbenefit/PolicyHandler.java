package sirenorderbenefit;

import sirenorderbenefit.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PolicyHandler{
    @Autowired StoreRepository storeRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCompleted_EarnMoney(@Payload Completed completed){

        System.out.println("\n\n##### listener EarnMoney : " + completed.toJson() + "\n\n");

        if (!completed.validate()) return;

        Optional<Store> storeOptional = storeRepository.findById(100L);

        if (storeOptional.isPresent()) {
            Store store = storeOptional.get();
            //TODO 상품ID로 상품 가격을 조회
            store.setMoney(store.getMoney() + 5000L);
            storeRepository.save(store);
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
