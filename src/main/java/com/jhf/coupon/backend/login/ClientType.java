package com.jhf.coupon.backend.login;

import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import org.jetbrains.annotations.NotNull;

public enum ClientType {
	ADMIN("admin"),
	COMPANY("company"),
	CUSTOMER("customer");

	private final String type;

	ClientType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@NotNull
	public static ClientType fromString(String type) throws ClientTypeNotFoundException {
		for (ClientType clientType : values()) {
			if (clientType.type.equalsIgnoreCase(type)) {
				return clientType;
			}
		}
		throw new ClientTypeNotFoundException("Could not find ClientType of type : " + type);
	}
}
