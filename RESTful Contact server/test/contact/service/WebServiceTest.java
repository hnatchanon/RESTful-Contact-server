package test.contact.service;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;

import contact.JettyMain;
import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.mem.MemContactDao;
import contact.service.mem.MemDaoFactory;

public class WebServiceTest {

	final static int PORT = 11414;
	
	private static String serviceUrl;
	private HttpClient client;
	private ContactDao dao;
	private Contact contact1;
	private Contact contact2;
	
	@BeforeClass
	public static void doFirst( ) throws Exception {
		// Start the Jetty server. 
		// Suppose this method returns the URL (with port) of the server
		serviceUrl = JettyMain.startServer( PORT );
		
	}
	
	@AfterClass
	public static void doLast( ) throws Exception {
		// stop the Jetty server after the last test
		JettyMain.stopServer();
	}

	
	@Before
	public void setUp() throws Exception {
		client = new HttpClient();
		client.start();
		// create a new DAO for each test and create some sample contacts
		dao = MemDaoFactory.getInstance().getContactDao();
		contact1 = new Contact("contact1", "Joe Contact", "joe@microsoft.com", "088888888");
		contact2 = new Contact("contact2", "Sally Contract", "sally@foo.com", "078984789");
		dao.save(contact1);
		dao.save(contact2);
	}
	
	@Test
	public void testGetContactSuccess() throws InterruptedException, ExecutionException, TimeoutException {
		ContentResponse response = client.GET(serviceUrl + "contacts/1001");
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		response = client.GET(serviceUrl + "contacts/1002");
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	};
	
	@Test
	public void testGetContactFail() throws InterruptedException, ExecutionException, TimeoutException {
		ContentResponse response = client.GET(serviceUrl + "contacts/11111");
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	};
	
	@Test
	public void testPostContactSuccess() throws InterruptedException, ExecutionException, TimeoutException {
		
		String contentStr = "<contact>"
				+ "<title>Test</title>"
				+ "<name>Name Tester</name>"
				+ "<email>tt@test.t</email>"
				+ "<phoneNumber>123456789</phoneNumber>"
				+ "</contact>";
		StringContentProvider content = new StringContentProvider(contentStr);
		
		Request request = client.newRequest(serviceUrl + "contacts/");
		request.content(content, "application/xml");
		request.method(HttpMethod.POST);
		
		ContentResponse response = request.send();
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
	};
	
	@Test
	public void testPostContactFail() throws InterruptedException, ExecutionException, TimeoutException {
		
		String contentStr = "<contact id='1001'>"
				+ "<title>Test</title>"
				+ "<name>Name Tester</name>"
				+ "<email>tt@test.t</email>"
				+ "<phoneNumber>123456789</phoneNumber>"
				+ "</contact>";
		StringContentProvider content = new StringContentProvider(contentStr);
		
		Request request = client.newRequest(serviceUrl + "contacts/");
		request.content(content, "application/xml");
		request.method(HttpMethod.POST);
		
		ContentResponse response = request.send();
		assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
	};
	
	@Test
	public void testPutContactSuccess() throws InterruptedException, ExecutionException, TimeoutException {
		long id = 1001;
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
		
		ContentResponse response = request.send();
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
	};
	
	@Test
	public void testPutContactFail() throws InterruptedException, ExecutionException, TimeoutException {
		long id = 111111;
		String contentStr = "<contact>"
				+ "<title>Test</title>"
				+ "</contact>";
		StringContentProvider content = new StringContentProvider(contentStr);
		
		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.content(content, "application/xml");
		request.method(HttpMethod.PUT);
		
		ContentResponse response = request.send();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	};
	
	@Test
	public void testDeleteSuccess() throws InterruptedException, TimeoutException, ExecutionException {
		long id = 1002;
		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.method(HttpMethod.DELETE);
		ContentResponse response = request.send();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testDeleteFail() throws InterruptedException, TimeoutException, ExecutionException {
		long id = 12424;
		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.method(HttpMethod.DELETE);
		ContentResponse response = request.send();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
}
