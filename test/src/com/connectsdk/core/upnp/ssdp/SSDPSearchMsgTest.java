package com.connectsdk.core.upnp.ssdp;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.util.Log;

import com.connectsdk.core.upnp.ssdp.SSDPSearchMsg;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class SSDPSearchMsgTest {

	String st = "ConnectSDK";
	String st2 = "udap: ConnectSDK";
	int mx = 5;
	String expectedResult = "M-SEARCH * HTTP/1.1\r\n" + "HOST: 239.255.255.250:1900\r\n"
			+ "MAN: \"ssdp:discover\"\r\n" + "ST: " + st + "\r\nMX: "
			+ Integer.toString(mx)+"\r\n"+""+"\r\n";
	String expectedResult2 = "M-SEARCH * HTTP/1.1\r\n" + "HOST: 239.255.255.250:1900\r\n"
			+ "MAN: \"ssdp:discover\"\r\n" + "ST: " + st2 + "\r\nMX: "
			+ Integer.toString(mx)+"\r\n"+"USER-AGENT: UDAP/2.0"+"\r\n"+"\r\n";

	
	public SSDPSearchMsgTest() {
		super();
	}

	@Test
	public void testToString() {
		
		SSDPSearchMsg msg = new SSDPSearchMsg(st);
		Log.d("TEST",msg.toString());
		
		Assert.assertEquals(expectedResult, msg.toString());
		
		msg.setmST(st2);
		Assert.assertEquals(expectedResult2, msg.toString());
		
	}

	

}
