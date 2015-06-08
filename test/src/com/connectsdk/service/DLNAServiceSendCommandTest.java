/*
 * DLNAServiceSendCommandTest
 * Connect SDK
 *
 * Copyright (c) 2015 LG Electronics.
 * Created by Oleksii Frolov on 14 May 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connectsdk.service;

import com.connectsdk.core.TestUtil;
import com.connectsdk.etc.helper.HttpConnection;
import com.connectsdk.etc.helper.HttpMessage;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommand;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DLNAServiceSendCommandTest {

    public static final String COMMAND_URL = "http://host:8080/path";

    StubDLNAService service;

    HttpConnection httpConnection;

    class StubDLNAService extends DLNAService {

        String connectionTarget;

        public StubDLNAService(ServiceDescription serviceDescription, ServiceConfig serviceConfig) {
            super(serviceDescription, serviceConfig);
        }

        @Override
        HttpConnection createHttpConnection(String target) throws IOException {
            this.connectionTarget = target;
            return httpConnection;
        }
    }

    @Before
    public void setUp() {
        httpConnection = Mockito.mock(HttpConnection.class);
        service = new StubDLNAService(Mockito.mock(ServiceDescription.class),
                Mockito.mock(ServiceConfig.class));
    }

    @Test
    public void testSendSimplePostCommand() throws Exception {
        Object payload = DLNAService.AV_TRANSPORT_URN;

        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        ServiceCommand<ResponseListener<Object>> command =
                new ServiceCommand<ResponseListener<Object>>(service, COMMAND_URL, payload, listener);
        service.avTransportURL = COMMAND_URL;

        service.sendCommand(command);
        TestUtil.runUtilBackgroundTasks();

        Assert.assertEquals(COMMAND_URL, service.connectionTarget);

        Mockito.verify(httpConnection, Mockito.times(1)).setMethod(Mockito.eq(HttpConnection.Method.POST));
        Mockito.verify(httpConnection, Mockito.times(1)).setPayload(Mockito.eq(payload.toString()));
        Mockito.verify(httpConnection, Mockito.times(1)).execute();
    }

    @Test
    public void testSendPostCommandWithNullPayload() throws Exception {
        Object payload = null;

        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        ServiceCommand<ResponseListener<Object>> command =
                new ServiceCommand<ResponseListener<Object>>(service, COMMAND_URL, payload, listener);
        service.avTransportURL = COMMAND_URL;

        service.sendCommand(command);
        TestUtil.runUtilBackgroundTasks();
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        Mockito.verify(httpConnection, Mockito.times(0)).execute();
        Mockito.verify(listener).onError(Mockito.any(ServiceCommandError.class));
    }

    @Test
    public void testSendPostCommandWithWrongPayload() throws Exception {
        Object payload = "payload";

        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        ServiceCommand<ResponseListener<Object>> command =
                new ServiceCommand<ResponseListener<Object>>(service, COMMAND_URL, payload, listener);
        service.avTransportURL = COMMAND_URL;

        service.sendCommand(command);
        TestUtil.runUtilBackgroundTasks();
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        Mockito.verify(httpConnection, Mockito.times(0)).execute();
        Mockito.verify(listener).onError(Mockito.any(ServiceCommandError.class));
    }

}
