package org.entropy.restclient.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.restclient.mapper.HotelMapper;
import org.entropy.restclient.pojo.Hotel;
import org.springframework.stereotype.Service;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> {
}
