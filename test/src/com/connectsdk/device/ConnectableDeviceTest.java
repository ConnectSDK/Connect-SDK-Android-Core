package com.connectsdk.device;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.connectsdk.service.DeviceService;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.MediaPlayer;

public class ConnectableDeviceTest {

	private ConnectableDevice device;

	@Before
	public void setUp() {
		device = new ConnectableDevice();
	}
	
	@Test
	public void testHasCapabilityWithEmptyServices() {
		Assert.assertFalse(device.hasCapability(MediaPlayer.Display_Video));
	}
	
	@Test
	public void testHasCapabilityWithServices() {
		DeviceService service = Mockito.mock(DeviceService.class);
		Mockito.when(service.hasCapability(MediaPlayer.Display_Video)).thenReturn(Boolean.TRUE);
		device.services.put("service", service);
		Assert.assertTrue(device.hasCapability(MediaPlayer.Display_Video));
	}
	
	@Test
	public void testHasAnyCapabilities() {
		DeviceService service = Mockito.mock(DeviceService.class);
		String[] capabilities = {Launcher.Browser, Launcher.YouTube};
		Mockito.when(service.hasAnyCapability(capabilities)).thenReturn(Boolean.TRUE);
		device.services.put("service", service);
		Assert.assertTrue(device.hasAnyCapability(capabilities));
	}
	
	@Test
	public void testHasAnyCapabilitiesWithoutServices() {
		DeviceService service = Mockito.mock(DeviceService.class);
		String[] capabilities = {Launcher.Browser, Launcher.YouTube};
		Mockito.when(service.hasAnyCapability(capabilities)).thenReturn(Boolean.FALSE);
		device.services.put("service", service);
		Assert.assertFalse(device.hasAnyCapability(capabilities));
	}
	
	@Test
	public void testHasCapabilities() {
		DeviceService service = Mockito.mock(DeviceService.class);
		Mockito.when(service.hasCapability(Launcher.Browser)).thenReturn(Boolean.TRUE);
		Mockito.when(service.hasCapability(Launcher.YouTube)).thenReturn(Boolean.TRUE);
		device.services.put("service", service);
		
		List<String> capabilities = new ArrayList<String>();
		capabilities.add(Launcher.Browser);
		capabilities.add(Launcher.YouTube);
		
		Assert.assertTrue(device.hasCapabilities(capabilities));
	}
	
}
