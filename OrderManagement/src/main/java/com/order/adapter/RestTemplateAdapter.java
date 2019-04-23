package com.order.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.order.exception.ServiceUnavailableException;

@Component
public class RestTemplateAdapter {

	@Bean
	public RestTemplate rest(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Autowired
	public RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "inventoryFallback", commandKey = "inventoryHystrixCommand", ignoreExceptions = {
			HttpClientErrorException.class })
	public <T, R> ResponseEntity<T> callInventoryManagementService(R requestBody, String url, HttpMethod type,
			Class<T> clazz) {
		HttpEntity<R> request = new HttpEntity<>(requestBody);
		ResponseEntity<T> response = restTemplate.exchange(url, type, request, clazz);
		return response;
	}

	public <T, R> ResponseEntity<T> inventoryFallback(R requestBody, String url, HttpMethod type, Class<T> clazz) {
		ResponseEntity res = new ResponseEntity(new ServiceUnavailableException("Service unavaialable"),
				HttpStatus.SERVICE_UNAVAILABLE);
		return res;

	}
}
