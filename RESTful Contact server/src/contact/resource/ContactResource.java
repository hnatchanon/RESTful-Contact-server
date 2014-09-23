package contact.resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.jpa.JpaDaoFactory;
import contact.service.mem.MemDaoFactory;

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
		dao = MemDaoFactory.getInstance().getContactDao();
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
		
		List<Contact> cts = dao.findByTitle(query);
		
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
		if(contact != null)
			return Response.ok(contact).build();
		return Response.status(Response.Status.NOT_FOUND).build();
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
		if(dao.find(contact.getId()) == null) {
			if(dao.save( contact )) {
				URI uri = uriInfo.getAbsolutePathBuilder().path(""+contact.getId() ).build();
				return Response.created(uri).build();
			}
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		return Response.status(Response.Status.CONFLICT).build();
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
	public Response updateContact( JAXBElement<Contact> element, @PathParam("id") long id) throws URISyntaxException {
		Contact contact = element.getValue();
		if(dao.find(id) == null) return Response.status(Response.Status.NOT_FOUND).build();
		contact.setId(id);
		
		if(contact.getEmail() == null)
			contact.setEmail("");
		if(contact.getName() == null)
			contact.setName("");
		if(contact.getPhoneNumber() == null)
			contact.setPhoneNumber("");
		if(contact.getTitle() == null)
			contact.setTitle("");
		
		if(dao.update(contact)) {
			URI uri = uriInfo.getAbsolutePathBuilder().path(""+contact.getId() ).build();
			return Response.created(uri).build();
		}
		return Response.status(Response.Status.BAD_REQUEST).build();
	}
	
	/**
	 * Delete a contact with matching id.
	 * @param id id
	 */
	@DELETE
	@Path("{id}")@Produces( MediaType.APPLICATION_XML )
	public Response deleteContact( @PathParam("id") String id) {
		if(dao.delete(Long.parseLong(id))) {
			return Response.ok().build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
}
