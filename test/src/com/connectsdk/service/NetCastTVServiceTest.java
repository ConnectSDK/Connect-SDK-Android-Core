package com.connectsdk.service;

import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NetCastTVServiceTest {

    private NetcastTVService service;

    private ServiceDescription serviceDescription;

    private ServiceConfig serviceConfig;

    @Before
    public void setUp() {
        serviceDescription = Mockito.mock(ServiceDescription.class);
        serviceConfig = Mockito.mock(ServiceConfig.class);
        service = new NetcastTVService(serviceDescription, serviceConfig);
    }

    @Test
    public void testDecToHex() {
        Assert.assertEquals("0000000000000010", service.decToHex("16"));
    }

    @Test
    public void testDecToHexWithNullArgument() {
        Assert.assertEquals(null, service.decToHex(null));
    }

    @Test
    public void testDecToHexWithEmptyArgument() {
        Assert.assertEquals(null, service.decToHex(""));
    }

    @Test
    public void testDecToHexWithWrongArgument() {
        Assert.assertEquals(null, service.decToHex("Not a number"));
    }

    @Test
    public void testDecToHexWithWrongCharactersArgument() {
        Assert.assertEquals("0000000000000010", service.decToHex(" 16\r\n"));
    }
}
