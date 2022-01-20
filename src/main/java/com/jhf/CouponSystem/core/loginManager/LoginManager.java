package main.java.com.jhf.CouponSystem.core.loginManager;

import java.sql.SQLException;

import main.java.com.jhf.CouponSystem.core.exceptions.ClientTypeNotFoundException;
import main.java.com.jhf.CouponSystem.core.exceptions.InvalidLoginCredentialsException;
import main.java.com.jhf.CouponSystem.core.facade.AdminFacade;
import main.java.com.jhf.CouponSystem.core.facade.ClientFacade;
import main.java.com.jhf.CouponSystem.core.facade.CompanyFacade;
import main.java.com.jhf.CouponSystem.core.facade.CustomerFacade;

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

	public ClientFacade login(String email, String password, ClientType clientType)
			throws SQLException, InterruptedException, ClientTypeNotFoundException, InvalidLoginCredentialsException {
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
			throw new InvalidLoginCredentialsException(
					"Could not Authenticate using email & password: " + email + ", " + password);
	}

}
