package com.vaccine.checker.utility;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Component
public class VaccineUtility {

	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	RestTemplate restTemplate;
	@Value("${api.cowin.getbydistrict}")
	String apiByDistrict;

	public Object convertJsonToObject(JSONObject jsonObject) {
		Object response = null;
		try {
			response = objectMapper.readValue(jsonObject.toString(), Object.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return response;
	}

	public String getTodaysDate() {

		return new SimpleDateFormat("dd-MM-yyyy").format(new Timestamp(System.currentTimeMillis()));
	}

	public JSONObject callAPI(String districtId, String date) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent", "Mozilla/5.0 Firefox/26.0");
		HttpEntity request = new HttpEntity(headers);
		ResponseEntity<Object> apiResp = restTemplate.exchange(apiByDistrict, HttpMethod.GET, request, Object.class,
				districtId, date);
		return new JSONObject(new Gson().toJson(apiResp.getBody()));
	}

}
