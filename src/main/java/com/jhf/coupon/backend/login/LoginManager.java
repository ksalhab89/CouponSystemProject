package com.jhf.coupon.backend.login;

import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import com.jhf.coupon.backend.facade.AdminFacade;
import com.jhf.coupon.backend.facade.ClientFacade;
import com.jhf.coupon.backend.facade.CompanyFacade;
import com.jhf.coupon.backend.facade.CustomerFacade;

import java.sql.SQLException;

public class LoginManager {
	private static LoginManager instance = null;
	private static ClientFacade facade;

	private LoginManager() {
	}

	public static LoginManager getInstance() {
		if (instance == null) {
			instance = new LoginManager();
		}
		return instance;
	}

	public ClientFacade login(String email, String password, ClientType clientType) throws SQLException, InterruptedException, ClientTypeNotFoundException {
		if (facade.login(email, password)) {
			switch (clientType.getType()) {
				case "admin":
					facade = new AdminFacade();
				case "company":
					facade = new CompanyFacade();
				case "customer":
					facade = new CustomerFacade();
				default:
					break;
			}
			return facade;
		} else
			throw new ClientTypeNotFoundException("Could not Authenticate using email & password of ClientType: " + clientType);
	}

}
