package com.oracle.psr.workerClasses;

public class ComputeVM {

	private String customerName, domainName, VMLabel, vmOrchestration, vmType, vmZone, vmLogicalUid, vmNamePrefix, vmID, vmShortName;

	public String getVmShortName() {
		return vmShortName;
	}

	public void setVmShortName(String vmShortName) {
		this.vmShortName = vmShortName;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getVmID() {
		return vmID;
	}

	public void setVmID(String vmID) {
		this.vmID = vmID;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getVMLabel() {
		return VMLabel;
	}

	public void setVMLabel(String vMLabel) {
		VMLabel = vMLabel;
	}

	public String getVmOrchestration() {
		return vmOrchestration;
	}

	public void setVmOrchestration(String vmOrchestration) {
		this.vmOrchestration = vmOrchestration;
	}

	public String getVmType() {
		return vmType;
	}

	public void setVmType(String vmType) {
		this.vmType = vmType;
	}

	public String getVmZone() {
		return vmZone;
	}

	public void setVmZone(String vmZone) {
		this.vmZone = vmZone;
	}

	public String getVmLogicalUid() {
		return vmLogicalUid;
	}

	public void setVmLogicalUid(String vmLogicalUid) {
		this.vmLogicalUid = vmLogicalUid;
	}

	public String getVmNamePrefix() {
		return vmNamePrefix;
	}

	public void setVmNamePrefix(String vmNamePrefix) {
		this.vmNamePrefix = vmNamePrefix;
	}

	@Override
	public String toString() {
		return "ComputeVM [customerName=" + customerName + ", domainName=" + domainName + ", VMLabel=" + VMLabel
				+ ", vmOrchestration=" + vmOrchestration + ", vmType=" + vmType + ", vmZone=" + vmZone
				+ ", vmLogicalUid=" + vmLogicalUid + ", vmNamePrefix=" + vmNamePrefix + ", vmID=" + vmID + "]";
	} 
	
}
