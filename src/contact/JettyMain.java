package contact;

import java.io.IOException;

import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import contact.resource.ContactResource;
import contact.service.mem.MemDaoFactory;

/**
 * <p>
 * This example shows how to deploy a RESTful web service
 * using a Jetty server that is created and started via code.
 * </p><p>
 * In this example, the resource classes are in the package "demo.resource".
 * Each resource class should be annotated with JAX-RS @Path("/something")
 * and the request handler methods should be annotated with @GET, @POST, etc.
 * </p>
 * <p>
 * This class creates a Jetty server on the specified port (default is PORT),
 * a ContextHandler that represents a Context containing a context path and
 * mapping of pathspecs to Servlets.
 * <p><tt>
 * handler.setContextPath("/")
 * </tt>/p><p>
 * Then the servlet holder is mapped to a path component (inside the context
 * path) using:
 * <p><tt>
 * handler.addServlet( servletHolder, "/*" );
 * </tt></p><p>
 * which means "map" everything inside this context to servletHolder.
 * In a more complex application (context), you could have many servlets
 * and map different pathspecs to different servlets.
 * <p>
 * In the case of a JAX-RS web service, each "resource" class also has
 * a @Path("/something") annotation, which can be used to map different
 * paths to different resources, so one ServletHolder can manage all your
 * resource classes.
 * </p>
 * 
 * <p>
 * I tested this with Jersey 2.12 and Jetty 9.2.  I used the following
 * JARs, referenced as libraries in Eclipse:
 * Jersey: lots of JARs! I included everything from:
 * jersey/lib directory, 
 * jersey/ext directory,
 * and
 * jersey/api/jaxrs.ws.rs-api-2.01.jar 
 * Some of these JARs probably aren't necessary, but I got runtime errors
 * about missing classes when I omitted JARs from the ext/ directory.
 * jersey/ext contains jars from other projects; this
 * may cause a problem if you have another version of the same JARs in
 * your project!  If you do, compare the JARs, or switch to a Maven
 * project so Maven will manage your dependencies. 
 * 
 * @author jim
 *
 */
public class JettyMain {

	private static Server server;

	public static void main(String[] args) {
		startServer(8080);
		waitToStop();
	}

	/**
	 * Create a Jetty server and a context, add Jetty ServletContainer
	 * which dispatches requests to JAX-RS resource objects,
	 * and start the Jetty server.
	 * 
	 * @param port port
	 * @return url
	 */
	public static String startServer(int port) {
		server = new Server( port );

		ServletContextHandler context = new ServletContextHandler( ServletContextHandler.SESSIONS );

		//context.setContextPath("/contacts");

		ServletHolder holder = new ServletHolder( org.glassfish.jersey.servlet.ServletContainer.class );

		holder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "contact.resource");
		context.addServlet( holder, "/*" );

		server.setHandler( context );

		System.out.println("Starting Jetty server on port " + port);
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return server.getURI().toString();
	}

	private static void waitToStop() {
		System.out.println("Server started.  Press ENTER to stop it.");
		try {
			int ch = System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Stopping server.");
		stopServer();
	}

	public static void stopServer() {
		MemDaoFactory.getInstance().shutdown();
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

