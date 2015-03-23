package com.connectsdk.service;

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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by oleksii.frolov on 1/13/2015.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class DLNAServiceTest {

    DLNAService service;

    @Before
    public void setUp() {
        service = new DLNAService(Mockito.mock(ServiceDescription.class), Mockito.mock(ServiceConfig.class), Robolectric.application);
    }

    @Test
    public void testParseData() {
        String tag = "TrackDuration";
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "   <SOAP-ENV:Body>\n" +
                "      <m:GetPositionInfoResponse xmlns:m=\"urn:schemas-upnp-org:service:AVTransport:1\">\n" +
                "         <Track xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"ui4\">1</Track>\n" +
                "         <TrackDuration xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"string\">0:00:52</TrackDuration>\n" +
                "         <TrackMetaData xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"string\">&lt;DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"&gt;&lt;item id=\"1000\" parentID=\"0\" restricted=\"0\"&gt;&lt;dc:title&gt;Sintel Trailer&lt;/dc:title&gt;&lt;dc:description&gt;Blender Open Movie Project&lt;/dc:description&gt;&lt;res protocolInfo=\"http-get:*:video/mp4:DLNA.ORG_OP=01\"&gt;http://ec2-54-201-108-205.us-west-2.compute.amazonaws.com/samples/media/video.mp4&lt;/res&gt;&lt;upnp:albumArtURI&gt;http://ec2-54-201-108-205.us-west-2.compute.amazonaws.com/samples/media/videoIcon.jpg&lt;/upnp:albumArtURI&gt;&lt;upnp:class&gt;object.item.videoItem&lt;/upnp:class&gt;&lt;/item&gt;&lt;/DIDL-Lite&gt;</TrackMetaData>\n" +
                "         <TrackURI xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"string\">http://ec2-54-201-108-205.us-west-2.compute.amazonaws.com/samples/media/video.mp4</TrackURI>\n" +
                "         <RelTime xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"string\">0:00:00</RelTime>\n" +
                "         <AbsTime xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"string\">NOT_IMPLEMENTED</AbsTime>\n" +
                "         <RelCount xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"i4\">2147483647</RelCount>\n" +
                "         <AbsCount xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"i4\">2147483647</AbsCount>\n" +
                "      </m:GetPositionInfoResponse>\n" +
                "   </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
        String value = service.parseData(response, tag);
        Assert.assertEquals("0:00:52", value);
    }

    @Test
    public void testParseDataWithError() {
        String tag = "errorCode";
        String response = "<?xml version=\"1.0\"?>\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><SOAP-ENV:Body><SOAP-ENV:Fault><faultcode>SOAP-ENV:Client</faultcode><faultstring>UPnPError</faultstring><detail><u:UPnPError xmlns:u=\"urn:schemas-upnp-org:control-1-0\"><u:errorCode>402</u:errorCode><u:errorDescription>Invalid Args</u:errorDescription></u:UPnPError></detail></SOAP-ENV:Fault></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        String value = service.parseData(response, tag);
        Assert.assertEquals("402", value);
    }

    @Test
    public void testParseData3Symbols() {
        String tag = "errorCode";
        String response = "&lt";
        String value = null;
        try {
            value = service.parseData(response, tag);
        } catch (Exception e) {
            Assert.fail("exception thrown: " + e);
        }
        Assert.assertEquals("", value);
    }

    @Test
    public void testGetMetadata() throws Exception {
        String title = "<title>";
        String description = "description";
        String mime = "audio/mpeg";
        String mediaURL = "http://host.com/media";
        String iconURL = "http://host.com/icon";

        String expectedXml =
                "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">" +
                "<item id=\"1000\" parentID=\"0\" restricted=\"0\">" +
                "<dc:title>&lt;title&gt;</dc:title>" +
                "<dc:description>" + description + "</dc:description>" +
                "<res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_OP=01\">" + mediaURL + "</res>" +
                "<upnp:albumArtURI>" + iconURL + "</upnp:albumArtURI>" +
                "<upnp:class>object.item.audioItem</upnp:class>" +
                "</item>" +
                "</DIDL-Lite>";

        String fragment = service.getMetadata(mediaURL, mime, title, description, iconURL);
        Assert.assertEquals(expectedXml, fragment);
    }


    @Test
    public void testGetMessageXml() throws Exception {
        String method = "GetPosition";
        String serviceURN = "http://serviceurn/";

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
        "<s:Body>" +
        "<u:" + method + " xmlns:u=\"" + serviceURN + "\">" +
        "<key>value</key>" +
        "</u:" + method + ">" +
        "</s:Body>" +
        "</s:Envelope>";

        Map<String, String> params = new HashMap<String, String>();
        params.put("key", "value");
        String source = service.getMessageXml(serviceURN, method, null, params);
        Assert.assertEquals(expectedXml, (source));
    }

    @Test
    public void testGetMessageXmlWithMetadata() throws Exception {
        String method = "SetAVTransportUri";
        String serviceURN = "http://serviceurn/";

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:" + method + " xmlns:u=\"" + serviceURN + "\">" +
                "<CurrentURIMetaData>" +

                "&lt;DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\"&gt;" +
                "&lt;item id=\"1000\" parentID=\"0\" restricted=\"0\"&gt;" +
                "&lt;dc:title&gt;&amp;amp;\"title\"&lt;/dc:title&gt;" +
                "&lt;dc:description&gt;&amp;amp;&lt;/dc:description&gt;" +
                "&lt;res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_OP=01\"&gt;http://url/t&amp;amp;t&lt;/res&gt;" +
                "&lt;upnp:albumArtURI&gt;http://host/image&lt;/upnp:albumArtURI&gt;" +
                "&lt;upnp:class&gt;object.item.audioItem&lt;/upnp:class&gt;" +
                "&lt;/item&gt;" +
                "&lt;/DIDL-Lite&gt;" +

                "</CurrentURIMetaData>" +
                "</u:" + method + ">" +
                "</s:Body>" +
                "</s:Envelope>";

        String metadata = service.getMetadata("http://url/t&t", "audio/mpeg", "&\"title\"", "&", "http://host/image");
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("CurrentURIMetaData", metadata);

        String source = service.getMessageXml(serviceURN, method, null, params);
        Assert.assertEquals(expectedXml, (source));
    }

    @Test
    public void testUrlEncode() throws Exception {
        String expected = "http://192.168.1.100:8000/ph&o't'o%20with%20symbols.jpg";
        String urlStr = "http://192.168.1.100:8000/ph&o't'o with symbols.jpg";
        Assert.assertEquals(expected, service.encodeURL(urlStr));
    }

    @Test
    public void testUrlEncodeAlreadyEncoded() throws Exception {
        String expected = "http://192.168.1.100:8000/ph&o't'o%20with%20symbols.jpg";
        String urlStr = "http://192.168.1.100:8000/ph&o't'o%20with%20symbols.jpg";
        Assert.assertEquals(expected, service.encodeURL(urlStr));
    }

}
