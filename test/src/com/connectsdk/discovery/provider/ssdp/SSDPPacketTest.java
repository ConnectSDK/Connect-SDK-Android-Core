package com.connectsdk.discovery.provider.ssdp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.DatagramPacket;
import java.util.Map;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class SSDPPacketTest {

	DatagramPacket mDatagramPacket;
	SSDPPacket ssdpPacket;

	public SSDPPacketTest() {
		super();
	}

	@Before
	public void setUp(){
		String testDatagramData = "";
		mDatagramPacket = new DatagramPacket(testDatagramData.getBytes(), 0);
	}
	

	@Test
	public void testParseDatagram() {
		String testDatagramData =
				"NOTIFY * HTTP/1.1\r\n" +
				"HOST: 239.255.255.250:1900\r\n" +
				"NT: nt_value\r\n" +
				"NTS: ssdp:byebye\r\n" +
				"USN: uuid:advertisement_UUID\r\n\r\n";
		mDatagramPacket = new DatagramPacket(testDatagramData.getBytes(), 0);
		ssdpPacket = new SSDPPacket(mDatagramPacket);
		Assert.assertEquals("NOTIFY * HTTP/1.1", ssdpPacket.getType());
		Assert.assertEquals("239.255.255.250:1900", ssdpPacket.getData().get("HOST"));
		Assert.assertEquals("nt_value", ssdpPacket.getData().get("NT"));
		Assert.assertEquals("ssdp:byebye", ssdpPacket.getData().get("NTS"));
		Assert.assertEquals("uuid:advertisement_UUID", ssdpPacket.getData().get("USN"));
	}


	@Test
	public void testParseLowercaseDatagram() {
		String testDatagramData =
				"NOTIFY * HTTP/1.1\r\n" +
				"host: 239.255.255.250:1900\r\n" +
				"nt: nt_value\r\n" +
				"Nts: ssdp:byebye\r\n" +
				"uSN: uuid:advertisement_UUID\r\n\r\n";
		mDatagramPacket = new DatagramPacket(testDatagramData.getBytes(), 0);
		ssdpPacket = new SSDPPacket(mDatagramPacket);
		Assert.assertEquals("NOTIFY * HTTP/1.1", ssdpPacket.getType());
		Assert.assertEquals("239.255.255.250:1900", ssdpPacket.getData().get("HOST"));
		Assert.assertEquals("nt_value", ssdpPacket.getData().get("NT"));
		Assert.assertEquals("ssdp:byebye", ssdpPacket.getData().get("NTS"));
		Assert.assertEquals("uuid:advertisement_UUID", ssdpPacket.getData().get("USN"));
	}


	@Test
	public void testParseEmptyDatagram() {
		String testDatagramData = "Unknown";
		mDatagramPacket = new DatagramPacket(testDatagramData.getBytes(), 0);
		ssdpPacket = new SSDPPacket(mDatagramPacket);
		Assert.assertNull(ssdpPacket.getType());
		Assert.assertTrue(ssdpPacket.getData().isEmpty());
	}

}
