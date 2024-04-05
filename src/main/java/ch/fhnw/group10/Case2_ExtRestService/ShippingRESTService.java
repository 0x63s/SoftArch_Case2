package ch.fhnw.group10.Case2_ExtRestService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.client.ExternalTaskClient;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import java.util.HashMap;
import java.util.Map;

public class ShippingRESTService {
	final static String SHIPPING_SERVICE_URL = "http://192.168.111.5:8080/v1/consignment/request";
	static final String EXTERNAL_TASK_TOPIC = "group10_contact_freight_forwarder";
	static final String EXTERNAL_TASK_BASIS_URL = "http://group10:Pliuzbi7vt8Ioud@192.168.111.3:8080/engine-rest"; //internal dev server, credentials are therefore visible
	final static String DROOLSTABLE = "ch/fhnw/group10/Case2_ExtRestService/Consignment/Shipping.drl.xls";
	static KieSession kieSession;


	private static final Logger log = LogManager.getLogger(ShippingRESTService.class);
	public static void main(String[] args) {
		ShippingRESTService srs = new ShippingRESTService();
		log.info("Initializing the Drools engine...");
		srs.init();

		log.info("Starting the external task client for the shipping service...");
		//Create a new external task client
		ExternalTaskClient client = ExternalTaskClient.create()
				.baseUrl(EXTERNAL_TASK_BASIS_URL).asyncResponseTimeout(1000).build();

		client.subscribe(EXTERNAL_TASK_TOPIC).lockDuration(1000).handler((externalTask, externalTaskService) -> {

			//Grab the variables from the process engine
			String delivery_address = (String) externalTask.getVariable("delivery_address");
			String customer_id = (String) externalTask.getVariable("customer_id");
			String contact_phone = (String) externalTask.getVariable("contact_phone");
			Long weight_temp = (Long) externalTask.getVariable("weight");
			int weight = weight_temp.intValue();
			String countryCodeString = (String) externalTask.getVariable("country_code");
			NewConsignment.Country country_code = NewConsignment.Country.valueOf(countryCodeString);


			//Print the variables
			log.info("Data for the consignment received from the process engine:");
			log.info("delivery_address: " + delivery_address);
			log.info("customer_id: " + customer_id);
			log.info("contact_phone: " + contact_phone);
			log.info("weight: " + weight);
			log.info("country_code: " + country_code);
			log.info("----------------------------------------------------------");

			//Create a REST client and send the data to the freight forwarder
			Client clientREST = ClientBuilder.newClient();
			WebTarget target = clientREST.target(SHIPPING_SERVICE_URL);

			// create the message object that we will send to the service
			NewConsignment nc = new NewConsignment();
			nc.setDeliveryAddress(delivery_address);
			nc.setRecepientPhone(contact_phone);
			nc.setCustomerReference(customer_id);
			nc.setWeight(weight);
			nc.setCountryCode(country_code);
			//Running the Drools engine to determine the shipping method
			log.info("Checking the shipping method...");
			nc.setShippingMethod(checkShippingMethod(nc));
			log.info("Shipping method: " + nc.getShippingMethod().name());
			log.info("----------------------------------------------------------");
			log.info("Sending the data to the freight forwarder...");

			//Set up the response objects
			Consignment response = null;
			Map<String, Object> results = new HashMap<String, Object>();

			try {
				//Send the data to the freight forwarder
				response = target.request(MediaType.APPLICATION_JSON)
						.post(Entity.entity(nc, MediaType.APPLICATION_JSON), Consignment.class);

				//Print the response
				log.info("----------------------------------------------------------");
				log.info("Response from the freight forwarder received:");
				log.info("Shipping Order ID: " + response.getOrderId());
				log.info("Pickup Date      : " + response.getPickupdate());
				log.info("Delivery Date    : " + response.getDeliverydate());

				//Send back the response to the process engine
				log.info("----------------------------------------------------------");
				log.info("Sending the response back to the process engine...");
				results.put("shipment_id", response.getOrderId());
				results.put("pick_up_date", response.getPickupdate());
				results.put("delivery_date", response.getDeliverydate());
				results.put("shipping_method", nc.getShippingMethod().name());
				results.put("success", true);

			} catch (WebApplicationException e) {
				//In case of an error...
				log.info("Error while ordering consignment:");
				if (e.getResponse().getStatus() == 501) {
					log.info("Request was not possible, please use hotline to order");
				} else {
					System.err.println(e.getMessage());
				}

				//Send back success = false
				results.put("success", false);
			}
			log.info("----------------------------------------------------------");

			//Close the REST client and complete the external task
			clientREST.close();
			externalTaskService.complete(externalTask, results);
		}).open();

		System.out.println("\n\n\n\n");
		log.info("External task client started successfully!");
	}

	private void init() {
		// Initialize the Drools engine
		KieServices kieServices = KieServices.Factory.get();

		// Load the rules from the Spreadsheet
		Resource dt = ResourceFactory.newClassPathResource(DROOLSTABLE, getClass());

		// Initialize the rule engine
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(dt);
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
		kieBuilder.buildAll();
		KieRepository kieRepository = kieServices.getRepository();
		ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
		KieContainer kieContainer = kieServices.newKieContainer(krDefaultReleaseId);
		kieSession = kieContainer.newKieSession();
	}

	private static NewConsignment.ShippingMethod checkShippingMethod(NewConsignment consignment){
		// Create a new session and insert the consignment
		kieSession.insert(consignment);
		kieSession.fireAllRules();

		// Return the shipping method
		return NewConsignment.ShippingMethod.valueOf(consignment.getShippingMethod().name());
	}
}
