package havis.middleware.reader.llrp.osgi;

import havis.middleware.ale.reader.ReaderConnector;
import havis.middleware.reader.llrp.LLRPReaderConnector;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	Logger log = Logger.getLogger(Activator.class.getName());

	private static final String NAME = "name";
	private final static String[] VALUES = new String[] { "LLRP", "RF-R300" };

	private final List<ServiceRegistration<?>> registrations = new ArrayList<>(VALUES.length);

	@Override
	public void start(BundleContext context) throws Exception {
		for (String value : VALUES) {
			Dictionary<String, String> properties = new Hashtable<>();
			properties.put(NAME, value);

			log.log(Level.FINE, "Register prototype service factory {0} (''{1}'': ''{2}'')", new Object[] { LLRPReaderConnector.class.getName(), NAME, value });
			registrations.add(context.registerService(ReaderConnector.class.getName(), new PrototypeServiceFactory<ReaderConnector>() {
				@Override
				public ReaderConnector getService(Bundle bundle, ServiceRegistration<ReaderConnector> registration) {
					return new LLRPReaderConnector();
				}

				@Override
				public void ungetService(Bundle bundle, ServiceRegistration<ReaderConnector> registration, ReaderConnector service) {
				}
			}, properties));
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		for (ServiceRegistration<?> registration : registrations)
			registration.unregister();
		registrations.clear();
	}
}