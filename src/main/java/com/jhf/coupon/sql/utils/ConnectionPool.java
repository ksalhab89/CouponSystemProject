package com.jhf.coupon.sql.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final int POOL_SIZE = 50;

	private static final Set<Connection> availableConnections = new HashSet<>();
	private static final Set<Connection> usedConnections = new HashSet<>();
	private static ConnectionPool instance = null;

	private ConnectionPool() {
		try {
			Class.forName(JDBC_DRIVER);
			String dbUrl = System.getenv("DB_URL");
			String dbUser = System.getenv("DB_USER");
			String dbPassword = System.getenv("DB_PASSWORD");

			if (dbUrl == null || dbUser == null || dbPassword == null) {
				Properties properties = new Properties();
				try (InputStream input = ConnectionPool.class.getClassLoader().getResourceAsStream("config.properties")) {
					properties.load(input);
				} catch (IOException e) {
					logger.error("Failed to load database configuration from config.properties", e);
				}
				dbUrl = properties.getProperty("db.url");
				dbUser = properties.getProperty("db.user");
				dbPassword = properties.getProperty("db.password");
			}

			for (int i = 0; i < POOL_SIZE; i++) {
				Connection realConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
				availableConnections.add(realConnection);
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("Failed to initialize connection pool", e);
		}
	}

	public static ConnectionPool getInstance() {
		if (instance == null) {
			synchronized (ConnectionPool.class) {
				if (instance == null) {
					instance = new ConnectionPool();
				}
			}
		}
		return instance;
	}

	public synchronized Connection getConnection() throws InterruptedException {
		while (availableConnections.isEmpty()) {
			wait(5000);
		}

		Connection realConnection = availableConnections.iterator().next();

		// Validate connection before returning
		try {
			if (realConnection.isClosed() || !realConnection.isValid(2)) {
				// Connection is invalid, remove it and get a new one
				availableConnections.remove(realConnection);
				try {
					realConnection.close();
				} catch (SQLException ignored) {
					// Already closed or unusable
				}

				// Create a new connection to replace the invalid one
				Properties properties = loadProperties();
				String dbUrl = System.getenv("DB_URL");
				String dbUser = System.getenv("DB_USER");
				String dbPassword = System.getenv("DB_PASSWORD");

				if (dbUrl == null || dbUser == null || dbPassword == null) {
					dbUrl = properties.getProperty("db.url");
					dbUser = properties.getProperty("db.user");
					dbPassword = properties.getProperty("db.password");
				}

				realConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			}
		} catch (SQLException e) {
			logger.error("Failed to validate or replace invalid connection", e);
		}

		availableConnections.remove(realConnection);
		usedConnections.add(realConnection);

		return createProxyConnection(realConnection);
	}

	private Properties loadProperties() {
		Properties properties = new Properties();
		try (InputStream input = ConnectionPool.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (input != null) {
				properties.load(input);
			}
		} catch (IOException e) {
			logger.error("Failed to load properties file", e);
		}
		return properties;
	}

	private Connection createProxyConnection(Connection realConnection) {
		return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class[]{Connection.class},
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if ("close".equals(method.getName())) {
							restoreConnection(realConnection);
							return null;
						}
						return method.invoke(realConnection, args);
					}
				});
	}

	private synchronized void restoreConnection(Connection connection) {
		if (usedConnections.remove(connection)) {
			availableConnections.add(connection);
			notifyAll();
		}
	}

	public synchronized void closeAll() {
		for (Connection connection : usedConnections) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Failed to close used connection", e);
			}
		}
		for (Connection connection : availableConnections) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Failed to close available connection", e);
			}
		}
		availableConnections.clear();
		usedConnections.clear();
	}

}
