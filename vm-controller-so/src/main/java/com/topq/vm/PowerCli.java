package com.topq.vm;

import java.io.File;

import jsystem.framework.JSystemProperties;
import jsystem.framework.system.SystemObjectImpl;

import com.aqua.sysobj.conn.CliCommand;
import com.aqua.sysobj.conn.CmdConnection;

public class PowerCli extends SystemObjectImpl {

	private String pathToScripts = JSystemProperties.getCurrentTestsPath() + File.separator + "lib" + File.separator + "scripts";
	private String powerCliCommand = "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -psc \"C:\\Program Files\\VMware\\Infrastructure\\vSphere PowerCLI\\vim.psc1\" -c ";

	public static enum VmControlOperations {
		poweron, poweroff, shutdown, reset, reboot, suspend, standby
	}

	@Override
	public void init() throws Exception {
		super.init();
	}

	public String executeAddScript(VirtualCenter virtualCenter, String vm, String host, String template, String customization,
			String dataStore) throws Exception {
		return executeScript("AddVMWare.ps1", virtualCenter, vm, host, template, customization, dataStore);
	}

	public String executeRemoveScript(VirtualCenter virtualCenter, String vm) throws Exception {
		return executeScript("RemoveVMWare.ps1", virtualCenter, vm, (String[]) null);
	}

	public String executeScript(String scriptName, VirtualCenter virtualCenter, String vm, String... params) throws Exception {

		String commandS = powerCliCommand + "\"" + pathToScripts + File.separator + scriptName + "\"";
		commandS += " " + virtualCenter.getServer();
		commandS += " " + virtualCenter.getUsername();
		commandS += " " + virtualCenter.getPassword();
		commandS += " " + vm;

		if (params != null) {
			for (String param : params) {
				commandS += " '" + param + "'";
			}
		}

		CmdConnection cmdcon = new CmdConnection();
		cmdcon.init();
		cmdcon.setCloneOnEveryOperation(true);

		CliCommand cmd = new CliCommand(commandS);
		cmd.setTimeout(1000 * 600);
		cmdcon.handleCliCommand("", cmd);

		String message = cmd.getResult();
		boolean status = !message.contains("FullyQualifiedErrorId");

		report.report("Execute " + commandS, message, status);
		setTestAgainstObject(message);
		return message;

	}

	public String getPathToScripts() {
		return pathToScripts;
	}

	public void setPathToScripts(String pathToScripts) {
		this.pathToScripts = pathToScripts;
	}

	public String getPowerCliCommand() {
		return powerCliCommand;
	}

	public void setPowerCliCommand(String powerCliCommand) {
		this.powerCliCommand = powerCliCommand;
	}

}
