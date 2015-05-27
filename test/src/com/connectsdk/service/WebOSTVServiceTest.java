/*
 * WebOSTVServiceTest
 * Connect SDK
 *
 * Copyright (c) 2015 LG Electronics.
 * Created by Oleksii Frolov on 27 May 2015
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

import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.PlaylistControl;
import com.connectsdk.service.capability.WebAppLauncher;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.NotSupportedServiceCommandError;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class WebOSTVServiceTest {

    private WebOSTVService service;
    private ServiceDescription serviceDescription;

    @Before
    public void setUp() {
        serviceDescription = Mockito.mock(ServiceDescription.class);
        Mockito.when(serviceDescription.getVersion()).thenReturn("5.0.0");
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Server", Arrays.asList("server server"));
        Mockito.when(serviceDescription.getResponseHeaders()).thenReturn(headers);
        service = new WebOSTVService(serviceDescription, Mockito.mock(ServiceConfig.class));
    }

    @Test
    public void testCapabilitiesForVersion5() {
        Mockito.when(serviceDescription.getVersion()).thenReturn("5.0.0");
        Assert.assertTrue(service.hasCapabilities(
                PlaylistControl.JumpToTrack,
                PlaylistControl.Next,
                PlaylistControl.Previous,
                WebAppLauncher.Any,
                MediaControl.Any));
    }

    @Test
    public void testCapabilitiesForVersion4() {
        Mockito.when(serviceDescription.getVersion()).thenReturn("4.0.0");
        Assert.assertTrue(service.hasCapabilities(
                MediaControl.Play,
                MediaControl.Pause,
                MediaControl.Stop,
                MediaControl.Seek,
                MediaControl.Position,
                MediaControl.Duration,
                MediaControl.PlayState,
                WebAppLauncher.Close,
                WebAppLauncher.Launch,
                WebAppLauncher.Launch_Params));
    }

    @Test
    public void testJumpToTrack() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        service.jumpToTrack(1, listener);
        Mockito.verify(listener).onError(Mockito.isA(NotSupportedServiceCommandError.class));
    }

    @Test
    public void testNext() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        service.next(listener);
        Mockito.verify(listener).onError(Mockito.isA(NotSupportedServiceCommandError.class));
    }

    @Test
    public void testPrevious() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        service.previous(listener);
        Mockito.verify(listener).onError(Mockito.isA(NotSupportedServiceCommandError.class));
    }
}
