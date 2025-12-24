package com.jhf.coupon.sql.utils;

import java.sql.Connection;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class ConnectionPool {
	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

	private static final Set<Connection> connections = new HashSet<>();
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
					e.printStackTrace();
				}
				dbUrl = properties.getProperty("db.url");
				dbUser = properties.getProperty("db.user");
				dbPassword = properties.getProperty("db.password");
			}

			for (int i = 50; i > 0; i--) {
				connections.add(DriverManager.getConnection(dbUrl, dbUser, dbPassword));
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static ConnectionPool getInstance() {
		if (instance == null) {
			instance = new ConnectionPool();
		}
		return instance;
	}

	public synchronized Connection getConnection() throws InterruptedException {
		Connection connection = null;
		if (connections.isEmpty()) {
			wait();
		} else {
			Iterator<Connection> it = connections.iterator();
			connection = it.next();
			connections.remove(connection);
		}
		return connection;
	}

	public void restoreConnection(Connection connection) {
		connections.add(connection);
		notifyAll();
	}

	public static void closeAll() {
		Iterator<Connection> iterator = connections.iterator();
		while (iterator.hasNext() & iterator.next() != null) {
			try {
				iterator.next().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
