package com.connectsdk.service.webos;


import com.connectsdk.service.WebOSTVService;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommand;
import com.connectsdk.service.command.ServiceCommandError;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.URI;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
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
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        ServiceCommand command = new ServiceCommand(null, null, (Object)null, listener);
        socketClient.requests.put(11, command);
        socketClient.state = WebOSTVServiceSocketClient.State.REGISTERED;
        socketClient.onMessage(" {\"type\":\"error\",\"id\":\"11\",\"error\":" +
                "\"403 access denied\",\"payload\":{}}");
        Assert.assertEquals(WebOSTVServiceSocketClient.State.REGISTERED, socketClient.getState());
        Mockito.verify(listener).onError(Mockito.any(ServiceCommandError.class));
    }
}
