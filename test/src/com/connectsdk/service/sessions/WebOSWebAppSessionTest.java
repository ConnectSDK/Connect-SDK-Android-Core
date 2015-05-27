/*
 * WebOSWebAppSessionTest
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
package com.connectsdk.service.sessions;

import com.connectsdk.service.DeviceService;
import com.connectsdk.service.WebOSTVService;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.webos.WebOSTVServiceSocketClient;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class WebOSWebAppSessionTest {

    private WebOSWebAppSession session;
    private LaunchSession launchSession;
    private DeviceService service;
    private WebOSTVServiceSocketClient socket;

    @Before
    public void setUp() {
        socket = Mockito.mock(WebOSTVServiceSocketClient.class);
        launchSession = Mockito.mock(LaunchSession.class);
        service = Mockito.mock(WebOSTVService.class);
        session = new WebOSWebAppSession(launchSession, service);
        session.setConnected(Boolean.TRUE);
        session.socket = socket;
    }

    @Test
    public void testPrevious() throws JSONException {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        session.previous(listener);

        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        ArgumentCaptor<JSONObject> argPacket = ArgumentCaptor.forClass(JSONObject.class);
        ArgumentCaptor<JSONObject> argPayload = ArgumentCaptor.forClass(JSONObject.class);
        Mockito.verify(socket).sendMessage(argPacket.capture(), argPayload.capture());
        Mockito.verify(listener).onSuccess(null);

        JSONObject packet = argPacket.getValue();
        JSONObject payload = argPayload.getValue();
        Assert.assertNull(payload);
        Assert.assertTrue(packet.has("payload"));
        Assert.assertEquals("playPrevious", packet.getJSONObject("payload")
                .getJSONObject("mediaCommand").getString("type"));
        Assert.assertEquals("connectsdk.mediaCommand", packet.getJSONObject("payload")
                .getString  ("contentType"));
    }

    @Test
    public void testNext() throws JSONException {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        session.next(listener);

        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        ArgumentCaptor<JSONObject> argPacket = ArgumentCaptor.forClass(JSONObject.class);
        ArgumentCaptor<JSONObject> argPayload = ArgumentCaptor.forClass(JSONObject.class);
        Mockito.verify(socket).sendMessage(argPacket.capture(), argPayload.capture());
        Mockito.verify(listener).onSuccess(null);

        JSONObject packet = argPacket.getValue();
        JSONObject payload = argPayload.getValue();
        Assert.assertNull(payload);
        Assert.assertTrue(packet.has("payload"));
        Assert.assertEquals("playNext", packet.getJSONObject("payload")
                .getJSONObject("mediaCommand").getString("type"));
        Assert.assertEquals("connectsdk.mediaCommand", packet.getJSONObject("payload")
                .getString("contentType"));
    }

    @Test
    public void testJumpToTrack() throws JSONException {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        session.jumpToTrack(7, listener);

        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        ArgumentCaptor<JSONObject> argPacket = ArgumentCaptor.forClass(JSONObject.class);
        ArgumentCaptor<JSONObject> argPayload = ArgumentCaptor.forClass(JSONObject.class);
        Mockito.verify(socket).sendMessage(argPacket.capture(), argPayload.capture());
        Mockito.verify(listener).onSuccess(null);

        JSONObject packet = argPacket.getValue();
        JSONObject payload = argPayload.getValue();
        Assert.assertNull(payload);
        Assert.assertTrue(packet.has("payload"));
        Assert.assertEquals("jumpToTrack", packet.getJSONObject("payload")
                .getJSONObject("mediaCommand").getString("type"));
        Assert.assertEquals(7, packet.getJSONObject("payload")
                .getJSONObject("mediaCommand").getInt("index"));
        Assert.assertEquals("connectsdk.mediaCommand", packet.getJSONObject("payload")
                .getString("contentType"));
    }

    @Test
    public void testGetPlaylistControl() {
        Assert.assertSame(session, session.getPlaylistControl());
    }

    @Test
    public void testGetPlaylistControlCapability() {
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.HIGH,
                session.getPlaylistControlCapabilityLevel());
    }
}
