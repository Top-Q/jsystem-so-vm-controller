package com.topq.vm;

import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import junit.framework.SystemTestCase4;

import org.junit.Before;
import org.junit.Test;

/**
 * @author uqa
 * 
 */
public class VmOperations extends SystemTestCase4 {

	private String vmName = "auto-yoram-situator-client";
	private String host = "172.18.24.43";
	private String template = "WIN_2008_32BIT_TMP";
	private String customization = "Microsoft Windows 2008 32bit";
	private String dataStore = "DS Netapp Automation - Lun10";
	private String snapshotName;
	private String cloneName;

	private VM vm;

	@Before
	public void getVM() throws Exception {
		vm = (VM) system.getSystemObject("vm");
		vm.setVmName(vmName);
	}

	@Test
	@TestProperties(name = "Deploy new VM from template", paramsInclude = { "vmName", "host", "template", "customization", "dataStore" })
	public void addVM() throws Exception {
		vm.add(getHost(), getTemplate(), getCustomization(), getDataStore());
	}

	@Test
	@TestProperties(name = "Remove VM", paramsInclude = { "vmName" })
	public void removeVM() throws Exception {
		vm.remove();
	}

	@Test
	@TestProperties(name = "Power On VM", paramsInclude = { "vmName" })
	public void powerOnVM() throws Exception {
		vm.makeSureExists();
		vm.powerOn();
	}

	@Test
	@TestProperties(name = "Power Off VM", paramsInclude = { "vmName" })
	public void powerOffVM() throws Exception {
		vm.makeSureExists();
		vm.powerOff();
	}

	@Test
	@TestProperties(name = "Shutdown VM", paramsInclude = { "vmName" })
	public void shutdownVM() throws Exception {
		vm.shutdownVM();
	}

	@Test
	@TestProperties(name = "Reset VM", paramsInclude = { "vmName" })
	public void resetVM() throws Exception {
		vm.resetVM();
	}

	@Test
	@TestProperties(name = "Reboot VM", paramsInclude = { "vmName" })
	public void rebootVM() throws Exception {
		vm.rebootVM();
	}

	@Test
	@TestProperties(name = "Suspend VM", paramsInclude = { "vmName" })
	public void suspendVM() throws Exception {
		vm.suspend();
	}

	@Test
	@TestProperties(name = "Standby VM", paramsInclude = { "vmName" })
	public void standbyVM() throws Exception {
		vm.standbyVM();
	}

	@Test
	@TestProperties(name = "Standby VM", paramsInclude = { "vmName" })
	public void getIp() throws Exception {
		vm.powerOn();
		vm.getIp();
	}

	@Test
	@TestProperties(name = "Clone VM", paramsInclude = { "vmName", "cloneName" })
	public void cloneVM() throws Exception {
		vm.setVmName(vmName);
		if (!vm.isUp()) {
			vm.cloneVM(cloneName);
		}
	}

	@Test
	@TestProperties(name = "Create Snapshot", paramsInclude = { "vmName", "snapshotName" })
	public void createSnapshot() throws Exception {
		vm.setVmName(vmName);
		vm.createSnapshot(snapshotName);
	}

	@Test
	@TestProperties(name = "Go To Snapshot", paramsInclude = { "vmName", "snapshotName" })
	public void goToSnapshot() throws Exception {
		vm.setVmName(vmName);
		vm.goToSnapshot(snapshotName);
	}

	/*
	 * Standard Getters and Setters
	 */

	public String getVmName() {
		return vmName;
	}

	@ParameterProperties(description = "Virtual Machine name", section = "General")
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	public String getSnapshotName() {
		return snapshotName;
	}

	@ParameterProperties(description = "Snapshot name", section = "General")
	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}

	public String getTemplate() {
		return template;
	}

	@ParameterProperties(description = "Template name", section = "General")
	public void setTemplate(String template) {
		this.template = template;
	}

	public String getCloneName() {
		return cloneName;
	}

	@ParameterProperties(description = "Clone name", section = "General")
	public void setCloneName(String cloneName) {
		this.cloneName = cloneName;
	}

	public String getCustomization() {
		return customization;
	}

	@ParameterProperties(description = "Customization Specification name", section = "General")
	public void setCustomization(String customization) {
		this.customization = customization;
	}

	public String getDataStore() {
		return dataStore;
	}

	@ParameterProperties(description = "DataStore name", section = "General")
	public void setDataStore(String dataStore) {
		this.dataStore = dataStore;
	}

	public String getHost() {
		return host;
	}

	@ParameterProperties(description = "Specific Host within the Server name", section = "General")
	public void setHost(String host) {
		this.host = host;
	}

}
