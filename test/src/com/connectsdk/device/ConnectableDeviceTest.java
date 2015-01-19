package com.connectsdk.device;

import com.connectsdk.service.DeviceService;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.ExternalInputControl;
import com.connectsdk.service.capability.KeyControl;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.MouseControl;
import com.connectsdk.service.capability.PlaylistControl;
import com.connectsdk.service.capability.PowerControl;
import com.connectsdk.service.capability.TVControl;
import com.connectsdk.service.capability.TextInputControl;
import com.connectsdk.service.capability.ToastControl;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.capability.WebAppLauncher;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectableDeviceTest {

	private ConnectableDevice device;

	@Before
	public void setUp() {
		device = new ConnectableDevice();
	}

	@Test
	public void testHasCapabilityWithEmptyServices() {
		Assert.assertFalse(device.hasCapability(MediaPlayer.Display_Image));
	}

	@Test
	public void testHasCapabilityWithServices() {
		DeviceService service = Mockito.mock(DeviceService.class);
		Mockito.when(service.hasCapability(MediaPlayer.Display_Image)).thenReturn(Boolean.TRUE);
		device.services.put("service", service);
		Assert.assertTrue(device.hasCapability(MediaPlayer.Display_Image));
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

	@Test
	public void testGetLauncher() {
		DeviceService service;


		service = Mockito.mock(DeviceService.class);
		Launcher launcherLow = Mockito.mock(Launcher.class);
		Mockito.when(service.getAPI(Launcher.class)).thenReturn(launcherLow);
		Mockito.when(launcherLow.getLauncherCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		Launcher launcherHigh = Mockito.mock(Launcher.class);
		Mockito.when(service.getAPI(Launcher.class)).thenReturn(launcherHigh);
		Mockito.when(launcherHigh.getLauncherCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);

		Assert.assertNotNull(device.getLauncher());
		Assert.assertSame(launcherHigh, device.getLauncher());
	}

	@Test
	public void testGetMediaPlayer() {
		DeviceService service;


		service = Mockito.mock(DeviceService.class);
		MediaPlayer controllerLow = Mockito.mock(MediaPlayer.class);
		Mockito.when(service.getAPI(MediaPlayer.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getMediaPlayerCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		MediaPlayer controllerHigh = Mockito.mock(MediaPlayer.class);
		Mockito.when(service.getAPI(MediaPlayer.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getMediaPlayerCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);


		Assert.assertNotNull(device.getMediaPlayer());
		Assert.assertSame(controllerHigh, device.getMediaPlayer());
	}

	@Test
	public void testGetMediaControl() {
		DeviceService service;


		service = Mockito.mock(DeviceService.class);
		MediaControl controllerLow = Mockito.mock(MediaControl.class);
		Mockito.when(service.getAPI(MediaControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getMediaControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		MediaControl controllerHigh = Mockito.mock(MediaControl.class);
		Mockito.when(service.getAPI(MediaControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getMediaControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);


		Assert.assertNotNull(device.getMediaControl());
		Assert.assertSame(controllerHigh, device.getMediaControl());
	}

	@Test
	public void testGetPlaylistControl() {
		DeviceService service;

		service = Mockito.mock(DeviceService.class);
		PlaylistControl controllerLow = Mockito.mock(PlaylistControl.class);
		Mockito.when(service.getAPI(PlaylistControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getPlaylistControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		PlaylistControl controllerHigh = Mockito.mock(PlaylistControl.class);
		Mockito.when(service.getAPI(PlaylistControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getPlaylistControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);


		Assert.assertNotNull(device.getPlaylistControl());
		Assert.assertSame(controllerHigh, device.getPlaylistControl());
	}

	@Test
	public void testGetVolumeControl() {
		DeviceService service;


		service = Mockito.mock(DeviceService.class);
		VolumeControl controllerLow = Mockito.mock(VolumeControl.class);
		Mockito.when(service.getAPI(VolumeControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getVolumeControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		VolumeControl controllerHigh = Mockito.mock(VolumeControl.class);
		Mockito.when(service.getAPI(VolumeControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getVolumeControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);


		Assert.assertNotNull(device.getVolumeControl());
		Assert.assertSame(controllerHigh, device.getVolumeControl());
	}

	@Test
	public void testGetWebAppLauncher() {
		DeviceService service;


		service = Mockito.mock(DeviceService.class);
		WebAppLauncher controllerLow = Mockito.mock(WebAppLauncher.class);
		Mockito.when(service.getAPI(WebAppLauncher.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getWebAppLauncherCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		WebAppLauncher controllerHigh = Mockito.mock(WebAppLauncher.class);
		Mockito.when(service.getAPI(WebAppLauncher.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getWebAppLauncherCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);

		Assert.assertNotNull(device.getWebAppLauncher());
		Assert.assertSame(controllerHigh, device.getWebAppLauncher());
	}

	@Test
	public void testGetTVControl() {
		DeviceService service;


		service = Mockito.mock(DeviceService.class);
		TVControl controllerLow = Mockito.mock(TVControl.class);
		Mockito.when(service.getAPI(TVControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getTVControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		TVControl controllerHigh = Mockito.mock(TVControl.class);
		Mockito.when(service.getAPI(TVControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getTVControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);

		Assert.assertNotNull(device.getTVControl());
		Assert.assertSame(controllerHigh, device.getTVControl());
	}

	@Test
	public void testGetToastControl() {
		DeviceService service;

		service = Mockito.mock(DeviceService.class);
		ToastControl controllerLow = Mockito.mock(ToastControl.class);
		Mockito.when(service.getAPI(ToastControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getToastControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		ToastControl controllerHigh = Mockito.mock(ToastControl.class);
		Mockito.when(service.getAPI(ToastControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getToastControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);


		Assert.assertNotNull(device.getToastControl());
		Assert.assertSame(controllerHigh, device.getToastControl());
	}

	@Test
	public void testGetTextInputControl() {
		DeviceService service;

		service = Mockito.mock(DeviceService.class);
		TextInputControl controllerLow = Mockito.mock(TextInputControl.class);
		Mockito.when(service.getAPI(TextInputControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getTextInputControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		TextInputControl controllerHigh = Mockito.mock(TextInputControl.class);
		Mockito.when(service.getAPI(TextInputControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getTextInputControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);


		Assert.assertNotNull(device.getTextInputControl());
		Assert.assertSame(controllerHigh, device.getTextInputControl());
	}

	@Test
	public void testGetMouseControl() {
		DeviceService service;


		service = Mockito.mock(DeviceService.class);
		MouseControl controllerLow = Mockito.mock(MouseControl.class);
		Mockito.when(service.getAPI(MouseControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getMouseControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		MouseControl controllerHigh = Mockito.mock(MouseControl.class);
		Mockito.when(service.getAPI(MouseControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getMouseControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);

		Assert.assertNotNull(device.getMouseControl());
		Assert.assertSame(controllerHigh, device.getMouseControl());
	}

	@Test
	public void testGetExternalInputControlControl() {
		DeviceService service;

		service = Mockito.mock(DeviceService.class);
		ExternalInputControl controllerLow = Mockito.mock(ExternalInputControl.class);
		Mockito.when(service.getAPI(ExternalInputControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getExternalInputControlPriorityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		ExternalInputControl controllerHigh = Mockito.mock(ExternalInputControl.class);
		Mockito.when(service.getAPI(ExternalInputControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getExternalInputControlPriorityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);


		Assert.assertNotNull(device.getExternalInputControl());
		Assert.assertSame(controllerHigh, device.getExternalInputControl());
	}

	@Test
	public void testGetPowerControl() {
		DeviceService service;

		service = Mockito.mock(DeviceService.class);
		PowerControl controllerLow = Mockito.mock(PowerControl.class);
		Mockito.when(service.getAPI(PowerControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getPowerControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		PowerControl controllerHigh = Mockito.mock(PowerControl.class);
		Mockito.when(service.getAPI(PowerControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getPowerControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);


		Assert.assertNotNull(device.getPowerControl());
		Assert.assertSame(controllerHigh, device.getPowerControl());
	}

	@Test
	public void testGetKeyControl() {
		DeviceService service;

		service = Mockito.mock(DeviceService.class);
		KeyControl controllerLow = Mockito.mock(KeyControl.class);
		Mockito.when(service.getAPI(KeyControl.class)).thenReturn(controllerLow);
		Mockito.when(controllerLow.getKeyControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.LOW);
		device.services.put("low", service);

		service = Mockito.mock(DeviceService.class);
		KeyControl controllerHigh = Mockito.mock(KeyControl.class);
		Mockito.when(service.getAPI(KeyControl.class)).thenReturn(controllerHigh);
		Mockito.when(controllerHigh.getKeyControlCapabilityLevel()).thenReturn(CapabilityMethods.CapabilityPriorityLevel.HIGH);
		device.services.put("high", service);

		Assert.assertNotNull(device.getKeyControl());
		Assert.assertSame(controllerHigh, device.getKeyControl());
	}
}
