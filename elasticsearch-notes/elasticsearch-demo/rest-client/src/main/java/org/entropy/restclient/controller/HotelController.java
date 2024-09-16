package org.entropy.restclient.controller;

import org.entropy.restclient.dto.RequestParams;
import org.entropy.restclient.service.HotelService;
import org.entropy.restclient.vo.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/list")
    public PageResult search(@RequestBody RequestParams params) {
        return hotelService.search(params);
    }

    @GetMapping("/agg")
    public Map<String, List<String>> filters(@RequestBody RequestParams params) {
        return hotelService.filters(params);
    }

    @GetMapping("/suggestion")
    public List<String> suggest(@RequestParam("key") String prefix) {
        return hotelService.suggest(prefix);
    }
}
