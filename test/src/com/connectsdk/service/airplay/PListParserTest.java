package com.connectsdk.service.airplay;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by oleksii.frolov on 3/19/2015.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class PListParserTest {

    @Test
    public void testSimplePlistParsing() throws JSONException, XmlPullParserException, IOException {
        String rawString = "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "\t<key>duration</key>\n" +
                "\t<real>52.209000000000003</real>\n" +
                "\t<key>loadedTimeRanges</key>\n" +
                "\t<array>\n" +
                "\t\t<dict>\n" +
                "\t\t\t<key>duration</key>\n" +
                "\t\t\t<real>52.209000000000003</real>\n" +
                "\t\t\t<key>start</key>\n" +
                "\t\t\t<real>0.0</real>\n" +
                "\t\t</dict>\n" +
                "\t</array>\n" +
                "\t<key>playbackBufferEmpty</key>\n" +
                "\t<true/>\n" +
                "\t<key>playbackBufferFull</key>\n" +
                "\t<false/>\n" +
                "\t<key>playbackLikelyToKeepUp</key>\n" +
                "\t<true/>\n" +
                "\t<key>position</key>\n" +
                "\t<real>4.6505421629999999</real>\n" +
                "\t<key>rate</key>\n" +
                "\t<real>1</real>\n" +
                "\t<key>readyToPlay</key>\n" +
                "\t<true/>\n" +
                "\t<key>seekableTimeRanges</key>\n" +
                "\t<array>\n" +
                "\t\t<dict>\n" +
                "\t\t\t<key>duration</key>\n" +
                "\t\t\t<real>52.209000000000003</real>\n" +
                "\t\t\t<key>start</key>\n" +
                "\t\t\t<real>0.0</real>\n" +
                "\t\t</dict>\n" +
                "\t</array>\n" +
                "\t<key>stallCount</key>\n" +
                "\t<integer>0</integer>\n" +
                "\t<key>uuid</key>\n" +
                "\t<string>D6E86A89-82F0-41F5-B680-B27AB83656F6-25-0000000E81A3E1CF</string>\n" +
                "</dict>\n" +
                "</plist>\n";

        JSONObject json = new PListParser().parse(rawString);
        Assert.assertTrue(json.has("duration"));
        Assert.assertTrue(json.has("loadedTimeRanges"));
        Assert.assertTrue(json.has("playbackBufferEmpty"));
        Assert.assertTrue(json.has("playbackBufferFull"));
        Assert.assertTrue(json.has("playbackLikelyToKeepUp"));
        Assert.assertTrue(json.has("position"));
        Assert.assertTrue(json.has("rate"));
        Assert.assertTrue(json.has("readyToPlay"));
        Assert.assertTrue(json.has("seekableTimeRanges"));
        Assert.assertTrue(json.has("uuid"));
        Assert.assertTrue(json.has("rate"));
        Assert.assertTrue(json.getJSONArray("seekableTimeRanges").getJSONObject(0).has("start"));

        Assert.assertEquals(1, json.getInt("rate"));
        Assert.assertEquals("D6E86A89-82F0-41F5-B680-B27AB83656F6-25-0000000E81A3E1CF", json.getString("uuid"));
    }
}
