package com.connectsdk.service.webos;


import com.connectsdk.BuildConfig;
import com.connectsdk.service.WebOSTVService;
import com.connectsdk.service.command.ServiceCommand;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.net.URI;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class WebOSTVServiceSocketClientTest {

    private WebOSTVServiceSocketClient socketClient;

    @Before
    public void setUp() {
        WebOSTVService service = Mockito.mock(WebOSTVService.class);
        URI uri = URI.create("http://127.0.0.1/");
        socketClient = new WebOSTVServiceSocketClient(service, uri);
    }

    @Test
    public void test403ErrorShouldNotCloseSocket() {
        ServiceCommand command = new ServiceCommand(null, null, (Object)null, null);
        WebOSTVServiceSocketClient spySocketClient = Mockito.spy(socketClient);

        spySocketClient.requests.put(11, command);
        spySocketClient.state = WebOSTVServiceSocketClient.State.REGISTERED;
        spySocketClient.onMessage(" {\"type\":\"error\",\"id\":\"11\",\"error\":" +
                "\"403 access denied\",\"payload\":{}}");

        Assert.assertEquals(WebOSTVServiceSocketClient.State.REGISTERED, spySocketClient.getState());
        Mockito.verify(spySocketClient, Mockito.times(0)).close();
    }
}
