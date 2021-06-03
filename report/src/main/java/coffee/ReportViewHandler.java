package coffee;

import coffee.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ReportViewHandler {


    @Autowired
    private ReportRepository reportRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_CREATE_1(@Payload Ordered ordered) {
        try {

            if (ordered.isMe()) {
                // view 객체 생성
                Report report = new Report();
                // view 객체에 이벤트의 Value 를 set 함
                report.setOrderId(ordered.getId());
                report.setCustomerId(ordered.getCustomerId());
                report.setProductId(ordered.getProductId());
                // view 레파지 토리에 save
                reportRepository.save(report);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderWaited_then_UPDATE_1(@Payload OrderWaited orderWaited) {
        try {
            if (orderWaited.isMe()) {
                // view 객체 조회
                List<Report> reportList = reportRepository.findByOrderId(orderWaited.getOrderId());
                for (Report report : reportList) {
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    report.setDeliveryId(orderWaited.getId());
                    report.setStatus(orderWaited.getStatus());
                    // view 레파지 토리에 save
                    reportRepository.save(report);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderReceived_then_UPDATE_2(@Payload OrderReceived orderReceived) {
        try {
            if (orderReceived.isMe()) {
                // view 객체 조회
                List<Report> reportList = reportRepository.findByOrderId(orderReceived.getOrderId());
                for (Report report : reportList) {
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    report.setStatus(orderReceived.getStatus());
                    // view 레파지 토리에 save
                    reportRepository.save(report);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryCompleted_then_UPDATE_3(@Payload DeliveryCompleted deliveryCompleted) {
        try {
            if (deliveryCompleted.isMe()) {
                // view 객체 조회
                List<Report> reportList = reportRepository.findByOrderId(deliveryCompleted.getOrderId());
                for (Report report : reportList) {
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    report.setStatus(deliveryCompleted.getStatus());
                    // view 레파지 토리에 save
                    reportRepository.save(report);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenStatusUpdated_then_UPDATE_4(@Payload StatusUpdated statusUpdated) {
        try {
            if (statusUpdated.isMe()) {
                // view 객체 조회
                List<Report> reportList = reportRepository.findByOrderId(statusUpdated.getOrderId());
                for (Report report : reportList) {
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    report.setDeliveryId(statusUpdated.getId());
                    report.setStatus(statusUpdated.getStatus());
                    // view 레파지 토리에 save
                    reportRepository.save(report);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}