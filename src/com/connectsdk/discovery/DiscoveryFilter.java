package com.connectsdk.discovery;

public class DiscoveryFilter {
	String serviceId = null;
	String serviceFilter = null;
	
	public DiscoveryFilter(String serviceId, String serviceFilter) {
		this.serviceId = serviceId;
		this.serviceFilter = serviceFilter;
	}

	public String getServiceId() {
		return serviceId;
	}
	
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	public String getServiceFilter() {
		return serviceFilter;
	}
	
	public void setServiceFilter(String serviceFilter) {
		this.serviceFilter = serviceFilter;
	}
}
