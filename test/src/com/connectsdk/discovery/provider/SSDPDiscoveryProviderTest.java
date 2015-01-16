package com.connectsdk.discovery.provider;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Context;

import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.discovery.provider.ssdp.SSDPClient;
import com.connectsdk.shadow.WifiInfoShadow;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE,shadows={WifiInfoShadow.class})
public class SSDPDiscoveryProviderTest{

	Context context;
	InetAddress localAddress;
	SSDPDiscoveryProvider dp;
	MulticastSocket mLocalSocket;
	private SSDPClient ssdpClient = Mockito.mock(SSDPClient.class);
	
	class StubSSDPDiscoveryProvider extends SSDPDiscoveryProvider {

		public StubSSDPDiscoveryProvider(Context context) {
			super(context);
			
		}
		
		@Override
		protected SSDPClient createSocket(InetAddress source) throws IOException {
			return ssdpClient;
		}

	};
	
	@Before
	public void setUp() throws Exception {
		byte[] data = new byte[1];
		when(ssdpClient.responseReceive()).thenReturn(new DatagramPacket(data, 1));
		when(ssdpClient.multicastReceive()).thenReturn(new DatagramPacket(data, 1));
		dp = new StubSSDPDiscoveryProvider(Robolectric.application);
		assertNotNull(dp);
	}
	@After
	public void tearDown() throws Exception {		
		dp.stop();
	}
	

	@Test
	public void testStart() throws JSONException, InterruptedException, IOException{
		//Test Desc. : Test to verify if the socket is created and is not null also sendSearch().
				
		dp.start();
		//assert that after start() SSDPClient was created.
		Assert.assertTrue(ssdpClient != null);

		//verify after socket create , sendSearch() is successful.
		DiscoveryFilter filter = new DiscoveryFilter("DLNA", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(filter);
		String msg = SSDPClient.getSSDPSearchMessage(filter.getServiceFilter());
		
		Thread.sleep(4000);
		dp.stop();
		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(ssdpClient, Mockito.times(3)).send(argument.capture());
		Assert.assertEquals(msg, new String(argument.getValue()));
					
	}
	
	@Test
	public void testStop() throws JSONException, InterruptedException, IOException{
		//Test Desc. : Test to verify if the sendSearch is stopped then the dataGramSocket is disconnected and closed.
		
		dp.start();
		dp.stop();		
		verify(ssdpClient, Mockito.times(1)).close();
	}
	
	@Test
	public void testAddDeviceFilter() throws JSONException {
		//Test Desc. : Test to verify if the deviceFilter is added properly.
		
		DiscoveryFilter filter = new DiscoveryFilter("DLNA", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.addDeviceFilter(filter);
		Assert.assertTrue(dp.serviceFilters.contains(filter));		
	}
	
	@Test
	public void testRemoveDeviceFilters() throws JSONException {
		//Test Desc. : Test to verify if the deviceFilter is removed properly.
		
		DiscoveryFilter filter = new DiscoveryFilter("DLNA", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(filter);		
		dp.removeDeviceFilter(filter);		
		Assert.assertFalse(dp.serviceFilters.contains(filter));
		
	}
	
	@Test
	public void testIsEmpty() throws JSONException {
		//Test Desc.: Verify if the serviceFilters is empty prior to calling the scheduled timer task start() which adds the searchTarget as filter into the ServiceFilters.
		
		DiscoveryFilter filter = new DiscoveryFilter("DLNA", "urn:schemas-upnp-org:device:MediaRenderer:1");
		Assert.assertTrue(dp.isEmpty());		
		dp.serviceFilters.add(filter);		
		Assert.assertFalse(dp.isEmpty());
	}
	
	@Test
	public void testGetLocationData() {
		//Test Desc. : Test to verify if the GetLocation works as expected.
		
		String location = "http://10.194.183.124:1874/";
		String uuid = "0f574021-141a-ebe8-eeac-bcf7b973615a";
		String serviceFilter = "urn:lge-com:service:webos-second-screen:1";
		
		dp.getLocationData(location, uuid, serviceFilter);
		
		// ConcurrentHashMap<String, ServiceDescription> foundService = dp.getFoundServices();		
		// Have to implement
		Assert.assertTrue(true);
		
	}
	
	@Test
	public void testServiceIdsForFilter() throws JSONException {
		//Test Desc. : Verify if SSDPDiscoveryProvider. serviceIdForFilter returns the serviceId for the specified filter added in ServiceFilters list.
		
		DiscoveryFilter filter = new DiscoveryFilter("DLNA", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(filter);		
		ArrayList<String> expectedResult = new ArrayList<String>();
		expectedResult.add(filter.getServiceId());		
		Assert.assertEquals(expectedResult, dp.serviceIdsForFilter(filter.getServiceFilter()));
		
	}
	
	@Test
	public void testIsSearchingForFilter() throws JSONException {
		//Test Desc. : Verify if SSDPDiscoveryProvider. IsSearchingForFilter returns the expected result.
		
		DiscoveryFilter filter = new DiscoveryFilter("DLNA", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(filter);		
		Assert.assertTrue(dp.isSearchingForFilter(filter.getServiceFilter()));
		
	}
	
	@Test
	public void testReset() throws IOException{
		//Test Desc. : Verify if JmdnsRegistry reset the services found for SSDPDiscoveryProvider.
		
		
		Assert.assertTrue(dp.foundServices.isEmpty());
		dp.start();
		Assert.assertTrue(dp.foundServices.isEmpty());
		
		dp.reset();
		Assert.assertTrue(dp.foundServices.isEmpty());
		
	}


}
