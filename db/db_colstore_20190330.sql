-- MySQL dump 10.13  Distrib 8.0.15, for Linux (x86_64)
--
-- Host: localhost    Database: db_colstore
-- ------------------------------------------------------
-- Server version	8.0.15

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8mb4 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `db_tbllist`
--

DROP TABLE IF EXISTS `db_tbllist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `db_tbllist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `db_id` int(11) DEFAULT NULL,
  `tbl_name` varchar(45) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `no_of_col` int(11) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_db_id_idx` (`db_id`),
  CONSTRAINT `fk_db_id` FOREIGN KEY (`db_id`) REFERENCES `tb_dblist` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `db_tbllist`
--

LOCK TABLES `db_tbllist` WRITE;
/*!40000 ALTER TABLE `db_tbllist` DISABLE KEYS */;
/*!40000 ALTER TABLE `db_tbllist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_collist`
--

DROP TABLE IF EXISTS `tb_collist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_collist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tbl_id` int(11) NOT NULL,
  `col_name` varchar(45) DEFAULT NULL,
  `col_dataType` enum('INT','STRING') DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tb_collist_1_idx` (`tbl_id`),
  CONSTRAINT `fk_tb_collist` FOREIGN KEY (`tbl_id`) REFERENCES `db_tbllist` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_collist`
--

LOCK TABLES `tb_collist` WRITE;
/*!40000 ALTER TABLE `tb_collist` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_collist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_dblist`
--

DROP TABLE IF EXISTS `tb_dblist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_dblist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `db_name` varchar(45) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tb_dblist_1_idx` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_dblist`
--

LOCK TABLES `tb_dblist` WRITE;
/*!40000 ALTER TABLE `tb_dblist` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_dblist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_menu_access`
--

DROP TABLE IF EXISTS `tb_menu_access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_menu_access` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `menu_id` int(11) DEFAULT NULL,
  `role_id` int(11) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_menu` (`menu_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_menu_access`
--

LOCK TABLES `tb_menu_access` WRITE;
/*!40000 ALTER TABLE `tb_menu_access` DISABLE KEYS */;
INSERT INTO `tb_menu_access` VALUES (32,1,2,1,'2014-09-05 17:41:04','2014-09-05 17:41:04'),(35,9,2,1,'2014-09-05 17:41:04','2014-09-05 17:41:04'),(36,11,2,1,'2014-09-05 17:41:04','2014-09-05 17:41:04'),(85,1,3,1,'2019-03-29 11:40:18','2019-03-29 11:40:18'),(86,9,3,1,'2019-03-29 11:40:49','2019-03-29 11:40:49');
/*!40000 ALTER TABLE `tb_menu_access` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_menu_details`
--

