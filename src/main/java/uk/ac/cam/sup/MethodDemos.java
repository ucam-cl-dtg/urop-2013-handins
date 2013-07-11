package uk.ac.cam.sup;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import uk.ac.cam.sup.controllers.BinController;

import com.googlecode.htmleasy.HtmleasyProviders;
import uk.ac.cam.sup.controllers.TestController;
import uk.ac.cam.sup.controllers.SubmissionController;

public class MethodDemos extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> myServices = new HashSet<Class<?>>();
		
		myServices.add(BinController.class);
        myServices.add(TestController.class);
        myServices.add(SubmissionController.class);
		
		// Add Htmleasy Providers
		myServices.addAll(HtmleasyProviders.getClasses());
		
		return myServices;
	}
}
