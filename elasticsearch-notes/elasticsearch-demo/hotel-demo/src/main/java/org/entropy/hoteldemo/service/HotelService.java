package org.entropy.hoteldemo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.hoteldemo.mapper.HotelMapper;
import org.entropy.hoteldemo.pojo.Hotel;
import org.springframework.stereotype.Service;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> {
}
