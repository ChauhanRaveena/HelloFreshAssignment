package APITest;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import org.json.simple.JSONObject;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.equalTo;

public class ExecutionClass {
	
		int statusCode;
		
		@Test(priority = 0)
		@Parameters({ "baseURI" })
		public void getAllCountries(String baseURI)
		{   
			
			// Specify the base URL to the RESTful web service
			RestAssured.baseURI = baseURI;
	 
			// Get the RequestSpecification of the request that you want to sent
			// to the server. The server is specified by the BaseURI that we have
			// specified in the above step.
			RequestSpecification httpRequest = RestAssured.given();
	 
			// Make a request to the server by specifying the method Type and the method URL.
			// This will return the Response from the server. Store the response in a variable.
			Response response = httpRequest.request(Method.GET, "/all");
	 
			// Now print the body of the message to see what response
			// we have received from the server
			String responseBody = response.getBody().asString();
			
			System.out.println("いいいいいいいいいいいいい Get all countries and validate that US, DE and GB were returned in the responseいいいいいいいいい� "+System.lineSeparator());
			System.out.println("Response Body is =>  " + responseBody+System.lineSeparator());
			
			// Get the status code from the Response. In case of a successful interaction with the web service, 
			//we should get a status code of 200.
			statusCode = response.getStatusCode();
			Assert.assertEquals(statusCode, 200 , "Response Codes do not match"+System.lineSeparator());
			System.out.println("The HTTP Response Code is: "+statusCode+System.lineSeparator());
			
			if(response != null)
			{
				response.then().assertThat().body("RestResponse.result.alpha2_code", hasItems("US","DE","GB"));
				System.out.println("Countries \"US\",\"DE\",\"GB\" are found in the list"+System.lineSeparator());
				System.out.println("Response Validation Successful"+System.lineSeparator());
			}
			
	}
		
		
		@Test(priority = 1)
		@Parameters({ "baseURI" })
		public void getEachCountryAndValidate(String baseURI) throws IOException
		{   
			
			InputStream inputStream = null;
			
			try {
				
				//Load API.properties file to fetch alpha2 codes for countries to get
				Properties prop = new Properties();
				String propFileName = "APITest/API.properties";
				inputStream = ExecutionClass.class.getClassLoader().getResourceAsStream(propFileName);
	 
				if (inputStream != null) {
					prop.load(inputStream);
				} else {
					throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				}
				
				//Fetch the values from API.properties file one by one and execute the REST request to get the countries as per their contrycodes
				for(Entry<Object, Object> countryCodes : prop.entrySet())
				{
					String[] values = new String[prop.size()];
					int index = 0;
					values[index] = (String) countryCodes.getValue();
					
					// Specify the base URL to the RESTful web service
					RestAssured.baseURI = baseURI;
					
					// Get the RequestSpecification of the request that you want to sent
					// to the server. The server is specified by the BaseURI that we have
					// specified in the above step.
					RequestSpecification httpRequest = RestAssured.given();
			 
					// Make a request to the server by specifying the method Type and the method URL.
					// This will return the Response from the server. Store the response in a variable.
					Response response = httpRequest.request(Method.GET, "/iso2code/"+values[index]);
			 
					// Now let us print the body of the message to see what response
					// we have received from the server
					String responseBody = response.getBody().asString();
					
					System.out.println("いいいいいいいいいい� Get response for COUNTRY_ISO2CODE = "+values[index]+" いいいいいいいいいいい� "+System.lineSeparator());
					System.out.println("Response Body is =>  " + responseBody);

					//Validate whether the response element alpha2_codes - US,DE,GB are found in the response. Its validated against the API.properties
					if(response.body().jsonPath().getJsonObject("RestResponse.result.alpha2_code") != null)
					{
					response.then().assertThat().body("RestResponse.result.alpha2_code", equalTo(values[index]));
					statusCode= response.getStatusCode();
					Assert.assertEquals(statusCode, 200 , "Response Codes do not match");
					System.out.println("Response Status Code is "+response.getStatusCode()+System.lineSeparator());
					System.out.println("Response Validation Successful"+System.lineSeparator());
					System.out.println("CountryCode "+values[index]+ " found in the list"+System.lineSeparator());
					}
					//Validate whether the response element alpha2_code for an inexistent country are not found in the response. Its validated against the API.properties
					else
					{
						statusCode = response.getStatusCode();
						Assert.assertEquals(statusCode, 200 , "Response Codes do not match");
						System.out.println("Response Status Code is "+response.getStatusCode()+System.lineSeparator());
						System.out.println("Response Validation Successful"+System.lineSeparator());
						System.out.println(values[index]+" Not Found In The List"+System.lineSeparator());
					}
					
				}
		     }
		  catch (Exception e) 
		 {
			System.out.println("Exception: " + e+System.lineSeparator());
		 } 
			finally {
			inputStream.close();
		}
	}
		
		@SuppressWarnings("unchecked")
		@Test(priority = 2)
		@Parameters({ "baseURI" })
		public void addNewCountry(String baseURI )
		{   
			
			// Specify the base URL to the RESTful web service
			RestAssured.baseURI = baseURI;
	 
			// Get the RequestSpecification of the request that you want to sent to the server. The server is specified by the BaseURI that we have
			// specified in the above step.
			RequestSpecification httpRequest = RestAssured.given();
	 
			//Create a JSON Request
			// JSONObject is a class that represents a simple JSON. We can add Key - Value pairs using the put method
			JSONObject requestParams = new JSONObject();
			requestParams.put("name", "Test Country"); // Cast
			requestParams.put("alpha2_code", "TC");
			requestParams.put("alpha3_code", "TCY");
	
			//Add the JSON body to request and send the request
			// Add a header stating the Request body is a JSON
			httpRequest.header("Content-Type", "application/json");
			 
			// Add the Json to the body of the request
			httpRequest.body(requestParams.toJSONString());
			 
			// Post the request and check the response
			Response response = httpRequest.request(Method.POST, "/iso2code/TC");
			
			//Validate the response
			statusCode = response.getStatusCode();
			Assert.assertEquals(statusCode, 200 , "Response Codes do not match");
			System.out.println("いいいいいいいいいいいいいいいい Get response for POST Operation いいいいいいいいいいいいいいいい�"+System.lineSeparator());
			System.out.println("Status Code for addition of new country is "+statusCode+System.lineSeparator());
			System.out.println("Response Validation Successful"+System.lineSeparator());
			System.out.println("Response Body For POST: "+System.lineSeparator()+response.getBody().asString());
			
	}
		
}

