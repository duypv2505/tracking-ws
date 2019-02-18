package com.example.demo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.example.demo.dto.BarcodeScannersDTO;
import com.example.demo.model.Customer;
import com.example.demo.model.Flight;
import com.example.demo.model.Waybill;


@Service
public class BarcodeScannerService {

	@Autowired
	MongoTemplate mongoTemplate;
	
	public String createFlightId(String flightId) {
		
		Flight flight = mongoTemplate.findAndModify(
        		new Query(Criteria.where("_id").is(flightId)),
                new Update().set("_id", flightId),
                new FindAndModifyOptions().returnNew(true).upsert(true),
                Flight.class);
    	
		return flight.get_id();
	}
	
	public String createWaybill(BarcodeScannersDTO scanObj) {
		
		Waybill waybill = mongoTemplate.findAndModify(
        		new Query(Criteria.where("barcode").is(scanObj.getBarcode())
        				//.and("customerId").is(scanObj.getCustomerId())
        				.and("flightId").is(scanObj.getFlightId())),
                new Update().set("barcode", scanObj.getBarcode())
                			.set("flightId", scanObj.getFlightId())
                			.set("customerId", scanObj.getCustomerId()),
                new FindAndModifyOptions().returnNew(true).upsert(true),
                Waybill.class);
    	
		return waybill.getBarcode();
	}
	
	public List<Customer> getCustomerData(){
		List<Customer> ret = mongoTemplate.findAll(Customer.class);
		
		return ret;
		
	}
	
	public List<Waybill> showAllWaybill(){
		List<Waybill> lstQuery = mongoTemplate.findAll(Waybill.class);
		lstQuery.sort(Comparator.comparing(Waybill::getFlightId)
								.reversed()
			                    .thenComparing(Comparator.comparing(Waybill::getCustomerId)
			                    .reversed()));
		
		return lstQuery;
	}
}
