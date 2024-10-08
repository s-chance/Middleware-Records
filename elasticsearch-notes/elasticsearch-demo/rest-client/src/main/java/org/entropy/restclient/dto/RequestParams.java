package org.entropy.restclient.dto;

import lombok.Data;

@Data
public class RequestParams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String brand;
    private String starName;
    private String city;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;
}
