/*
 Navicat Premium Data Transfer

 Source Server         : 本机
 Source Server Type    : MySQL
 Source Server Version : 50717
 Source Host           : localhost
 Source Database       : ratel

 Target Server Type    : MySQL
 Target Server Version : 50717
 File Encoding         : utf-8

 Date: 02/11/2019 10:33:51 AM
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `api`
-- ----------------------------
DROP TABLE IF EXISTS `api`;
CREATE TABLE `api` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_id` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `name` varchar(127) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `parameter` text,
  `vhost` varchar(255) DEFAULT NULL,
  `running` smallint(6) DEFAULT '0' COMMENT '0= 停止，1=运行， 2=暂停',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `app`
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `port` smallint(6) NOT NULL COMMENT '使用的端口',
  `protocol` varchar(16) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deploy_group` smallint(6) DEFAULT NULL,
  `name` varchar(127) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `parameter` text,
  `vhost` varchar(255) DEFAULT NULL,
  `running` smallint(6) DEFAULT '0' COMMENT '0= 停止，1=运行， 2=暂停',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `groups`
-- ----------------------------
DROP TABLE IF EXISTS `groups`;
CREATE TABLE `groups` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `name` varchar(127) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `groups`
-- ----------------------------
BEGIN;
INSERT INTO `groups` VALUES ('0', '默认', '这是一个默认组', '2018-08-31 17:11:58');
COMMIT;

-- ----------------------------
--  Table structure for `node`
-- ----------------------------
DROP TABLE IF EXISTS `node`;
CREATE TABLE `node` (
  `mac` varchar(32) NOT NULL,
  `ip` varchar(32) DEFAULT NULL,
  `join_date` datetime DEFAULT NULL,
  `group_id` smallint(6) DEFAULT NULL,
  `parameter` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mac`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `sys_app_status`
-- ----------------------------
DROP TABLE IF EXISTS `sys_app_status`;
CREATE TABLE `sys_app_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node` varchar(64) DEFAULT NULL,
  `app_id` int(11) DEFAULT NULL,
  `type` tinyint(4) DEFAULT NULL COMMENT '类型，0=基础',
  `status` text,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `I_ND_TIME` (`node`,`create_time`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=18695 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `sys_status`
-- ----------------------------
DROP TABLE IF EXISTS `sys_status`;
CREATE TABLE `sys_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node` varchar(64) DEFAULT NULL,
  `type` tinyint(4) DEFAULT NULL COMMENT '类型，0=基础',
  `status` text,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `I_ND_TIME` (`node`,`create_time`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=5834 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `sys_user`
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `account` varchar(32) NOT NULL COMMENT '账户名 account name',
  `password` varchar(128) NOT NULL COMMENT '密码 password',
  `name` varchar(256) NOT NULL COMMENT '名字/昵称 name/nickname',
  `email` varchar(256) NOT NULL COMMENT 'email',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期 create date',
  `is_locked_out` int(1) NOT NULL DEFAULT '0' COMMENT '用户是否锁定 is the user locked out',
  `last_login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近登录 last login date',
  `role` varchar(20) DEFAULT NULL COMMENT 'admin, guest',
  `department` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`id`,`is_locked_out`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `sys_user`
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` VALUES ('1', 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 'admin@example.com', '2017-10-24 17:35:39', '0', '2017-10-24 17:35:39', 'admin', '');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
