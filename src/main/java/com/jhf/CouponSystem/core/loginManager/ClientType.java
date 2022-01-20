package main.java.com.jhf.CouponSystem.core.loginManager;

import main.java.com.jhf.CouponSystem.core.exceptions.ClientTypeNotFoundException;

public enum ClientType {
	ADMIN("admin"), COMPANY("company"), CUSTOMER("customer");

	private final String type;

	private static ClientType getClientType(String type) throws ClientTypeNotFoundException {
		for (ClientType clientType : values()) {
			if (clientType.type.equals(type)) {
				return clientType;
			}
		}
		throw new ClientTypeNotFoundException("Could not find ClientType of type : " + type);
	}

	public String getType() {
		return type;
	}

	private ClientType(String type) {
		this.type = type;
	}
}
