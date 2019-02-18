package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BarcodeScannersDTO;
import com.example.demo.model.Customer;
import com.example.demo.model.Waybill;
import com.example.demo.service.BarcodeScannerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RestController
public class BarcodeScannerController {

	@Autowired
	BarcodeScannerService barcodeScannerService;

	@RequestMapping(value = "/create-flight-id", method = RequestMethod.POST)
	public ResponseEntity<?> login(@RequestBody HashMap<String, String> info, HttpServletResponse response) {

		String flightId = barcodeScannerService.createFlightId(info.get("data"));
		return new ResponseEntity<String>(flightId, HttpStatus.OK);
	}

	@RequestMapping(value = "/scan", method = RequestMethod.POST)
	public ResponseEntity<?> barcodeScan(@RequestBody HashMap<String, BarcodeScannersDTO> info,
			HttpServletResponse response) {

		BarcodeScannersDTO scan = info.get("data");
		String track = barcodeScannerService.createWaybill(scan);

		return new ResponseEntity<String>(track, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-customer", method = RequestMethod.GET)
	public ResponseEntity<?> getCustomer() {

		List<Customer> lstCustomer = barcodeScannerService.getCustomerData();

		return new ResponseEntity<List<Customer>>(lstCustomer, HttpStatus.OK);
	}

	@RequestMapping(value = "/show-all-waybill", method = RequestMethod.GET)
	public ResponseEntity<?> showAllWaybill() {

		List<Waybill> lstInfo = barcodeScannerService.showAllWaybill();

		return new ResponseEntity<List<Waybill>>(lstInfo, HttpStatus.OK);
	}

	@RequestMapping(value = "/trackingEMS", method = RequestMethod.POST)
	public String apiPOST(@RequestBody HashMap<String, Object> info, HttpServletResponse response) {
		try {
			String url = (String) info.get("url");
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(info.get("param"));			
			String result = sendApiPOST(url, json);
			if (!result.equals("API_ERROR")) {
				if (result.contains("\"code\":4016") || result.contains("\"code\":200")) {
					String code = info.get("tracking_number").toString();
					String urlGet = "https://api.trackingmore.com/v2/trackings/vietnam-post/" + code;
					result = sendApiGET(urlGet);
				}
			}
			return result;
		} catch (Exception e) {
			// TODO: handle exception
			return "API_ERROR";
		}
	}

	// get data from external api with get action
	public String sendApiGET(String url) throws Exception {

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// Starts the connection
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Trackingmore-Api-Key", "e534a472-b74a-4f56-82e7-0e1e1c9023a1");
			// add request header
			int reponseCode = con.getResponseCode();
			if (reponseCode != 200) {
				return "API_ERROR";
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer result = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}
			in.close();
			return result.toString();
		} catch (Throwable throwable) {
			// TODO: handle exception
			return "API_ERROR";
		}

	}

	public String sendApiPOST(String url, String json) throws Exception {

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Trackingmore-Api-Key", "e534a472-b74a-4f56-82e7-0e1e1c9023a1");
			if (json != "") {
				OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
				writer.write(json);
				writer.close();
			}

			int reponseCode = con.getResponseCode();
			if (reponseCode != 200) {
				return "API_ERROR";
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer result = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}
			in.close();
			return result.toString();
		} catch (Throwable throwable) {
			// TODO: handle exception
			return "API_ERROR";
		}

	}

}
