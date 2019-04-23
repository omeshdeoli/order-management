package com.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.order.dto.InvoiceResponse;
import com.order.dto.OrderCaptureRequest;
import com.order.dto.OrderCaptureResponse;
import com.order.dto.ShipmentRequest;
import com.order.dto.ShipmentResponse;
import com.order.service.KafkaService;
import com.order.service.OrderService;

@RestController
@RequestMapping(value = "/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	public KafkaService kafkaService;

	@PostMapping("/capture")
	private OrderCaptureResponse captureOrder(@RequestBody OrderCaptureRequest orderCaptureRequest) {

		return orderService.captureOrder(orderCaptureRequest);
	}

	@GetMapping("/invoice/{orderId}")
	private InvoiceResponse generateInvoice(@PathVariable("orderId") String orderId) {

		return orderService.generateInvoice(orderId);
	}

	@PostMapping("/publish/shipReq")
	private ShipmentResponse pubShipReq(@RequestBody ShipmentRequest shipReq) {

		return kafkaService.sendShipmentRequest(shipReq.getOrderId(), shipReq.getCustomerId());
	}
}
