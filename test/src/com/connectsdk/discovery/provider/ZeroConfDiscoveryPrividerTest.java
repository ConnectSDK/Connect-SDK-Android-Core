package com.connectsdk.discovery.provider;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.JmDNSImpl;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Context;

import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.service.config.ServiceDescription;
import com.connectsdk.shadow.WifiInfoShadow;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, shadows = { WifiInfoShadow.class })
public class ZeroConfDiscoveryPrividerTest {

	private ZeroconfDiscoveryProvider dp;
	private JmDNS mDNS;
	private ServiceEvent eventMock;

	// stub classes are used to allow mocking inside Robolectric test
	class StubJmDNS extends JmDNSImpl {

		public StubJmDNS() throws IOException {
			this(null, null);
		}

		public StubJmDNS(InetAddress address, String name) throws IOException {
			super(address, name);
		}
	}

	abstract class StubServiceEvent extends ServiceEvent {

		public StubServiceEvent(Object eventSource) {
			super(eventSource);
		}
	}

	class StubZeroConfDiscoveryProvider extends ZeroconfDiscoveryProvider {

		public StubZeroConfDiscoveryProvider(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected JmDNS createJmDNS() {
			return mDNS;
		}
	}

	@Before
	public void setUp() {
		dp = new ZeroconfDiscoveryProvider(Robolectric.application);
		mDNS = mock(StubJmDNS.class);
		eventMock = mock(StubServiceEvent.class);

		dp.jmdns = mDNS;
		dp.jmdnsListener.serviceAdded(eventMock);
	}

	@Test
	public void testStart() {

	}

	@Test
	public void testJmdnsServiceAdded() throws Exception {
		// Test Desc.: Verify when service added to the JmdnsServiceListener
		// then "service information is queried from registry with injected
		// event mock object.

		dp.start();

		verify(eventMock, atLeastOnce()).getType();
		verify(eventMock, atLeastOnce()).getName();
		verify(mDNS, timeout(100)).requestServiceInfo(eventMock.getType(),
				eventMock.getName(), 1);
	}

	@Test
	public void testAddListener() throws Exception {
		// Test Desc.: Verify ZeroConfDiscoveryProvider addListener - Adds a
		// DiscoveryProviderListener instance which is the DiscoveryManager Impl
		// to ServiceListeners List.

		DiscoveryManager listenerMock = mock(DiscoveryManager.class);

		Assert.assertFalse(dp.serviceListeners.contains(listenerMock));
		dp.addListener(listenerMock);

		Assert.assertTrue(dp.serviceListeners.contains(listenerMock));
	}

	@Test
	public void testRemoveListener() throws Exception {

		// Test Desc.: Verify ZeroConfDiscoveryProvider RemoveListener - Removes
		// a DiscoveryProviderListener instance which is the DiscoveryManager
		// Impl from ServiceListeners List.

		DiscoveryManager listenerMock = mock(DiscoveryManager.class);

		Assert.assertFalse(dp.serviceListeners.contains(listenerMock));
		dp.serviceListeners.add(listenerMock);
		Assert.assertTrue(dp.serviceListeners.contains(listenerMock));

		dp.removeListener(listenerMock);

		Assert.assertFalse(dp.serviceListeners.contains(listenerMock));
	}

	@Test
	public void testFiltersAreEmptyByDefault() throws Exception {
		// Test Desc.: Verify if the serviceFilters is empty prior to calling
		// the scheduled timer task start() which adds the searchTarget as
		// filter into the ServiceFilters.

		JSONObject parameters = new JSONObject();
		parameters.put("serviceId", "Apple TV");
		parameters.put("filter", "_testservicetype._tcp.local.");
		Assert.assertTrue(dp.isEmpty());

		dp.serviceFilters.add(parameters);

		Assert.assertFalse(dp.isEmpty());
	}

	@Test
	public void testStopZeroConfService() throws Exception {
		// Test Desc. : Verify if on stop() of ZeroConfDiscoveryProvider
		// Service, implicitly invoke the removeServiceListener() on JmDns
		// instance,

		JSONObject parameters = new JSONObject();
		parameters.put("serviceId", "Test TV");
		parameters.put("filter", "_testservicetype._tcp.local.");
		dp.serviceFilters.add(parameters);

		ServiceListener listener = dp.jmdnsListener;

		verify(mDNS, Mockito.never()).removeServiceListener(
				parameters.getString("filter"), listener);
		dp.stop();
		verify(mDNS, Mockito.times(1)).removeServiceListener(
				parameters.getString("filter"), listener);
	}

	@Test
	public void testReset() throws Exception {
		// Test Desc. : Verify if JmdnsRegistry reset the services found for
		// ZeroConfDiscoveryProvider.

		ServiceDescription serviceDesc = new ServiceDescription();
		dp.foundServices.put("service", serviceDesc);
		Assert.assertFalse(dp.foundServices.isEmpty());

		dp.reset();
		Assert.assertTrue(dp.foundServices.isEmpty());
	}

	@Test
	public void testAddDeviceFilter() throws Exception {
		// Test Desc. : Verify if ZeroConfDiscoveryProvider. AddDeviceFilter
		// adds the specified device filter to serviceFilters list.

		JSONObject parameters = new JSONObject();
		parameters.put("serviceId", "Test TV");
		parameters.put("filter", "_testservicetype._tcp.local.");

		Assert.assertFalse(dp.serviceFilters.contains(parameters));
		dp.addDeviceFilter(parameters);

		Assert.assertTrue(dp.serviceFilters.contains(parameters));
	}

	@Test
	public void testRemoveDeviceFilter() throws Exception {
		// Test Desc. : Verify if ZeroConfDiscoveryProvider. removeDeviceFilter
		// removes the entry specified device filter from to serviceFilters
		// list.

		JSONObject parameters = new JSONObject();
		parameters.put("serviceId", "Test TV");
		parameters.put("filter", "_testservicetype._tcp.local.");

		dp.serviceFilters.add(parameters);
		Assert.assertFalse(dp.serviceFilters.isEmpty());

		dp.removeDeviceFilter(parameters);

		Assert.assertTrue(dp.serviceFilters.isEmpty());
	}

	@Test
	public void testServiceIdForFilter() throws Exception {
		// Test Desc. : Verify if ZeroConfDiscoveryProvider. serviceIdForFilter
		// returns the serviceId for the specified filter added in
		// ServiceFilters list.

		JSONObject parameters = new JSONObject();
		parameters.put("serviceId", "Test TV");
		parameters.put("filter", "_testservicetype._tcp.local.");
		dp.serviceFilters.add(parameters);

		String filter = parameters.getString("filter");
		String serviceId = dp.serviceIdForFilter(filter);

		Assert.assertEquals("Test TV", serviceId);
	}

}
