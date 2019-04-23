package com.order.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.order.adapter.RestTemplateAdapter;
import com.order.config.SystemConfig;
import com.order.dto.InvoiceResponse;
import com.order.dto.OrderCaptureRequest;
import com.order.dto.OrderCaptureResponse;
import com.order.dto.OrderLine;
import com.order.dto.OrderLineAmount;
import com.order.dto.ProductResponse;
import com.order.dto.ProductResponseList;
import com.order.dto.StockUpdate;
import com.order.dto.StockUpdateResponse;
import com.order.entity.Order;
import com.order.exception.ResourceNotFoundException;
import com.order.exception.ServiceUnavailableException;
import com.order.exception.UnavailableStockException;
import com.order.repository.OrderLineRepository;
import com.order.repository.OrderRepository;

@Service
public class OrderService {

	@Autowired
	public OrderRepository orderRepo;

	@Autowired
	public OrderLineRepository orderLineRepo;

	@Autowired
	public SystemConfig systemConfig;

	@Autowired
	public RestTemplateAdapter restTemplateAdapter;

	@Autowired
	public KafkaService kafkaService;

	public OrderCaptureResponse captureOrder(OrderCaptureRequest orderCaptureRequest) {
		// TODO Auto-generated method stub
		String errResponse;
		ResponseEntity<StockUpdateResponse> response;
		List<OrderLine> ordeLineList = orderCaptureRequest.getOrderLine();
		for (OrderLine order : ordeLineList) {
			order.setQuantity(Math.negateExact(order.getQuantity()));
		}
		try {
			response = updateStock(orderCaptureRequest.getOrderLine());
		} catch (HttpClientErrorException ex) {
			errResponse = ex.getResponseBodyAsString();
			throw new UnavailableStockException(errResponse);
		}
		if (!response.getStatusCode().equals(HttpStatus.OK))
			throw new ServiceUnavailableException("Service Unavailable");
		for (OrderLine order : ordeLineList) {
			order.setQuantity(Math.negateExact(order.getQuantity()));
		}
		Order persistedOrder = updateOrderDetailsinDB(orderCaptureRequest);
		sendShipmentRequest(orderCaptureRequest, persistedOrder);
		OrderCaptureResponse orderResponse = new OrderCaptureResponse();
		orderResponse.setOrderId(persistedOrder.getOrderId());

		orderResponse.setOrderLine(orderCaptureRequest.getOrderLine());

		return orderResponse;
	}

	private void sendShipmentRequest(OrderCaptureRequest orderCaptureRequest, Order persistedOrder) {

		kafkaService.sendShipmentRequest(persistedOrder.getOrderId(), orderCaptureRequest.getCustomerId());

	}

	private Order updateOrderDetailsinDB(OrderCaptureRequest orderCaptureRequest) {

		Order order = new Order();
		order.setCustomerId(orderCaptureRequest.getCustomerId());
		order.setOrderStatus("ORDER CAPTURED");
		order.setOrderReceivedDate(new Date(System.currentTimeMillis()));
		List<com.order.entity.OrderLine> orderLineEntityList = new ArrayList<>();
		for (OrderLine orderLine : orderCaptureRequest.getOrderLine()) {
			com.order.entity.OrderLine orderLineEntity = new com.order.entity.OrderLine();
			orderLineEntity.setOrder(order);
			orderLineEntity.setProductId(orderLine.getProductId());
			orderLineEntity.setQuantity(orderLine.getQuantity());
			orderLineEntityList.add(orderLineEntity);
		}
		order.setOrderLine(orderLineEntityList);
		return orderRepo.save(order);

	}

	private ResponseEntity<StockUpdateResponse> updateStock(List<OrderLine> orderLine) {

		String stockListURL = systemConfig.getStockListUrl();
		// String stockListURL = "http://localhost:8080/inventory/stock/list";
		return restTemplateAdapter.callInventoryManagementService(new StockUpdate(orderLine), stockListURL,
				HttpMethod.POST, StockUpdateResponse.class);

	}

	public InvoiceResponse generateInvoice(String orderId) {

		Optional<Order> persistedOrderOp = orderRepo.findById(Long.valueOf(orderId));
		if (!persistedOrderOp.isPresent())
			throw new ResourceNotFoundException("No order placed with this id");
		InvoiceResponse resp = new InvoiceResponse();
		Order order = persistedOrderOp.get();
		resp.setCustomerId(order.getCustomerId());
		resp.setOrderId(order.getOrderId());

		List<String> productIdList = new ArrayList<>();

		for (com.order.entity.OrderLine orderLine : order.getOrderLine()) {

			productIdList.add(String.valueOf(orderLine.getProductId()));
		}

		String prodListurl = systemConfig.getProdListUrl();
		// String uri = "http://localhost:8080/inventory/product/idList";

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		params.put("ids", productIdList);

		String paramURI = UriComponentsBuilder.fromHttpUrl(prodListurl).queryParams(params).build().toUriString();

		ResponseEntity<ProductResponseList> response = restTemplateAdapter.callInventoryManagementService(null,
				paramURI, HttpMethod.GET, ProductResponseList.class);

		ProductResponseList prodResponse = response.getBody();
		List<ProductResponse> prodList = prodResponse.getProduct();

		Map<Long, ProductResponse> map = new HashMap<>();
		for (ProductResponse i : prodList) {
			map.put(i.getProductId(), i);
		}
		List<OrderLineAmount> orderLineAmountList = new ArrayList<>();
		int totalAmount = 0;
		for (com.order.entity.OrderLine orderLine : order.getOrderLine()) {
			OrderLineAmount orderLineAmount = new OrderLineAmount();
			orderLineAmount.setProductId(orderLine.getId());
			orderLineAmount.setQuantity(orderLine.getQuantity());
			orderLineAmount.setProductName(map.get(orderLine.getProductId()).getProductName());
			orderLineAmount.setProductDesc(map.get(orderLine.getProductId()).getProductDesc());
			orderLineAmount.setPrice(map.get(orderLine.getProductId()).getPrice());
			orderLineAmount.setAmount(
					Math.multiplyExact(map.get(orderLine.getProductId()).getPrice(), orderLine.getQuantity()));
			orderLineAmountList.add(orderLineAmount);
			totalAmount = totalAmount + orderLineAmount.getAmount();
		}

		resp.setOrderLine(orderLineAmountList);
		resp.setTotalAmount(totalAmount);
		return resp;
	}

}
