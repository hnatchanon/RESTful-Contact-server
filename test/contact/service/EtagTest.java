package test.contact.service;

import static org.junit.Assert.*;

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

/**
 * Test ETag, If-Match, and If-None-Match.
 * @author Natchanon Hongladaromp
 *
 */
public class EtagTest {

	final static int PORT = 11414;

	private static String serviceUrl;
	private HttpClient client;

	/**
	 * Do before all test.
	 */
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

	/**
	 * Do after all test.
	 */
	@AfterClass
	public static void doLast( ) {
		// stop the Jetty server after the last test
		try {
			JettyMain.stopServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Do before each test.
	 */
	@Before
	public void setUp() {
		client = new HttpClient();
		try {
			client.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Contact contact = new Contact("contact1", "Joe Contact", "joe@microsoft.com", "088888888");
		contact.setId(1000001);
		MemDaoFactory.getInstance().getContactDao().save(contact);
	}
	
	/**
	 * Do after each test.
	 */
	@After
	public void clear() {
		MemDaoFactory.getInstance().getContactDao().delete(1000001);
		try {
			client.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test ETag of POST.
	 */
	@Test
	public void testETagPost() {

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

		ContentResponse response = null;
		try {
			response = request.send();
			assertFalse(response.getHeaders().get("Etag").isEmpty());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
		
		String[] location = response.getHeaders().get("Location").split("/");
		Long id = Long.parseLong(location[location.length-1]);

		MemDaoFactory.getInstance().getContactDao().delete(id);
	}
	
	/**
	 * Test ETag of GET.
	 */
	@Test
	public void testETagGet() {
		ContentResponse response;
		try {
			response = client.GET(serviceUrl + "contacts/1000001");
			assertFalse(response.getHeaders().get("Etag").isEmpty());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test GET with header "If-None-Match" that none match.
	 */
	@Test
	public void testGetNoneMatchSuccess() {
		long id = 1000001;
		Contact c = MemDaoFactory.getInstance().getContactDao().find(id);
		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.header("If-None-Match", "\""+c.hashCode()+"\"");
		request.method(HttpMethod.GET);
		System.out.println(request.getHeaders());

		ContentResponse response;
		try {
			response = request.send();
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test GET with header "If-None-Match" that match.
	 */
	@Test
	public void testGetNoneMatchFail() {
		long id = 1000001;
		Request request = client.newRequest(serviceUrl + "contacts/" + id);
		request.header("If-None-Match", "\"123456789\"");
		request.method(HttpMethod.GET);
		System.out.println(request.getHeaders());

		ContentResponse response;
		try {
			response = request.send();
			assertEquals(Response.Status.NOT_MODIFIED.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test PUT with header "If-Match" that not match.
	 */
	@Test
	public void testPutMatchFail() {
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
		request.header("If-Match", "\"lll\"");
		request.method(HttpMethod.PUT);

		ContentResponse response;
		try {
			response = request.send();
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
