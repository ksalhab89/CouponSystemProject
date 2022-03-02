package com.jhf.coupon.sql.utils;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConnectionPool {
	private static final String URL = "jdbc:mysql://localhost:3306/couponSystem?useSSL=false&serverTimezone=UTC";
	private static final String USER = "projectUser";
	private static final String PASSWORD = "projectUser";
	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

	private static final Set<Connection> connections = new HashSet<>();
	private static ConnectionPool instance = null;

	@SneakyThrows
	private ConnectionPool() {
		Class.forName(JDBC_DRIVER);
		for (int i = 50; i > 0; i--) {
			connections.add(DriverManager.getConnection(URL, USER, PASSWORD));
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

	@SneakyThrows
	public static void closeAll() {
		Iterator<Connection> iterator = connections.iterator();
		while (iterator.hasNext() & iterator.next() != null) {
			iterator.next().close();
		}
	}

}
