package org.zxc.service.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling
public class RestConfig {
    @Bean
    public RestTemplate restTemplate() {
    	SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    	requestFactory.setConnectTimeout(1500);
        return new RestTemplate(requestFactory);
    }
}
