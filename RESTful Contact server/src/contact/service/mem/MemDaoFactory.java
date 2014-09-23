package contact.service.mem;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import contact.entity.Contact;
import contact.entity.Contacts;
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * Manage instances of Data Access Objects (DAO) used in the app.
 * This enables you to change the implementation of the actual ContactDao
 * without changing the rest of your application.
 * 
 * @author jim
 */
public class MemDaoFactory extends DaoFactory {
	// singleton instance of this factory
	private static DaoFactory factory;
	private ContactDao daoInstance;
	
	private MemDaoFactory() {
		daoInstance = new MemContactDao();
	}
	
	public static DaoFactory getInstance() {
		if (factory == null) factory = new MemDaoFactory();
		return factory;
	}
	
	public ContactDao getContactDao() {
		return daoInstance;
	}

	@Override
	public void shutdown() {
		List<Contact> contacts = daoInstance.findAll();
		Contacts exportContacts = new Contacts();
		exportContacts.setContacts( contacts );

		try {
			JAXBContext context = JAXBContext.newInstance( Contacts.class );
			File outputFile = new File( "/tmp/ContactsSevicePersistence.xml" );
			Marshaller marshaller = context.createMarshaller();	
			marshaller.marshal( exportContacts, outputFile );
		} catch ( JAXBException e ) {
			e.printStackTrace();
		}
	}
}
