package com.connectsdk.discovery;

public class DiscoveryFilter {
    String serviceId = null;
    String serviceFilter = null;
    Object option = null;

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
    
    public Object getOption() {
        return option;
    }
    
    public void setOption(Object option) {
        this.option = option;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiscoveryFilter that = (DiscoveryFilter) o;

        if (serviceFilter != null ? !serviceFilter.equals(that.serviceFilter) : that.serviceFilter != null)
            return false;
        if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null)
            return false;
        if (option != null ? !option.equals(that.option) : that.option != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceId != null ? serviceId.hashCode() : 0;
        result = 31 * result + (serviceFilter != null ? serviceFilter.hashCode() : 0);
        result = 31 * result + (option != null ? option.hashCode() : 0);
        return result;
    }
}
