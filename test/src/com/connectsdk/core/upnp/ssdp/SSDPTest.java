package com.connectsdk.core.upnp.ssdp;

import java.net.DatagramPacket;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.connectsdk.discovery.provider.ssdp.SSDPPacket;

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
	SSDPPacket ssdpPacket;
	
	@Test
	public void testParseDatagramPositiveresult() {		 
		ssdpPacket = new SSDPPacket(mDatagramPacket);
		Assert.assertEquals("positive test", ssdpPacket.getData().get("CONNECTSDK").toString());
	}
	
	@Test
	public void testParseDatagramNegativeResult() {
		ssdpPacket = new SSDPPacket(mDatagramPacket);
		Assert.assertFalse("negative test".equals(ssdpPacket.getData().get("CONNECTSDK").toString()));
	}
}
