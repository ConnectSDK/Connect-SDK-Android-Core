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

import java.security.Key;
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


}
