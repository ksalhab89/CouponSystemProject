package com.jhf.coupon.backend.login;

import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public enum ClientType {
	ADMIN("admin"),
	COMPANY("company"),
	CUSTOMER("customer");

	@Getter
	private final String type;

	@NotNull
	private static ClientType getClientType(String type) throws ClientTypeNotFoundException {
		for (ClientType clientType : values()) {
			if (clientType.type.equals(type)) {
				return clientType;
			}
		}
		throw new ClientTypeNotFoundException("Could not find ClientType of type : " + type);
	}
}
