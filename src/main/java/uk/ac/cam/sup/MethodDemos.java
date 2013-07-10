package uk.ac.cam.sup;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import uk.ac.cam.sup.controllers.BinController;
import uk.ac.cam.sup.controllers.MainController;

import com.googlecode.htmleasy.HtmleasyProviders;

public class MethodDemos extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> myServices = new HashSet<Class<?>>();
		
		myServices.add(BinController.class);
		
		// Add Htmleasy Providers
		myServices.addAll(HtmleasyProviders.getClasses());
		
		return myServices;
	}
}
