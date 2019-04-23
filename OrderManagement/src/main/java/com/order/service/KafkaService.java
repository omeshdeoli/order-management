package com.order.service;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.order.dto.ShipmentRequest;
import com.order.dto.ShipmentResponse;

@Service
public class KafkaService {

	@Autowired
	@Qualifier("shipmentRequestKafkaTemplate")
	private KafkaTemplate<String, ShipmentRequest> shipmentRequestKafkaTemplate;

	public ShipmentResponse sendShipmentRequest(Long orderId, Long customerId) {

		ShipmentResponse shipmentResponse = new ShipmentResponse();

		ListenableFuture<SendResult<String, ShipmentRequest>> a = shipmentRequestKafkaTemplate.send("shippedOrders",
				new ShipmentRequest(orderId, customerId));
		try {
			SendResult<String, ShipmentRequest> b = a.get();
			shipmentResponse.setMsgSent(true);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			shipmentResponse.setMsgSent(false);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			shipmentResponse.setMsgSent(false);
		}

		return shipmentResponse;
	}

}
