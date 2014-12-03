package com.connectsdk.core.upnp.ssdp;

import java.net.DatagramPacket;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.util.Log;

import com.connectsdk.core.upnp.ssdp.SSDP.ParsedDatagram;
//import android.util.Log;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class SSDPTest{
	
	public SSDPTest() {
		super();
	}

	DatagramPacket mDatagramPacket;
	String testDatagramData = "NOTIFY * HTTP/1.1\nCONNECTSDK: positive test\n"
			+ "SDK: negative test\n";
	ParsedDatagram pd;
	
	@Test
	public void testParseDatagram() {
		 mDatagramPacket = new DatagramPacket(testDatagramData.getBytes(), 0);
		 pd = new ParsedDatagram(mDatagramPacket);
		Assert.assertEquals("positive test", pd.data.get("CONNECTSDK").toString());
		Assert.assertFalse("negative test".equals(pd.data.get("CONNECTSDK").toString()));
		Log.d("Test", pd.data.get("CONNECTSDK").toString());
	}

	
}
