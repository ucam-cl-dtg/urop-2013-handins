package uk.ac.cam.sup.controllers;

import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import uk.ac.cam.sup.models.Answer;

// Import the following for logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import the following for hibernate requests
import uk.ac.cam.sup.HibernateSessionRequestFilter;
import org.hibernate.Session;

// Import the following for raven AND hibernate
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;


@Path("/")
public class MainController {

	/*
	 * The request will automatically be given the value of the current request
	 * when using the @Context tag. You need the request for getting a hibernate
	 * session and also for getting the current user, which is passed as a session
	 * attribute (the session being contained in the request).
	 */
	@Context
	HttpServletRequest request;

	/* 
	 * You could also declare the following in a method locally; you might as well
	 * have one logger for the entire class. Make sure it is static if you do.  
	 */
	private static Logger log = LoggerFactory.getLogger(MainController.class);
	
	
	@GET
	@Path("/")
	@ViewWith("/soy/main.index")
	public Map demo() {
		// See the hibernateDemo()-method for how to use hibernate
		List allDemoModels = hibernateDemo(); 
		
		// See the logDemo()-method for how to log stuff
		logDemo();
		
		/*
		 * The ravenDemo will only retrieve the CRSID. The actual authentication
		 * works by setting a filter in web.xml (see web.xml in webapp/WEB-INF!).
		 * Note that you'll have to put the raven filter BEFORE the easyhtml filter
		 * in the web.xml file.
		 * Download all the files necessary for the raven authentication from:
		 * https://github.com/ucam-cl-dtg/ucam-webauth/tree/master/src/main/java
		 */
		String CRSID = ravenDemo();
		
		
		return ImmutableMap.of("userID", CRSID, "data", allDemoModels);
	}

	public List hibernateDemo() {
		// This will store some data in the database first.
		
		// Get the session for all db transfers by passing the current request to the
		// openSession() method in HibernateSessionRequestfilter. 
		Session s = HibernateSessionRequestFilter.openSession(request);
		s.beginTransaction();
		s.save(new Answer("model1"));
		s.save(new Answer("some name"));
		s.save(new Answer("Bob :)"));
		s.getTransaction().commit();
		s.close();
		
		// To retrieve data from the db with hibernate you do the same as always.
		s = HibernateSessionRequestFilter.openSession(request);
		s.beginTransaction();
		List result = s.createQuery("from DemoModel").list();
		s.getTransaction().commit();
		s.close();
		
		return result;
	}
	
	public void logDemo() {
		/*
		 * If you have a Logger-object it is easy to log with different levels.
		 * Some of the levels are: debug, info, warn, error.
		 * See example for logging below.
		 */
		
		log.debug("This will print out a debug-message into the log");
		log.info("Use this for printing info-messages to the log");
		log.warn("This one for warning...");
		log.error("You can see logging is easy...");
		
	}

	public String ravenDemo() {
		// This will extract the CRSID of the current user and return it:
		return (String) request.getSession().getAttribute("RavenRemoteUser");
	}

}
