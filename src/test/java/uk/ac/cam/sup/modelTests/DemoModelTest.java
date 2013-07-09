package uk.ac.cam.sup.modelTests;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DemoModelTest {
	private SessionFactory sessionFactory;
	
	@Before
	public void setUp() throws Exception {
		// Set up the sessionFactory for database transactions
        sessionFactory = new Configuration()
                .configure() // configures settings from hibernate.cfg.xml
                .buildSessionFactory();
	}
	  
	@After
	public void tearDown() throws Exception{
		//Close the session if it exists
		if(sessionFactory != null) {
			sessionFactory.close();	
		}
	}
	
	@Test
	public void testController() {
		//Run tests

	}
	
	
}