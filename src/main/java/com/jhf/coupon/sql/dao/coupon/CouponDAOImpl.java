package com.jhf.coupon.sql.dao.coupon;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;

public class CouponDAOImpl implements CouponsDAO {
	private final ConnectionPool pool;
	private Connection connection;

	public CouponDAOImpl() {
		pool = ConnectionPool.getInstance();
	}

	@Override
	public boolean couponExists(@NotNull Coupon coupon) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM `coupons`" +
				                  " WHERE `TITLE` = '" + coupon.getTitle() +
				                  "' AND `COMPANY_ID` = '" + coupon.getCompanyID() + "'";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		ResultSet resultSet = preparedStatement.executeQuery(sqlQuery);
		boolean exists = resultSet.next();

		preparedStatement.close();
		connection.close();
		return exists;
	}

	public void addCoupon(@NotNull Coupon coupon) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "INSERT INTO coupons (COMPANY_ID, CATEGORY_ID, TITLE, DESCRIPTION, " +
				                  "START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) " +
				                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, coupon.getCompanyID());
		preparedStatement.setInt(2, coupon.getCATEGORY().getId());
		preparedStatement.setString(3, coupon.getTitle());
		preparedStatement.setString(4, coupon.getDescription());
		preparedStatement.setDate(5, coupon.getStartDate());
		preparedStatement.setDate(6, coupon.getEndDate());
		preparedStatement.setInt(7, coupon.getAmount());
		preparedStatement.setDouble(8, coupon.getPrice());
		preparedStatement.setString(9, coupon.getImage());

		preparedStatement.execute();
		preparedStatement.close();
		connection.close();
	}

	public void updateCoupon(@NotNull Coupon coupon) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "UPDATE coupons SET " +
				                  "`COMPANY_ID` = ?, " +
				                  "`CATEGORY_ID` = ?, " +
				                  "`TITLE` = ?, " +
				                  "`DESCRIPTION` = ?, " +
				                  "`START_DATE` = ?, " +
				                  "`END_DATE` = ?, " +
				                  "`AMOUNT` = ?, " +
				                  "`PRICE` = ?, " +
				                  "`IMAGE` = ? " +
				                  "WHERE `ID` = " + coupon.getId();
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, coupon.getCompanyID());
		preparedStatement.setInt(2, coupon.getCATEGORY().getId());
		preparedStatement.setString(3, coupon.getTitle());
		preparedStatement.setString(4, coupon.getDescription());
		preparedStatement.setDate(5, coupon.getStartDate());
		preparedStatement.setDate(6, coupon.getEndDate());
		preparedStatement.setInt(7, coupon.getAmount());
		preparedStatement.setDouble(8, coupon.getPrice());
		preparedStatement.setString(9, coupon.getImage());

		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	public void deleteCoupon(int couponID) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "DELETE FROM coupons " +
				                  "WHERE `ID` = " + couponID;
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	public ArrayList<Coupon> getAllCoupons() throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
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
		Coupon coupon;
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM coupons " +
				                  "WHERE `ID` = " + couponID;
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		ResultSet resultSet = preparedStatement.executeQuery(sqlQuery);
		boolean exists = resultSet.next();
		if (exists) {
			coupon = new Coupon(
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

		resultSet.close();
		preparedStatement.close();
		connection.close();

		return coupon;
	}

	@Override
	public ArrayList<Coupon> getCompanyCoupons(int companyId) throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM coupons " +
				                  "WHERE `COMPANY_ID` = " + companyId;
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

	@Override
	public ArrayList<Coupon> getCompanyCoupons(@NotNull Company company, @NotNull Category CATEGORY) throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM coupons " +
				                  "WHERE `COMPANY_ID` = " + company.getId() +
				                  " AND `CATEGORY_ID` = " + CATEGORY.getId();
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

	@Override
	public ArrayList<Coupon> getCompanyCoupons(@NotNull Company company, double maxPrice) throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM coupons " +
				                  "WHERE `COMPANY_ID`= " + company.getId() +
				                  " AND `PRICE` IS BETWEEN 0 AND " + (int) maxPrice;
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

	public boolean customerCouponPurchaseExists(int customerId, int couponId) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM customers_vs_coupons " +
				                  "WHERE `CUSTOMER_ID` = " + customerId +
				                  " AND `COUPON_ID` = " + couponId;
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		ResultSet resultSet = preparedStatement.executeQuery(sqlQuery);
		boolean exists = resultSet.next();

		preparedStatement.close();
		resultSet.close();
		connection.close();

		return exists;
	}

	public void addCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "INSERT INTO customers_vs_coupons " +
				                  "VALUES (?, ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, customerId);
		preparedStatement.setInt(2, couponId);
		preparedStatement.execute();

		preparedStatement.close();
		connection.close();
	}

	@Override
	public ArrayList<Coupon> getCustomerCoupons(@NotNull Customer customer) throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		connection = pool.getConnection();
		String sqlQuery = "SELECT `COUPON_ID` FROM customers_vs_coupons " +
				                  "WHERE `CUSTOMER_ID` = " + customer.getId();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sqlQuery);

		while (resultSet.next()) {
			list.add(getCoupon(resultSet.getInt("COUPON_ID")));
		}

		resultSet.close();
		statement.close();
		connection.close();

		return list;
	}

	public void deleteCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "DELETE FROM customers_vs_coupons " +
				                  "WHERE CUSTOMER_ID = " + customerId +
				                  " AND COUPON_ID = " + couponId;
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.execute();

		preparedStatement.close();
		connection.close();
	}
}
