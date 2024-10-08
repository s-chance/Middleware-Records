package org.entropy.restclient.constant;

public class HotelConstant {
    public static final String MAPPING_TEMPLATE = """
            {
              "mappings": {
                "properties": {
                  "id": {
                    "type": "keyword"
                  },
                  "name": {
                    "type": "text",
                    "analyzer": "ik_max_word",
                    "copy_to": "all"
                  },
                  "address": {
                    "type": "keyword",
                    "index": false
                  },
                  "price": {
                    "type": "integer"
                  },
                  "score": {
                    "type": "integer"
                  },
                  "brand": {
                    "type": "keyword",
                    "copy_to": "all"
                  },
                  "city": {
                    "type": "keyword",
                    "copy_to": "all"
                  },
                  "starName": {
                    "type": "keyword",
                    "copy_to": "all"
                  },
                  "business": {
                    "type": "keyword",
                    "copy_to": "all"
                  },
                  "location": {
                    "type": "geo_point"
                  },
                  "pic": {
                    "type": "keyword",
                    "index": false
                  },
                  "all": {
                    "type": "text",
                    "analyzer": "ik_max_word"
                  }
                }
              }
            }
            """;
}
