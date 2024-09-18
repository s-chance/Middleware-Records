package org.entropy.restclient.mq;

import org.entropy.restclient.constant.MqConstant;
import org.entropy.restclient.service.HotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class HotelListener {

    private final HotelService hotelService;

    public HotelListener(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * 监听新增或修改的业务
     * @param id
     */
    @RabbitListener(queues = MqConstant.HOTEL_INSERT_QUEUE)
    public void listenHotelInsertOrUpdate(Long id) {
        hotelService.insertOrUpdateById(id);
    }

    /**
     * 监听删除的业务
     * @param id
     */
    @RabbitListener(queues = MqConstant.HOTEL_DELETE_QUEUE)
    public void listenHotelDelete(Long id) {
        hotelService.deleteById(id);
    }
}
