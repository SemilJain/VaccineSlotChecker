package com.vaccine.checker.service;

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.vaccine.checker.utility.VaccineUtility;

@Service
public class VaccineService {

	private static final Logger logger = LoggerFactory.getLogger(VaccineService.class);

	@Autowired
	VaccineUtility vaccineUtility;
	@Value("${api.key}")
	String apiKey;
	@Value("${email.from}")
	String fromEmail;
	@Value("${email.to}")
	String toEmail;

	public JSONObject getVaccineResponse(String districtId, String date) {
		JSONObject response = null;
		try {
			JSONObject apiResponse = callCowinAPI(districtId, date);
			response = getFinalResponse(apiResponse);
		} catch (Exception e) {
			response = new JSONObject().put("errorMessage",
					"Error while checking for vaccine slots. Try again after some time");
			logger.error("Error in Service class: {}", e.getMessage());
			e.printStackTrace();
		}

		return response;

	}

	@Async
	public void sendMail(String response) {
		Email from = new Email(fromEmail);
		String subject = "Vaccine Details from Cowin Site";
		Content content = new Content("text/plain", response);
		Email to = new Email(toEmail);
		Mail mail = new Mail(from, subject, to, content);
		SendGrid sg = new SendGrid(apiKey);
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response responseMail = sg.api(request);
			System.out.println("Mail Status: " + responseMail.getStatusCode());
			System.out.println("Mail Body: " + responseMail.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private JSONObject callCowinAPI(String districtId, String date) {
		JSONObject resp = vaccineUtility.callAPI(districtId, date);
		return resp;
	}

	private JSONObject getFinalResponse(JSONObject apiResponse) {
		JSONObject finalResponse = new JSONObject();
		JSONArray centerData = new JSONArray();
//		System.out.println(apiResponse);
		JSONArray centers = apiResponse.getJSONArray("centers");

		for (Object o : centers) {
			JSONObject eachCenter = (JSONObject) o;
			if (checkIf18plusCenter(eachCenter))
				centerData.put(getStructuredData(eachCenter));
		}
		finalResponse.put("errorMessage", "");
		finalResponse.put("data", centerData);
		return finalResponse;
	}

	private JSONObject getStructuredData(JSONObject eachCenter) {
		JSONObject structuredData = new JSONObject();
		structuredData.put("Center Name", eachCenter.getString("name"));
		structuredData.put("Address", eachCenter.getString("address"));
		structuredData.put("fee type", eachCenter.getString("fee_type"));
		structuredData.put("Details", getVaccineDetails(eachCenter.getJSONArray("sessions")));
		structuredData.put("Fees (If Any)", eachCenter.optJSONArray("vaccine_fees"));
		return structuredData;
	}

	private JSONArray getVaccineDetails(JSONArray jsonArray) {
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = jsonArray.getJSONObject(i);
			if (obj.getInt("min_age_limit") == 45)
				jsonArray.remove(i);
		}
		return jsonArray;
	}

	private Boolean checkIf18plusCenter(JSONObject eachCenter) {
		for (Object o : eachCenter.getJSONArray("sessions")) {
			JSONObject session = (JSONObject) o;
			if (session.getInt("min_age_limit") == 18)
				return true;
		}
		return false;

	}

}
