package com.topq.vm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsystem.extensions.analyzers.text.FindText;
import jsystem.extensions.analyzers.text.GetTextCounter;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;
import jsystem.utils.MiscUtils;

import com.topq.vm.PerlToolkit.VmControlOperations;

public class VM extends SystemObjectImpl {

	public PerlToolkit perlToolkit;
	public VirtualCenter virtualCenter;
	public PowerCli powerCli;

	/**
	 * VM name on the VM server
	 */
	private String vmName;

	private String dnsName;
	private String sshDnsName;
	private String defaultSnapshot;
	private String ESXHost;
	private String dataCenter = "";
	private String resourcePool;
	private String dataStore = "";
	private String vmxFile = ""; // "VMcenter.vmx";
	private String portGroup;

	private String ip = null;

	@Override
	public void init() throws Exception {
		super.init();
	}

	public void add(String host, String template, String customization, String dataStore) throws Exception {
		// Check if VM exists
		if (!exists()) {
			powerCli.executeAddScript(virtualCenter, getVmName(), host, template, customization, dataStore);
		}
	}

	public void remove() throws Exception {
		// Check if VM exists
		if (exists()) {
			powerOff();
			powerCli.executeRemoveScript(virtualCenter, getVmName());
		}
	}

	public void powerOn() throws Exception {
		if (!isUp()) {
			perlToolkit.vmControl(true, virtualCenter, getVmName(), VmControlOperations.poweron);
		}
		waitForUpAndRunning();
	}

	public void powerOff() throws Exception {
		if (isUp()) {
			perlToolkit.vmControl(true, virtualCenter, getVmName(), VmControlOperations.poweroff);
		}
		waitForDown();
	}

	public void shutdownVM() throws Exception {
		if (isUp()) {
			perlToolkit.vmControl(true, virtualCenter, getVmName(), VmControlOperations.shutdown);
		}
		waitForDown();
	}

	public void resetVM() throws Exception {
		if (isUp()) {
			perlToolkit.vmControl(true, virtualCenter, getVmName(), VmControlOperations.reset);
		} else {
			powerOn();
		}
		waitForUpAndRunning();
	}

	public void rebootVM() throws Exception {
		if (isUp()) {
			perlToolkit.vmControl(true, virtualCenter, getVmName(), VmControlOperations.reboot);
		} else {
			powerOn();
		}
		waitForUpAndRunning();
	}

	public void suspend() throws Exception {
		if (isUp()) {
			perlToolkit.vmControl(true, virtualCenter, getVmName(), VmControlOperations.suspend);
		}
		waitForDown(120);
	}

	public void standbyVM() throws Exception {
		if (isUp()) {
			perlToolkit.vmControl(true, virtualCenter, getVmName(), VmControlOperations.standby);
		}
		waitForDown(120);
	}

	public String getIp() throws Exception {
		if (ip == null) {
			ip = waitForVmIp(120000);
		}
		return ip;
	}

	public String getVmIp() throws Exception {
		getVmInfo();
		try {
			GetTextCounter textCounter = new GetTextCounter("IP Address");
			analyze(textCounter);
			return textCounter.getCounter();
		} catch (Throwable e) {
			return "Not Known";
		}
	}

	public String waitForVmIp(long timeout) throws Exception {
		long startTime = System.currentTimeMillis();
		report.startLevel("Wait for VM IP");
		String ip = getVmIp();
		while (ip.equals("Not Known") && (System.currentTimeMillis() - startTime < timeout)) {
			ip = getVmIp();
		}
		report.report("VM " + getVmName() + " IP is " + ip, !ip.equals("Not Known"));
		report.stopLevel();
		return ip;
	}

	public void waitForUpAndRunning() throws Exception {
		waitForUpAndRunning(600);
	}

	public void waitForUpAndRunning(int timeout) throws Exception {
		long startTime = System.currentTimeMillis();
		boolean up = false;
		report.startLevel("Wait for VM to get Up and Running (VMware Tools is running and the version is current)");
		while (!up && (System.currentTimeMillis() - startTime < timeout * 1000)) {
			up = isUpAndRunning();
		}
		report.report("VM " + getVmName() + " is " + (up ? "up and running" : "down"), up);
		report.stopLevel();
	}

	public void waitForDown() throws Exception {
		waitForDown(120);
	}

	public void waitForDown(int timeout) throws Exception {
		long startTime = System.currentTimeMillis();
		boolean down = false;
		while (!down && (System.currentTimeMillis() - startTime < timeout * 1000)) {
			down = isDown();
		}
		report.report("VM " + getVmName() + " is " + (down ? "down" : "up and running"), down);
	}

