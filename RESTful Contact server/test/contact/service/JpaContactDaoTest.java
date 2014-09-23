package test.contact.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.jpa.JpaDaoFactory;
import contact.service.mem.MemContactDao;

/**
 * A few really basic tests of JPA using the JPA DAO.
 * Common problems are:
 * a) the database name or directory in persistence.xml aren't accessible to you.
 * b) "Can't start database" if another instance of Derby is using this database.
 * c) you don't have JDBC driver for Derby correctly configured in persistence.xml
 *    or derby.jar isnt' on the build path.  (Similarly for HSQLDB or MySQL.)
 * d) you didn't add persistence annotations (@Entity, @Id) to the entity classes.
 * e) persistence unit name in JpaDaoFactory (used to create EntityManagerFactory)
 *    isn't same as the name in persistence.xml
 * 
 * TODO for reliable test we need a way to completely wipe clean the database.
 * But the current ContactDao interface doesn't have a "removeAll" method.
 * 
 * @author jim
 *
 */
public class JpaContactDaoTest {
	ContactDao dao;
	Contact foo;

	@BeforeClass
	public void doFirst() {
		// this method is called before any tests and before the @Before method
		//url = JettyMain.startServer( );
	}

	@AfterClass

	@Before
	public void setUp() {
		dao = (new JpaDaoFactory()).getContactDao();
		foo = new Contact("foo", "Joe Contact", "joe@microsoft.com", "088888888");
	}

	@Test
	public void testSaveAndFind() {
		foo = new Contact("foo title", "Foo Bar", "foo@bar.com", "000000000");
		assertTrue( dao.save(foo) );
		assertTrue( foo.getId() > 0 );
		System.out.println("Saved foo and got foo.id = "+foo.getId());

		// Now find it again
		Contact fooAgain = dao.find(foo.getId());
		assertNotNull( fooAgain );
		assertSame( "DAO should return the same object reference", foo, fooAgain ); // foo == fooAgain
	}

	// this test requires that testSaveAndFind be performed first
	@Test
	public void testDelete( ) {
		long id = foo.getId();
		Assume.assumeTrue(id > 0);
		// is foo still there?
		Contact fooAgain = dao.find(id);
		Assume.assumeNotNull(fooAgain);

		assertTrue( dao.delete(id) );
		assertEquals( "after deleting the id should be zero", 0L, foo.getId() );
	}

}
