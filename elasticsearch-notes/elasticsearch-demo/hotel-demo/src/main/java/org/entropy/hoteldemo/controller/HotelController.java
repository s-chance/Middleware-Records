package org.entropy.hoteldemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.entropy.hoteldemo.constant.MqConstant;
import org.entropy.hoteldemo.pojo.Hotel;
import org.entropy.hoteldemo.service.HotelService;
import org.entropy.hoteldemo.vo.PageResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    private final HotelService hotelService;
    private final RabbitTemplate rabbitTemplate;

    public HotelController(HotelService hotelService, RabbitTemplate rabbitTemplate) {
        this.hotelService = hotelService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/{id}")
    public Hotel queryById(@PathVariable("id") Long id) {
        return hotelService.getById(id);
    }

    @GetMapping("/list")
    public PageResult hotelList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "1") Integer size
    ) {
        Page<Hotel> result = hotelService.page(new Page<>(page, size));
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @PostMapping
    public void saveHotel(@RequestBody Hotel hotel) {
        hotelService.save(hotel);
        rabbitTemplate.convertAndSend(MqConstant.HOTEL_EXCHANGE, MqConstant.HOTEL_INSERT_KEY, hotel.getId());
    }

    @PutMapping
    public void updateById(@RequestBody Hotel hotel) {
        if (hotel.getId() == null) {
            throw new InvalidParameterException("id can't be null");
        }
        hotelService.updateById(hotel);
        rabbitTemplate.convertAndSend(MqConstant.HOTEL_EXCHANGE, MqConstant.HOTEL_INSERT_KEY, hotel.getId());
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        hotelService.removeById(id);
        rabbitTemplate.convertAndSend(MqConstant.HOTEL_EXCHANGE, MqConstant.HOTEL_DELETE_KEY, id);
    }
}
