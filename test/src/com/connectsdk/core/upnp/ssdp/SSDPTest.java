package com.connectsdk.core.upnp.ssdp;

import java.net.DatagramPacket;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.connectsdk.core.upnp.ssdp.SSDP.ParsedDatagram;
//import android.util.Log;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class SSDPTest{
	
	public SSDPTest() {
		super();
	}

	@Before
	public void setUp(){
		mDatagramPacket = new DatagramPacket(testDatagramData.getBytes(), 0);
		
	}
	
	DatagramPacket mDatagramPacket;
	String testDatagramData = "NOTIFY * HTTP/1.1\nCONNECTSDK: positive test\n"
			+ "SDK: negative test\n";
	ParsedDatagram pd;
	
	@Test
	public void testParseDatagramPositiveresult() {		 
		 pd = new ParsedDatagram(mDatagramPacket);
		Assert.assertEquals("positive test", pd.data.get("CONNECTSDK").toString());
				
	}
	
	@Test
	public void testParseDatagramNegativeResult() {
		 pd = new ParsedDatagram(mDatagramPacket);
		Assert.assertFalse("negative test".equals(pd.data.get("CONNECTSDK").toString()));
		
	}

	
}
