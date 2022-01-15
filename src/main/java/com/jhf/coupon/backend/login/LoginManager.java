package com.jhf.coupon.backend.login;

import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.backend.facade.AdminFacade;
import com.jhf.coupon.backend.facade.ClientFacade;
import com.jhf.coupon.backend.facade.CompanyFacade;
import com.jhf.coupon.backend.facade.CustomerFacade;
import org.jetbrains.annotations.NotNull;

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

	public ClientFacade login(String email, String password, @NotNull ClientType clientType) throws SQLException, InterruptedException, ClientTypeNotFoundException, InvalidLoginCredentialsException {
		switch (clientType.getType()) {
			case "admin":
				facade = new AdminFacade();
				break;
			case "company":
				facade = new CompanyFacade();
				break;
			case "customer":
				facade = new CustomerFacade();
				break;
			default:
				throw new ClientTypeNotFoundException("Could not find ClientType of type : " + clientType);
		}
		if (facade.login(email, password))
			return facade;
		else
			throw new InvalidLoginCredentialsException("Could not Authenticate using email & password: " + email + ", " + password);
	}

}
