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

	public CouponDAOImpl() {
		pool = ConnectionPool.getInstance();
	}

	@Override
	public boolean couponExists(@NotNull Coupon coupon) throws InterruptedException, SQLException {
		String sqlQuery = "SELECT * FROM `coupons` WHERE `TITLE` = ? AND `COMPANY_ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, coupon.getTitle());
			preparedStatement.setInt(2, coupon.getCompanyID());
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	public void addCoupon(@NotNull Coupon coupon) throws InterruptedException, SQLException {
		String sqlQuery = "INSERT INTO coupons (COMPANY_ID, CATEGORY_ID, TITLE, DESCRIPTION, " +
				"START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
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
		}
	}

	public void updateCoupon(@NotNull Coupon coupon) throws InterruptedException, SQLException {
		String sqlQuery = "UPDATE coupons SET `COMPANY_ID` = ?, `CATEGORY_ID` = ?, `TITLE` = ?, " +
				"`DESCRIPTION` = ?, `START_DATE` = ?, `END_DATE` = ?, `AMOUNT` = ?, `PRICE` = ?, " +
				"`IMAGE` = ? WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, coupon.getCompanyID());
			preparedStatement.setInt(2, coupon.getCATEGORY().getId());
			preparedStatement.setString(3, coupon.getTitle());
			preparedStatement.setString(4, coupon.getDescription());
			preparedStatement.setDate(5, coupon.getStartDate());
			preparedStatement.setDate(6, coupon.getEndDate());
			preparedStatement.setInt(7, coupon.getAmount());
			preparedStatement.setDouble(8, coupon.getPrice());
			preparedStatement.setString(9, coupon.getImage());
			preparedStatement.setInt(10, coupon.getId());
			preparedStatement.executeUpdate();
		}
	}

	public void deleteCoupon(int couponID) throws InterruptedException, SQLException {
		String sqlQuery = "DELETE FROM coupons WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, couponID);
			preparedStatement.executeUpdate();
		}
	}

	public ArrayList<Coupon> getAllCoupons() throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		String sqlQuery = "SELECT * FROM coupons";
		try (Connection connection = pool.getConnection();
		     Statement statement = connection.createStatement();
		     ResultSet resultSet = statement.executeQuery(sqlQuery)) {
			while (resultSet.next()) {
				list.add(mapResultSetToCoupon(resultSet));
			}
		}
		return list;
	}

	public Coupon getCoupon(int couponID) throws InterruptedException, SQLException, CategoryNotFoundException {
		String sqlQuery = "SELECT * FROM coupons WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, couponID);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToCoupon(resultSet);
				} else {
					throw new CouponNotFoundException(
							"Could not find Coupon with id: " + couponID);
				}
			}
		}
	}

	@Override
	public ArrayList<Coupon> getCompanyCoupons(int companyId) throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		String sqlQuery = "SELECT * FROM coupons WHERE `COMPANY_ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, companyId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					list.add(mapResultSetToCoupon(resultSet));
				}
			}
		}
		return list;
	}

	@Override
	public ArrayList<Coupon> getCompanyCoupons(@NotNull Company company, @NotNull Category CATEGORY) throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		String sqlQuery = "SELECT * FROM coupons WHERE `COMPANY_ID` = ? AND `CATEGORY_ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, company.getId());
			preparedStatement.setInt(2, CATEGORY.getId());
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					list.add(mapResultSetToCoupon(resultSet));
				}
			}
		}
		return list;
	}

	@Override
	public ArrayList<Coupon> getCompanyCoupons(@NotNull Company company, double maxPrice) throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		String sqlQuery = "SELECT * FROM coupons WHERE `COMPANY_ID` = ? AND `PRICE` BETWEEN 0 AND ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, company.getId());
			preparedStatement.setDouble(2, maxPrice);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					list.add(mapResultSetToCoupon(resultSet));
				}
			}
		}
		return list;
	}

	public boolean customerCouponPurchaseExists(int customerId, int couponId) throws InterruptedException, SQLException {
		String sqlQuery = "SELECT * FROM customers_vs_coupons WHERE `CUSTOMER_ID` = ? AND `COUPON_ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, customerId);
			preparedStatement.setInt(2, couponId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	public void addCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException {
		String sqlQuery = "INSERT INTO customers_vs_coupons VALUES (?, ?)";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, customerId);
			preparedStatement.setInt(2, couponId);
			preparedStatement.execute();
		}
	}

	@Override
	public ArrayList<Coupon> getCustomerCoupons(@NotNull Customer customer) throws InterruptedException, SQLException, CategoryNotFoundException {
		ArrayList<Coupon> list = new ArrayList<>();
		String sqlQuery = "SELECT `COUPON_ID` FROM customers_vs_coupons WHERE `CUSTOMER_ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, customer.getId());
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					list.add(getCoupon(resultSet.getInt("COUPON_ID")));
				}
			}
		}
		return list;
	}

	public void deleteCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException {
		String sqlQuery = "DELETE FROM customers_vs_coupons WHERE CUSTOMER_ID = ? AND COUPON_ID = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, customerId);
			preparedStatement.setInt(2, couponId);
			preparedStatement.execute();
		}
	}

	/**
	 * Maps a ResultSet row to a Coupon object.
	 *
	 * @param resultSet the ResultSet positioned at a valid row
	 * @return a Coupon object populated from the current ResultSet row
	 * @throws SQLException if a database access error occurs or column is not found
	 * @throws CategoryNotFoundException if the category ID is invalid
	 */
	private Coupon mapResultSetToCoupon(ResultSet resultSet) throws SQLException, CategoryNotFoundException {
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
	}
}