DROP TABLE IF EXISTS `tb_menu_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_menu_details` (
  `menu_id` int(11) NOT NULL AUTO_INCREMENT,
  `menu_name` varchar(50) DEFAULT NULL,
  `target_page` varchar(200) DEFAULT NULL,
  `icon_image` varchar(200) DEFAULT NULL,
  `menu_description` varchar(100) DEFAULT NULL,
  `hint_text` varchar(100) DEFAULT NULL,
  `status` tinyint(4) DEFAULT '1',
  `show_order` int(11) DEFAULT '1',
  `created_on` datetime DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  `parent_menu` int(11) DEFAULT '0',
  `system_menu` tinyint(4) DEFAULT '0',
  `has_submenu` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`menu_id`),
  UNIQUE KEY `unq_menu` (`menu_name`)
) ENGINE=MyISAM AUTO_INCREMENT=432 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_menu_details`
--

LOCK TABLES `tb_menu_details` WRITE;
/*!40000 ALTER TABLE `tb_menu_details` DISABLE KEYS */;
INSERT INTO `tb_menu_details` VALUES (1,'Configurations','BLANK',NULL,NULL,NULL,1,1,NULL,NULL,0,0,1),(9,'Logout','/faces/index.xhtml',NULL,'Logout Button. Remove Login Credentials from Session.','Click to Logout and clear Session Cache.',1,99,NULL,NULL,0,0,0),(11,'User Manager','/faces/ui/config/usermgr.xhtml','','Create New User','',1,1,'2019-03-29 11:24:30','2019-03-29 11:24:30',1,0,0);
/*!40000 ALTER TABLE `tb_menu_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_user_roles`
--

DROP TABLE IF EXISTS `tb_user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_user_roles` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `role_descp` varchar(200) DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `created_on` datetime DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  `homepage` varchar(100) DEFAULT 'loginsuccess',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uniq_role_name` (`role_name`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_user_roles`
--

LOCK TABLES `tb_user_roles` WRITE;
/*!40000 ALTER TABLE `tb_user_roles` DISABLE KEYS */;
INSERT INTO `tb_user_roles` VALUES (1,'superuser','superuser',1,'2014-09-05 18:40:13','2014-09-05 18:40:38','uimainmenu'),(2,'administrator','admin',1,'2014-09-05 18:40:16','2014-09-05 18:40:40','uimainmenu'),(3,'Content Provider','Content Provider',1,'2014-09-05 18:40:19','2014-09-05 18:40:43','uimainmenu'),(4,'Portal Owner','Portal Owner/Content Publisher',1,'2014-09-05 18:40:21','2014-09-05 18:40:46','uimainmenu'),(5,'Content Administrator','Content Administrator',1,'2014-09-05 18:40:23','2014-09-05 18:40:48','uimainmenu'),(6,'SCM/Accounts','SCM/Accounts',1,'2014-09-05 18:40:25','2014-09-05 18:40:51','uimainmenu'),(7,'Technical Operation','Technical Operation',1,'2014-09-05 18:40:28','2014-09-05 18:40:53','uimainmenu'),(8,'Business Operation','Business Operation',1,'2014-09-05 18:40:30','2014-09-05 18:40:56','uimainmenu'),(9,'Customer Care - System','Customer Care - System',1,'2014-09-05 18:40:33','2014-09-05 18:40:58','uimainmenu'),(10,'Customer Care - Portal','Customer Care - Portal',1,'2014-09-05 18:40:35','2014-09-05 18:41:01','uimainmenu');
/*!40000 ALTER TABLE `tb_user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_users`
--

DROP TABLE IF EXISTS `tb_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `status` tinyint(4) DEFAULT '1',
  `created_on` datetime DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  `company_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `unq_user` (`user_name`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_users`
--

LOCK TABLES `tb_users` WRITE;
/*!40000 ALTER TABLE `tb_users` DISABLE KEYS */;
INSERT INTO `tb_users` VALUES (1,2,'admin','admin','admin@abc.com','919999631990',1,NULL,NULL,1),(4,3,'nilesh','nilesh','nileshsingh067@gmail.com','9457240028',1,'2019-03-29 11:32:35','2019-03-29 11:32:35',1);
/*!40000 ALTER TABLE `tb_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_users_contact_details`
--

DROP TABLE IF EXISTS `tb_users_contact_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_users_contact_details` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `contact_category` int(11) DEFAULT NULL,
  `designation` varchar(50) DEFAULT NULL,
  `reg_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_key` (`user_id`,`name`,`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_users_contact_details`
--

LOCK TABLES `tb_users_contact_details` WRITE;
/*!40000 ALTER TABLE `tb_users_contact_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_users_contact_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_users_portal_access`
--

DROP TABLE IF EXISTS `tb_users_portal_access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tb_users_portal_access` (
  `portal_access_map_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `role_id` int(11) DEFAULT NULL,
  `portal_id` int(11) DEFAULT NULL,
  `access_start_date` datetime DEFAULT NULL,
  `access_end_date` datetime DEFAULT NULL,
  `use_date_check` tinyint(4) DEFAULT '1',
  `status` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`portal_access_map_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_users_portal_access`
--

LOCK TABLES `tb_users_portal_access` WRITE;
/*!40000 ALTER TABLE `tb_users_portal_access` DISABLE KEYS */;
INSERT INTO `tb_users_portal_access` VALUES (1,2,4,1,'2014-09-10 13:13:49','2015-09-10 13:13:49',0,1),(2,3,4,1,'2014-09-10 13:18:16','2015-09-10 13:18:16',0,1);
/*!40000 ALTER TABLE `tb_users_portal_access` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-03-30 15:04:19
