package contact.resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * ContactResource provides RESTful web resources using JAX-RS
 * annotations to map requests to request handling code,
 * and to inject resources into code.
 * 
 * @author Natchanon Hongladaromp 5510546034
 *
 */
@Path("/contacts")
public class ContactResource {
	
	private ContactDao dao;
	
	@Context
	UriInfo uriInfo;
	
	public ContactResource() {
		dao = DaoFactory.getInstance().getContactDao();
	}

	/**
	 * Get contact(s) whose title contains the query string(substring match).
	 * @param query String to query
	 * @return contact(s) whose title contains the query string 
	 */
	@GET
	@Produces( MediaType.APPLICATION_XML )
	public Response getContacts( @QueryParam("q") String query ) {
		if(query==null) return getContacts();
		StringBuilder sb = new StringBuilder();
		
		List<Contact> cts = new ArrayList<Contact>();
		Iterator<Contact> itr = dao.findAll().iterator();
		while(itr.hasNext()) {
			Contact c = itr.next();
			if(c.getTitle().contains(query))
				cts.add(c);
		}
		
		GenericEntity<List<Contact>> entitiies = new GenericEntity<List<Contact>>(cts){};
		return Response.ok(entitiies).build();
	}
	
	/**
	 * Get a list of all contacts.
	 * @return list of all contacts
	 */
	public Response getContacts() {
		GenericEntity<List<Contact>> entitiies = new GenericEntity<List<Contact>>(dao.findAll()){};
		return Response.ok(entitiies).build();
	}
	
	/**
	 * Get one contact by id.
	 * @param id id
	 * @return contact
	 */
	@GET
	@Path("{id}")@Produces( MediaType.APPLICATION_XML )
	public Response getContact( @PathParam("id") long id ) 
	{
		Contact contact = dao.find(id);
		return Response.ok(contact).build();
	}
	
	/**
	 * Create a new contact. If contact id is omitted or 0, the server will assign a unique ID and return it as the Location header.
	 * @param element contact element
	 * @param uriInfo uri information
	 * @return URI location
	 * @throws URISyntaxException
	 */
	@POST
	@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
	public Response postContact( JAXBElement<Contact> element, @Context UriInfo uriInfo ) throws URISyntaxException {
		Contact contact = element.getValue();
		dao.save( contact );
		return Response.created(new URI("localhost:8080/contacts/"+contact.getId())).build();
	}

	/**
	 * Update a contact. Only update the attributes supplied in request body.
	 * @param element contact element
	 * @param id id
	 * @return URI location
	 * @throws URISyntaxException
	 */
	@PUT
	@Path("{id}")@Produces( MediaType.APPLICATION_XML )
	public Response updateContact( JAXBElement<Contact> element, @PathParam("id") String id) throws URISyntaxException {
		Contact contact = element.getValue();
		contact.setId(Long.parseLong(id));
		dao.update(contact);
		return Response.created(new URI("localhost:8080/contacts/"+id)).build();
	}
	
	/**
	 * Delete a contact with matching id.
	 * @param id id
	 */
	@DELETE
	@Path("{id}")@Produces( MediaType.APPLICATION_XML )
	public void deleteContact( @PathParam("id") String id) {
		dao.delete(Long.parseLong(id));
	}
}
