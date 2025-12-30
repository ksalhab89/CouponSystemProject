CREATE DATABASE  IF NOT EXISTS `couponsystem` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `couponsystem`;
-- MySQL dump 10.13  Distrib 8.0.27, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: couponsystem
-- ------------------------------------------------------
-- Server version	8.0.27

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `ID` int NOT NULL COMMENT 'Identification Number',
  `NAME` varchar(48) DEFAULT NULL COMMENT 'Coupon Category Name',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (10,'SKYING'),(20,'SKY_DIVING'),(30,'FANCY_RESTAURANT'),(40,'ALL_INCLUSIVE_VACATION');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `companies`
--

DROP TABLE IF EXISTS `companies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `companies` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT 'Identification Number',
  `NAME` varchar(48) DEFAULT NULL COMMENT 'Company Name',
  `EMAIL` varchar(48) DEFAULT NULL COMMENT 'Company Email',
  `PASSWORD` varchar(60) DEFAULT NULL COMMENT 'Login Password (bcrypt hash)',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `companies`
--

LOCK TABLES `companies` WRITE;
/*!40000 ALTER TABLE `companies` DISABLE KEYS */;
INSERT INTO `companies` VALUES (11,'JHF10','jhf10@mail.com','jhf'),(12,'JHF11','jhf11@mail.com','jhf'),(13,'JHF12','jhf12@mail.com','jhf'),(14,'JHF13','jhf13@mail.com','jhf'),(15,'JHF14','jhf14@mail.com','jhf'),(16,'JHF15','jhf15@mail.com','jhf'),(17,'JHF8','jhf8@mail.com','jhf7'),(18,'JHF66','jhf65@mail.com','jhf'),(19,'JHF26','jhf25@mail.com','jhf'),(20,'JHF9','jhf9@mail.com','jhf7'),(21,'Khaled','Salhab@mail.com','pass'),(22,'Khaled','Salhab@mail.com','pass');
/*!40000 ALTER TABLE `companies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupons`
--

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT 'Identification Number',
  `COMPANY_ID` int DEFAULT NULL COMMENT 'Company Identification Number',
  `CATEGORY_ID` int DEFAULT NULL COMMENT 'Category Identification Number',
  `TITLE` varchar(48) DEFAULT NULL COMMENT 'Coupon Title',
  `DESCRIPTION` varchar(48) DEFAULT NULL COMMENT 'Description of the coupon',
  `START_DATE` date DEFAULT NULL COMMENT 'Coupon creation date',
  `END_DATE` date DEFAULT NULL COMMENT 'Coupon expiration date',
  `AMOUNT` int DEFAULT NULL COMMENT 'Quantity of coupons in stock',
  `PRICE` double DEFAULT NULL COMMENT 'Price of the coupon',
  `IMAGE` varchar(48) DEFAULT NULL COMMENT 'Name of the image file',
  PRIMARY KEY (`ID`),
  KEY `COMPANY_ID` (`COMPANY_ID`),
  KEY `CATEGORY_ID` (`CATEGORY_ID`),
  CONSTRAINT `coupons_ibfk_1` FOREIGN KEY (`COMPANY_ID`) REFERENCES `companies` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `coupons_ibfk_2` FOREIGN KEY (`CATEGORY_ID`) REFERENCES `categories` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupons`
--

LOCK TABLES `coupons` WRITE;
/*!40000 ALTER TABLE `coupons` DISABLE KEYS */;
INSERT INTO `coupons` VALUES (18,21,40,'Maldives Trip','All Inclusive trip to Maldives','2022-01-18','2022-12-01',2,2500,'Image');
/*!40000 ALTER TABLE `coupons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT 'Identification Number',
  `FIRST_NAME` varchar(48) DEFAULT NULL COMMENT 'First Name',
  `LAST_NAME` varchar(48) DEFAULT NULL COMMENT 'Last Name',
  `EMAIL` varchar(48) DEFAULT NULL COMMENT 'Customer Email',
  `PASSWORD` varchar(60) DEFAULT NULL COMMENT 'Login Password (bcrypt hash)',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'Dawoud','kabhad','kabha@mail.com','kabha'),(2,'Dawoudd','Kabhaa','kabhaa@mail.com','kabha'),(3,'dawoudd','kabhaa','Kabh@mail.com','kabha'),(4,'dawod','kabha','dawoud@mail.com','kabha'),(5,'dawood','kabhha','dawooud@mail.com','kabha'),(6,'dawohod','kabhhha','dawhooud@mail.com','kabha'),(7,'ddawohod','kkabhhha','kdawhooud@mail.com','kabha'),(8,'dddawohod','kdkabhhha','kddawhooud@mail.com','kabha'),(9,'ddddawohod','kddkabhhha','kdddawhooud@mail.com','kabha'),(10,'dddddawohod','kdddkabhhha','kdddawhdooud@mail.com','kabha'),(11,'Mohammad','Yassin','tester@mail.com','tester');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers_vs_coupons`
--

DROP TABLE IF EXISTS `customers_vs_coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers_vs_coupons` (
  `CUSTOMER_ID` int NOT NULL,
  `COUPON_ID` int NOT NULL,
  PRIMARY KEY (`CUSTOMER_ID`,`COUPON_ID`),
  KEY `COUPON_ID` (`COUPON_ID`),
  CONSTRAINT `customers_vs_coupons_ibfk_1` FOREIGN KEY (`CUSTOMER_ID`) REFERENCES `customers` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `customers_vs_coupons_ibfk_2` FOREIGN KEY (`COUPON_ID`) REFERENCES `coupons` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers_vs_coupons`
--

LOCK TABLES `customers_vs_coupons` WRITE;
/*!40000 ALTER TABLE `customers_vs_coupons` DISABLE KEYS */;
INSERT INTO `customers_vs_coupons` VALUES (1,18);
/*!40000 ALTER TABLE `customers_vs_coupons` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-01-18  7:59:06
