package contact.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="contacts")
@XmlAccessorType(XmlAccessType.FIELD)
public class Contacts {
	
	@XmlElement(name="contact")
	private List<Contact> contacts;

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}
	
	public List<Contact> getContacts() {
		return this.contacts;
	}
}
