package test.contact.service;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import contact.JettyMain;
import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.mem.MemDaoFactory;

public class WebServiceTest {

		final static int PORT = 11414;
	
		private static String serviceUrl;
		private HttpClient client;
		private ContactDao dao;
		private Contact contact1;
		private Contact contact2;
	
		@BeforeClass
		public static void doFirst( ) {
			// Start the Jetty server. 
			// Suppose this method returns the URL (with port) of the server
			try {
				serviceUrl = JettyMain.startServer( PORT );
			} catch (Exception e) {
				e.printStackTrace();
			}
	
		}
	
		@AfterClass
		public static void doLast( ) {
			// stop the Jetty server after the last test
			try {
				JettyMain.stopServer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	@Before
	public void setUp() {
		client = new HttpClient();
		try {
			client.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// create a new DAO for each test and create some sample contacts
		dao = MemDaoFactory.getInstance().getContactDao();
		contact1 = new Contact("contact1", "Joe Contact", "joe@microsoft.com", "088888888");
		contact1.setId(1000001);
		contact2 = new Contact("contact2", "Sally Contract", "sally@foo.com", "078984789");
		contact2.setId(1000002);
		dao.save(contact1);
		dao.save(contact2);
	}

	@After
	public void clear() {
		dao.delete(1000001);
		dao.delete(1000002);
		try {
			client.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetContactSuccess() {
		ContentResponse response;
		try {
			response = client.GET(serviceUrl + "contacts/1000001");
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetContactFail() {
		ContentResponse response;
		try {
			response = client.GET(serviceUrl + "contacts/1000000");
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPostContactSuccess() {

		String contentStr = "<contact>"
				+ "<title>@@@@@@</title>"
				+ "<name>Name Tester</name>"
				+ "<email>tt@test.t</email>"
				+ "<phoneNumber>123456789</phoneNumber>"
				+ "</contact>";
		StringContentProvider content = new StringContentProvider(contentStr);

		Request request = client.newRequest(serviceUrl + "contacts/");
		request.content(content, "application/xml");
		request.method(HttpMethod.POST);

		ContentResponse response;
		try {
			response = request.send();
			String[] location = response.getHeaders().get("Location").split("/");
			long id = Long.parseLong(location[location.length-1]);
			dao.delete(id);
			assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPostContactFail() {

		String contentStr = "<contact id='1000001'>"
				+ "<title>Test</title>"
				+ "<name>Name Tester</name>"
				+ "<email>tt@test.t</email>"
				+ "<phoneNumber>123456789</phoneNumber>"
				+ "</contact>";
		StringContentProvider content = new StringContentProvider(contentStr);

		Request request = client.newRequest(serviceUrl + "contacts/");
		request.content(content, "application/xml");
		request.method(HttpMethod.POST);

		ContentResponse response;
		try {
			response = request.send();
			assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPutContactSuccess() {
		long id = 1000001;
		String contentStr = "<contact>"
				+ "<title>Test</title>"
				+ "<name>Name Tester</name>"
				+ "<email>tt@test.t</email>"
				+ "<phoneNumber>123456789</phoneNumber>"
				+ "</contact>";
		StringContentProvider content = new StringContentProvider(contentStr);

		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.content(content, "application/xml");
		request.method(HttpMethod.PUT);

		ContentResponse response;
		try {
			response = request.send();
			assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPutContactFail() {
		long id = 1000000;
		String contentStr = "<contact>"
				+ "<title>Test</title>"
				+ "</contact>";
		StringContentProvider content = new StringContentProvider(contentStr);

		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.content(content, "application/xml");
		request.method(HttpMethod.PUT);

		ContentResponse response;
		try {
			response = request.send();
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteSuccess() {
		long id = 1000002;
		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.method(HttpMethod.DELETE);
		ContentResponse response;
		try {
			response = request.send();
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteFail() {
		long id = 1000000;
		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.method(HttpMethod.DELETE);
		ContentResponse response;
		try {
			response = request.send();
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
