package com.topq.vm;

import jsystem.framework.system.SystemObjectImpl;

public class VirtualCenter extends SystemObjectImpl {

	private String server;
	private String username;
	private String password;

	@Override
	public void init() throws Exception {
		super.init();
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