	public boolean isUpAndRunning() throws Exception {
		if (isUp()) {
			// Note that we don't care if VM tools are current or not
			return getTestAgainstObject().toString().contains("VMware Tools is running");
		}
		return false;
	}

	public void makeSureExists() throws Exception {
		if (!exists()) {
			throw new Exception("VM " + getVmName() + " does net existss");
		}
	}

	public boolean exists() throws Exception {
		// Search for the !message "No Virtual Machine Found With Name 'machine mane'"
		return !getVmGuestInfo().contains("No Virtual Machine Found");
	}

	public boolean isUp() throws Exception {
		// Search for the message "For display, Virtual Machine 'machine mane' under host IP should be powered ON"
		return !getVmGuestInfo().contains("powered ON");
	}

	public boolean isDown() throws Exception {
		return !isUp();
	}

	public String getVmGuestInfo() throws Exception {
		perlToolkit.executeScript(true, "vm\\guestinfo.pl", virtualCenter, getVmName(), "--operation", "display");
		setTestAgainstObject(perlToolkit.getTestAgainstObject());
		return getTestAgainstObject().toString();
	}

	/*
	 * Not reviewed yet...
	 */

	public void cloneVM(String VMClone_Name) throws Exception {
		perlToolkit.executeScript(true, "vm\\vmclone.pl", "--username", virtualCenter.getUsername(), "--password",
				virtualCenter.getPassword(), "--vmhost", virtualCenter.getServer(), "--vmname", getVmName(), "--vmname_destination",
				"VMClone_Name"
		// "--url","https://"+virtualCenter.getServerName()+":"+"443"+"/sdk/webService"
		// //requires vCenter (~2000$ min)
				);
	}

	public void registerVm() throws Exception {
		perlToolkit.executeScript(true, "vm\\vmregister.pl", "--server", virtualCenter.getServer(), "--username",
				virtualCenter.getUsername(), "--password", virtualCenter.getPassword(), "--vmname", getVmName(), "--operation", "register",
				"--hostname", getESXHost(), "--vmxpath", "[" + getDataStore() + "]" + getVmName() + "/" + vmxFile, "--pool",
				getResourcePool(), "--datacenter", getDataCenter());
	}

	public void unRegisterVm() throws Exception {
		perlToolkit.executeScript(true, "vm\\vmregister.pl", "--server", virtualCenter.getServer(), "--username",
				virtualCenter.getUsername(), "--password", virtualCenter.getPassword(), "--vmname", getVmName(), "--operation",
				"unregister");
	}

	public void goToSnapshot(String snapshotName) throws Exception {
		perlToolkit.executeScript(true, "vm\\snapshotmanager.pl", "--server", virtualCenter.getServer(), "--username",
				virtualCenter.getUsername(), "--password", virtualCenter.getPassword(), "--vmname", getVmName(), "--operation", "goto",
				"--snapshotname", snapshotName);
		FindText snapshotRes = new FindText("Revert To Snapshot " + snapshotName + " For Virtual Machin.+completed sucessfully", true);
		perlToolkit.analyze(snapshotRes);
	}

	public void createSnapshot(String snapshotName) throws Exception {
		perlToolkit.executeScript(true, "vm\\snapshotmanager.pl", "--server", virtualCenter.getServer(), "--username",
				virtualCenter.getUsername(), "--password", virtualCenter.getPassword(), "--vmname", getVmName(), "--operation", "create",
				"--snapshotname", snapshotName);
		FindText snapshotRes = new FindText("Snapshot " + snapshotName + " for virtual machine.+created sucessfully", true);
		perlToolkit.analyze(snapshotRes);
	}

	public void addHardDiskToVM(int size) throws Exception {
		perlToolkit.executeScript(true, "vm\\vdiskcreate.pl", "--server", virtualCenter.getServer(), "--username",
				virtualCenter.getUsername(), "--password", virtualCenter.getPassword(), "--vmname", getVmName(), "--filename", getVmName(),
				"--disksize", Integer.toString(size));
	}

	public void getVmInfo() throws Exception {
		perlToolkit.executeScript(true, "vm\\vminfo.pl", "--server", virtualCenter.getServer(), "--username", virtualCenter.getUsername(),
				"--password", virtualCenter.getPassword(), "--vmname", getVmName());
		setTestAgainstObject(perlToolkit.getTestAgainstObject());
		if (getTestAgainstObject().toString().contains("Error")) {
			throw new Exception("Failed to get VM information");
		}
	}

