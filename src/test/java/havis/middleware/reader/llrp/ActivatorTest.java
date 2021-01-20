package havis.middleware.reader.llrp;

import havis.middleware.ale.reader.ReaderConnector;
import havis.middleware.reader.llrp.osgi.Activator;

import java.util.Dictionary;
import java.util.Hashtable;

import mockit.Mocked;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;

public class ActivatorTest {

	@Test
	public void activatorTest(@Mocked final BundleContext context) throws Exception {
		final Dictionary<String, String> properties1 = new Hashtable<>();
		properties1.put("name", "LLRP");
		final Dictionary<String, String> properties2 = new Hashtable<>();
		properties2.put("name", "RF-R300");

		Activator activator = new Activator();
		activator.start(context);

		new Verifications() {
			{
				PrototypeServiceFactory<ReaderConnector> factory1, factory2;

				context.registerService(ReaderConnector.class.getName(), factory1 = withCapture(), properties1);
				times = 1;

				context.registerService(ReaderConnector.class.getName(), factory2 = withCapture(), properties2);
				times = 1;
				
				Assert.assertNotNull(factory1.getService(null, null));
				factory1.ungetService(null, null, null);
				
				Assert.assertNotNull(factory2.getService(null, null));
				factory2.ungetService(null, null, null);
			}
		};

		activator.stop(context);
	}
}