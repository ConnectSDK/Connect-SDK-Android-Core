package com.connectsdk.service.upnp;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class DLNAEventParser {
	private static final String ns = null;
	
	public JSONObject parse(InputStream in) throws XmlPullParserException, IOException, JSONException {
    	try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readEvent(parser);
        } finally {
            in.close();
        }
	}
	
	private JSONObject readEvent(XmlPullParser parser) throws IOException, XmlPullParserException, JSONException {
		JSONObject event = new JSONObject();
		
	    parser.require(XmlPullParser.START_TAG, ns, "Event");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        if (name.equals("InstanceID")) {
	        	event.put("Event", readInstanceId(parser));
	        }
	        else {
	        	skip(parser);
	        }
	    }
	    return event;
	}
	
	private JSONObject readInstanceId(XmlPullParser parser) throws IOException, XmlPullParserException, JSONException {
		JSONObject instanceId = new JSONObject();
		
	    parser.require(XmlPullParser.START_TAG, ns, "InstanceID");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();

	        instanceId.put(name, readAttributeValue(name, parser));
	    }
	    return instanceId;
	}
	
	private String readAttributeValue(String target, XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, target);
	    String value = parser.getAttributeValue(null, "val");
        parser.nextTag();
	    parser.require(XmlPullParser.END_TAG, ns, target);
	    return value;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
}
