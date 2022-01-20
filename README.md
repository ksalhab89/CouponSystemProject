## CouponSystemProject
JHF FullStack Coupon Project 

This Project intends to test the Full Stack Capability of integrating SQL DB and Java at its basic OOP form . 

The Project consists of two main groups , core and sql 

It includes a daily job that checks expiration date of created coupons . 


**Before you start **
-On MySQL workbench 
	1.import DB from .zip file named Coupon_SystemDB.sql as a self-contained File DB 
	2.Set as default Schema in WorkBench

-On Eclipse Project
	In /main/java/com/jhf/CouponSystem/sql/utils/ConnectionPool
	Change the path , username and password to work with the local workbench
	private static final String URL = "jdbc:mysql://localhost:3306/<Path>?useSSL=false&serverTimezone=UTC";
	private static final String USER = "<user>";
	private static final String PASSWORD = "<password>";


**Run**	
-Run Program From Runner
	
Tip: you can modify run methods in main/java/com/jhf/CouponSystem/MockUp