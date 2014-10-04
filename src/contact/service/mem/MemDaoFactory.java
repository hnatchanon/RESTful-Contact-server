package contact.service.mem;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.internal.descriptors.InteractionArgument;

import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * Manage instances of Data Access Objects (DAO) used in the app.
 * This enables you to change the implementation of the actual MemContactDao
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
		load(daoInstance);
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
		ContactList exportContacts = new ContactList();
		exportContacts.setContactList( contacts );

		try {
			JAXBContext context = JAXBContext.newInstance( ContactList.class );
			File outputFile = new File( "D://data/data.xml" );
			Marshaller marshaller = context.createMarshaller();	
			marshaller.marshal( exportContacts, outputFile );
		} catch ( JAXBException e ) {
			e.printStackTrace();
		}
//		System.out.println("save");
	}

	public static void load(ContactDao daoInstance) {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance( ContactList.class );
			File inputFile = new File( "D://data/data.xml" );
			Unmarshaller unmarshaller = context.createUnmarshaller();
			ContactList importContacts = (ContactList) unmarshaller.unmarshal(inputFile);
			if(importContacts.getContactList() != null) {
				Iterator<Contact> itr = importContacts.getContactList().iterator();
				while(itr.hasNext())
					daoInstance.save(itr.next());
			}

		} catch (JAXBException e) {
			e.printStackTrace();
		}	
//		System.out.println("load");
	}
}
