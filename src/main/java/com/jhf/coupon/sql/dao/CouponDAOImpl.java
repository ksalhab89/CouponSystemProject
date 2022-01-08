package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Category;
import com.jhf.coupon.backend.Coupon;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.sql.dao.exceptions.CouponNotFoundException;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;

public class CouponDAOImpl implements CouponsDAO {
	private final ConnectionPool pool;
	private Connection connection;

	CouponDAOImpl() {
		pool = ConnectionPool.getInstance();
	}

	public void addCoupon(@NotNull Coupon coupon) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		//todo check if should insert id!
		String sqlQuery = "INSERT INTO coupons VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(2, coupon.getCompanyID());
		preparedStatement.setInt(3, coupon.getCATEGORY().getCategoryId());
		preparedStatement.setString(4, coupon.getTitle());
		preparedStatement.setString(5, coupon.getDescription());
		preparedStatement.setString(6, coupon.getDescription());
		preparedStatement.setDate(7, coupon.getStartDate());
		preparedStatement.setDate(8, coupon.getEndDate());
		preparedStatement.setDouble(9, coupon.getPrice());
		preparedStatement.setString(10, coupon.getImage());
		preparedStatement.execute();
		preparedStatement.close();
		connection.close();
	}

	public void updateCoupon(@NotNull Coupon coupon) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "UPDATE coupons SET COMPANY_ID = ? AND CATEGORY_ID = ? AND TITLE = ? AND DESCRIPTION = ? " +
				                  "AND START_DATE = ? AND END_DATE = ? AND AMOUNT = ? AND PRICE = ? AND IMAGE = ? " +
				                  "WHERE ID = ?;";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, coupon.getCompanyID());
		preparedStatement.setInt(2, coupon.getCATEGORY().getCategoryId());
		preparedStatement.setString(3, coupon.getTitle());
		preparedStatement.setString(4, coupon.getDescription());
		preparedStatement.setString(5, coupon.getDescription());
		preparedStatement.setDate(6, coupon.getStartDate());
		preparedStatement.setDate(7, coupon.getEndDate());
		preparedStatement.setDouble(8, coupon.getPrice());
		preparedStatement.setString(9, coupon.getImage());
		preparedStatement.setInt(10, coupon.getId());
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	public void deleteCoupon(int couponID) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "DELETE FROM coupons WHERE ID = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, couponID);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	public ArrayList<Coupon> getAllCoupons() throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<Coupon>();
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM coupons";
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sqlQuery);

		while (resultSet.next()) {
			list.add(new Coupon(
					resultSet.getInt("ID"),
					resultSet.getInt("COMPANY_ID"),
					Category.getCategory(resultSet.getInt("CATEGORY_ID")),
					resultSet.getString("TITLE"),
					resultSet.getString("DESCRIPTION"),
					resultSet.getDate("START_DATE"),
					resultSet.getDate("END_DATE"),
					resultSet.getInt("AMOUNT"),
					resultSet.getDouble("PRICE"),
					resultSet.getString("IMAGE")));
		}
		resultSet.close();
		statement.close();
		connection.close();
		return list;
	}

	public Coupon getCoupon(int couponID) throws InterruptedException, SQLException, CategoryNotFoundException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT EXISTS(SELECT * FROM coupons WHERE ID = ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, couponID);

		boolean exists = preparedStatement.execute(sqlQuery);
		if (exists) {
			sqlQuery = "SELECT * FROM companies WHERE ID = '" + couponID + "';";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);
			return new Coupon(
					resultSet.getInt("ID"),
					resultSet.getInt("COMPANY_ID"),
					Category.getCategory(resultSet.getInt("CATEGORY_ID")),
					resultSet.getString("TITLE"),
					resultSet.getString("DESCRIPTION"),
					resultSet.getDate("START_DATE"),
					resultSet.getDate("END_DATE"),
					resultSet.getInt("AMOUNT"),
					resultSet.getDouble("PRICE"),
					resultSet.getString("IMAGE"));
		} else throw new CouponNotFoundException(
				"Could not find Coupon with id: " + couponID);
	}

	public void addCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "INSERT INTO customers_vs_coupons VALUES (?, ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, customerId);
		preparedStatement.setInt(2, couponId);
		preparedStatement.execute();
		preparedStatement.close();
		connection.close();
	}

	public void deleteCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "DELETE FROM customers_vs_coupons WHERE CUSTOMER_ID = ? AND COUPON_ID = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, customerId);
		preparedStatement.setInt(2, couponId);
		preparedStatement.execute();
		preparedStatement.close();
		connection.close();
	}
}