	public void vmMigrate(String dstEsxHost, PowerState powerState) throws Exception {
		boolean isPowered;
		if (powerState == PowerState.ON) {
			isPowered = true;
		} else {
			isPowered = false;
		}
		if (getESXHost() == null) {
			updateVmESXHost(isPowered);
		}
		report.report("Starting migration of " + getVmName() + " from ESX host " + ESXHost + " to ESX host " + dstEsxHost);
		perlToolkit.executeScript(true, "vm\\vmmigrate.pl", "--server", virtualCenter.getServer(), "--username",
				virtualCenter.getUsername(), "--password", virtualCenter.getPassword(), "--vmname", getVmName(), "--targetdatastore",
				dataStore, "--targetpool", resourcePool, "--priority", "highPriority", "--state", powerState.getValue(), "--sourcehost",
				ESXHost, "--targetServerName", dstEsxHost);
		if (perlToolkit.isAnalyzeSuccess(new FindText("Guest Info for the Virtual Machine '" + getVmName() + "' under host " + dstEsxHost))) {
			report.report("Virtual Machine '" + getVmName() + "' is under host " + dstEsxHost, true);
		} else if (perlToolkit.isAnalyzeSuccess(new FindText("Virtual Machine " + getVmName() + " sucessfully migrated to host "
				+ dstEsxHost))) {
			report.report("Migrated " + getVmName() + " from ESX host " + getESXHost() + " to ESX host " + dstEsxHost, true);
		} else {
			report.report("Failed to migrate " + getVmName() + " from ESX host " + getESXHost() + " to ESX host " + dstEsxHost, false);
		}
		setTestAgainstObject(perlToolkit.getTestAgainstObject());
		updateVmESXHost(isPowered);
	}

	public void updateVmESXHost(boolean isPowered) throws Exception {
		String ESXHost = "";
		getVmGuestInfo();
		// Guest Info for the Virtual Machine 'QA-LX-01' under host 10.55.1.11
		Pattern p = Pattern.compile("(.*)" + "host(.*)");
		Matcher m = p.matcher(this.getTestAgainstObject().toString());
		if (m.find()) {
			ESXHost = m.group(2);
		} else {
			report.report("Failed to update " + getVmName() + " hosting ESX host", Reporter.FAIL);
		}
		if (!isPowered) {
			ESXHost = ESXHost.substring(0, ESXHost.indexOf(" should be powered ON"));
		}
		// Matcher m = p.matcher(this.getTestAgainstObject().toString());
		ESXHost = ESXHost.trim();
		setESXHost(ESXHost);
		report.report("Updated " + getVmName() + " hosting ESX host to " + getESXHost(), Reporter.PASS);
	}

	public void destroyVM() throws Exception {
		perlToolkit.executeScript(true, "vm\\vmcontrol.pl", "--server", virtualCenter.getServer(), "--username",
				virtualCenter.getUsername(), "--password", virtualCenter.getPassword(), "--vmname", getVmName(), "--operation", "poweroff");
		setTestAgainstObject(perlToolkit.getTestAgainstObject());
		unRegisterVm();
		// TBD storage....
		// storage.cliCommand("sudo rm -rf " + storage.vmfs +
		// center.getVmName());
	}

	public String ping() {
		return MiscUtils.ping(getVmName());
	}

	public String getVmName() {
		return vmName;
	}

	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	public String getDnsName() {
		return dnsName;
	}

	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	public String getDefaultSnapshot() {
		return defaultSnapshot;
	}

	public void setDefaultSnapshot(String defaultSnapshot) {
		this.defaultSnapshot = defaultSnapshot;
	}

	public String getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(String dataCenter) {
		this.dataCenter = dataCenter;
	}

	public String getResourcePool() {
		return resourcePool;
	}

	public void setResourcePool(String resourcePool) {
		this.resourcePool = resourcePool;
	}

	public String getDataStore() {
		return dataStore;
	}

	public void setDataStore(String dataStore) {
		this.dataStore = dataStore;
	}

	public String getESXHost() {
		return ESXHost;
	}

	public void setESXHost(String host) {
		ESXHost = host;
	}

	public String getPortGroup() {
		return portGroup;
	}

	public void setPortGroup(String portGroup) {
		this.portGroup = portGroup;
	}

	public String getSshDnsName() {
		return sshDnsName;
	}

	public void setSshDnsName(String sshDnsName) {
		this.sshDnsName = sshDnsName;
	}

	public enum PowerState {
		ON("poweredOn"), OFF("poweredOff"), SUSPENDED("suspended");

		private String value;

		PowerState(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
