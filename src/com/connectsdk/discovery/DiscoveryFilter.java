package com.connectsdk.discovery;

public class DiscoveryFilter {
	String serviceId;
	String serviceFilter;
	String modelName;
	
	public DiscoveryFilter(String serviceId, String serviceFilter) {
		this.serviceId = serviceId;
		this.serviceFilter = serviceFilter;
	}

	public DiscoveryFilter(String serviceId, String serviceFilter, String modelName) {
		this(serviceId, serviceFilter);
		this.modelName = modelName;
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

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public boolean contains(String filter, String modelName) {
		return (serviceFilter.equals(filter) 
				&& (this.modelName == null || (modelName != null && modelName.contains(this.modelName))));
	}
}
