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
import org.json.JSONObject;
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

import com.connectsdk.core.upnp.ssdp.SSDPSearchMsg;
import com.connectsdk.core.upnp.ssdp.SSDPSocket;
import com.connectsdk.shadow.WifiInfoShadow;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE,shadows={WifiInfoShadow.class})
public class SSDPDiscoveryProviderTest{

	Context context;
	InetAddress localAddress;
	SSDPDiscoveryProvider dp;
	MulticastSocket mLocalSocket;
	private SSDPSocket ssdpSocket = Mockito.mock(SSDPSocket.class);
	
	class StubSSDPDiscoveryProvider extends SSDPDiscoveryProvider {

		public StubSSDPDiscoveryProvider(Context context) {
			super(context);
			
		}
		
		@Override
		protected SSDPSocket createSocket(InetAddress source) throws IOException {
			return ssdpSocket;
		}

	};
	
	@Before
	public void setUp() throws Exception {
		byte[] data = new byte[1];
		when(ssdpSocket.responseReceive()).thenReturn(new DatagramPacket(data, 1));
		dp = new StubSSDPDiscoveryProvider(Robolectric.application);
		assertNotNull(dp);
	}
	
	@Test
	public void testSendSearch() throws JSONException, InterruptedException, IOException {
		//Test Desc. : Test to verify if the sendSearch scheduled properly and correct message is being passed for search.
		
		dp.start();
			
		try {
			JSONObject parameters =new JSONObject();
			parameters.put("serviceId", "DLNA");
			parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
			dp.serviceFilters.add(parameters);
			SSDPSearchMsg search = new SSDPSearchMsg(parameters.getString("filter"));
			String msg = search.toString();
			dp.sendSearch();			
			
			Thread.sleep(10);
			ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
			verify(ssdpSocket, Mockito.times(1)).send(argument.capture());
			Assert.assertEquals(msg, new String(argument.getValue()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testStart() throws JSONException, InterruptedException, IOException{
		//Test Desc. : Test to verify if the socket is created and is not null also sendSearch().
				
		dp.start();
		//assert that after start() SSDPSocket was created.
		Assert.assertTrue(ssdpSocket != null);
				
		//verify after socket create , sendSearch() is successful.
		JSONObject parameters =new JSONObject();
		parameters.put("serviceId", "DLNA");
		parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(parameters);
		SSDPSearchMsg search = new SSDPSearchMsg(parameters.getString("filter"));
		String msg = search.toString();
		
		Thread.sleep(1000);
		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(ssdpSocket, Mockito.times(1)).send(argument.capture());
		Assert.assertEquals(msg, new String(argument.getValue()));
						
	}
	
	@Test
	public void testStop() throws JSONException, InterruptedException, IOException{
		//Test Desc. : Test to verify if the sendSearch is stopped then the dataGramSocket is disconnected and closed.
		
		dp.start();
		
		//assert that after start() SSDPSocket was created.
		Assert.assertTrue(ssdpSocket != null);
				
		//verify after socket create , sendSearch() is successful.
		JSONObject parameters =new JSONObject();
		parameters.put("serviceId", "DLNA");
		parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(parameters);
		SSDPSearchMsg search = new SSDPSearchMsg(parameters.getString("filter"));
		String msg = search.toString();
		
		Thread.sleep(1000);
		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(ssdpSocket, Mockito.times(1)).send(argument.capture());
		Assert.assertEquals(msg, new String(argument.getValue()));
							
		dp.stop();		
		verify(ssdpSocket, Mockito.times(1)).close();
				
	}
	
	@Test
	public void testStopDatagram() throws JSONException, InterruptedException, IOException{
		//Test Desc. : Test to verify if the sendSearch is stopped then the dataGram Socket is disconnected and closed.
				
		dp.start();
		
		//assert that after start() SSDPSocket was created.
		Assert.assertTrue(ssdpSocket != null);
				
		//verify after socket create , sendSearch() is successful.
		JSONObject parameters =new JSONObject();
		parameters.put("serviceId", "DLNA");
		parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(parameters);
		SSDPSearchMsg search = new SSDPSearchMsg(parameters.getString("filter"));
		String msg = search.toString();
		
		Thread.sleep(1000);
		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(ssdpSocket, Mockito.times(1)).send(argument.capture());
		Assert.assertEquals(msg, new String(argument.getValue()));
				
		/*dp.getmSSDPSocket().setWildSocket(testSocket);
		
		
		SSDPSocket ssdpSocket = new SSDPSocket(localAddress);
		mLocalSocket = new MulticastSocket();
		
		testSocket.connect(localAddress, 1903);
		mLocalSocket.connect(localAddress, 1904);
		assertFalse(ssdpSocket.isConnected());*/
		
		dp.stop();		
		verify(ssdpSocket, Mockito.times(1)).close();
		
		/*verify(testSocket, Mockito.times(1)).disconnect();
		verify(testSocket, Mockito.times(1)).close();
		assertFalse(ssdpSocket.isConnected());*/
		
	}
	
	
	@Test
	public void testAddDeviceFilter() throws JSONException {
		//Test Desc. : Test to verify if the deviceFilter is added properly.
		
		JSONObject parameters =new JSONObject();
		parameters.put("serviceId", "DLNA");
		parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.addDeviceFilter(parameters);
		Assert.assertTrue(dp.serviceFilters.contains(parameters));		
	}
	
	@Test
	public void testRemoveDeviceFilters() throws JSONException {
		//Test Desc. : Test to verify if the deviceFilter is removed properly.
		
		JSONObject parameters =new JSONObject();
		parameters.put("serviceId", "DLNA");
		parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(parameters);		
		dp.removeDeviceFilter(parameters);		
		Assert.assertFalse(dp.serviceFilters.contains(parameters));
		
	}
	
	@Test
	public void testIsEmpty() throws JSONException {
		//Test Desc.: Verify if the serviceFilters is empty prior to calling the scheduled timer task start() which adds the searchTarget as filter into the ServiceFilters.
		
		JSONObject parameters =new JSONObject();
		parameters.put("serviceId", "DLNA");
		parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
		Assert.assertTrue(dp.isEmpty());		
		dp.serviceFilters.add(parameters);		
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
		
		JSONObject parameters =new JSONObject();
		parameters.put("serviceId", "DLNA");
		parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(parameters);		
		ArrayList<String> expectedResult = new ArrayList<String>();
		expectedResult.add(parameters.getString("serviceId"));		
		Assert.assertEquals(expectedResult, dp.serviceIdsForFilter(parameters.getString("filter")));
		
	}
	
	@Test
	public void testIsSearchingForFilter() throws JSONException {
		//Test Desc. : Verify if SSDPDiscoveryProvider. IsSearchingForFilter returns the expected result.
		
		JSONObject parameters =new JSONObject();
		parameters.put("serviceId", "DLNA");
		parameters.put("filter", "urn:schemas-upnp-org:device:MediaRenderer:1");
		dp.serviceFilters.add(parameters);		
		Assert.assertTrue(dp.isSearchingForFilter(parameters.getString("filter")));
		
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
