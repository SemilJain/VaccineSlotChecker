package com.vaccine.checker.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vaccine.checker.service.VaccineService;
import com.vaccine.checker.utility.VaccineUtility;

@RestController
public class VaccineController {

	@Autowired
	VaccineService vaccineService;
	@Autowired
	VaccineUtility vaccineUtility;
	@Value("${value.district.id}")
	String districtId;

	private static final Logger logger = LoggerFactory.getLogger(VaccineController.class);

	@GetMapping("/checkAvailability")
	@ResponseBody
	public String getVaccineSlots(@RequestParam String date) {
		String response = "";
		JSONObject jsonResponse = vaccineService.getVaccineResponse(districtId, date);
		logger.info("Final response in Controller {}", jsonResponse);
		if (jsonResponse.getString("errorMessage").isEmpty()) {
			response = generateMessage(jsonResponse.getJSONArray("data"));
			vaccineService.sendMail(response);
		} else {
			response = jsonResponse.getString("errorMessage");
		}
		System.out.println("response: " + response);
		return response;

	}

	private String generateMessage(JSONArray jsonArray) {
		StringBuilder respString = new StringBuilder();
		for (Object o : jsonArray) {
			respString.append("----------------------------------------------------------------------\n");
			JSONObject json = (JSONObject) o;
			respString.append("Center Name : ").append(json.getString("Center Name")).append("\n");
			respString.append("Address : ").append(json.getString("Address")).append("\n");
			respString.append("fee type : ").append(json.getString("fee type")).append("\n");
			respString.append("Fees (If Any) : ").append(json.optJSONArray("Fees (If Any)")).append("\n");
			respString.append("Vaccine Details : \n").append(formatDetails(json.getJSONArray("Details"))).append("\n");
		}

		return respString.toString();
	}

	private String formatDetails(JSONArray details) {
		StringBuilder detailsResp = new StringBuilder();
		int i = 1;
		for (Object o : details) {
			JSONObject json = (JSONObject) o;
			detailsResp.append("\t").append(i + ") ").append("Vaccine Name : ").append(json.getString("vaccine"))
					.append("\n");
			detailsResp.append("\t   ").append("Date : ").append(json.getString("date")).append("\n");
			detailsResp.append("\t   ").append("Age : ").append(json.getInt("min_age_limit")).append("+").append("\n");
			detailsResp.append("\t   ").append("Slots : ").append(json.getJSONArray("slots")).append("\n");
			i++;
		}
		return detailsResp.toString();
	}

//	@Scheduled(cron = "${cron.expression.vaccine}", zone = "IST")
//	public void getVaccineSlotsViaScheduler() {
//		String date = vaccineUtility.getTodaysDate();
//		JSONObject jsonResponse = vaccineService.getVaccineResponse(districtId,date);
//		logger.info("Final response in Controller {}", jsonResponse);
//	}

}
