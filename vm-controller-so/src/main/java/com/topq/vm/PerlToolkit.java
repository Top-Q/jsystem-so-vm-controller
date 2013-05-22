package com.topq.vm;

import java.io.File;

import jsystem.framework.system.SystemObjectImpl;
import jsystem.utils.exec.Command;
import jsystem.utils.exec.Execute;

public class PerlToolkit extends SystemObjectImpl {

	public static enum VmControlOperations {
		poweron, poweroff, shutdown, reset, reboot, suspend, standby
	}

	@Override
	public void init() throws Exception {
		super.init();
	}

	private String toolKitLocation = "C:\\Program Files\\VMware\\VMware vSphere CLI\\Perl\\apps";

	public String vmControl(boolean validateSuccess, VirtualCenter virtualCenter, String vm, VmControlOperations oper) throws Exception {
		return executeScript(validateSuccess, "vm\\vmcontrol.pl", virtualCenter, vm, "--operation", oper.name());
	}

	public String executeScript(boolean validateSuccess, String scriptName, VirtualCenter virtualCenter, String vm, String... params)
			throws Exception {

		Command command = new Command();
		command.setDir(new File(toolKitLocation));

		String[] commands = new String[params.length + 11];
		commands[0] = "CMD.exe";
		commands[1] = "/C";
		commands[2] = scriptName;
		commands[3] = "--server";
		commands[4] = virtualCenter.getServer();
		commands[5] = "--username";
		commands[6] = virtualCenter.getUsername();
		commands[7] = "--password";
		commands[8] = virtualCenter.getPassword();
		commands[9] = "--vmname";
		commands[10] = vm;
		System.arraycopy(params, 0, commands, 11, params.length);
		command.setCmd(commands);

		Execute.execute(command, true);

		// SOAP Fault
		String message = command.getStd().toString();
		boolean status = true;
		if (validateSuccess) {
			status = !message.contains("SOAP Fault");
		}
		report.report("Execute " + command.getCommandAsString(), message, status);
		setTestAgainstObject(message);
		return message;
	}

	public void executeScript(boolean validateSuccess, String scriptName, String... params) throws Exception {
		Command command = new Command();
		command.setDir(new File(toolKitLocation));
		String[] commands = new String[params.length + 3];
		commands[0] = "CMD.exe";
		commands[1] = "/C";
		commands[2] = scriptName;
		System.arraycopy(params, 0, commands, 3, params.length);
		command.setCmd(commands);

		Execute.execute(command, true);
		// SOAP Fault
		String message = command.getStd().toString();
		boolean status = true;
		if (validateSuccess) {
			status = !message.contains("SOAP Fault");
		}
		setTestAgainstObject(message);
	}

	public String getToolKitLocation() {
		return toolKitLocation;
	}

	public void setToolKitLocation(String toolKitLocation) {
		this.toolKitLocation = toolKitLocation;
	}

}
