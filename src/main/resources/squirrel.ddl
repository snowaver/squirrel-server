/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50722
Source Host           : localhost:3306
Source Database       : squirrel

Target Server Type    : MYSQL
Target Server Version : 50722
File Encoding         : 65001

Date: 2019-10-08 17:57:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for chat_group
-- ----------------------------
DROP TABLE IF EXISTS `chat_group`;
CREATE TABLE `chat_group` (
  `ID` bigint(11) NOT NULL AUTO_INCREMENT,
  `IS_DELETED` bit(1) NOT NULL DEFAULT b'0',
  `CREATE_TIME` datetime NOT NULL,
  `CREATE_BY` bigint(20) NOT NULL,
  `LAST_MODIFY_TIME` datetime NOT NULL,
  `LAST_MODIFY_BY` bigint(255) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chat_group
-- ----------------------------
INSERT INTO `chat_group` VALUES ('24', '\0', '2019-10-08 17:21:55', '21', '2019-10-08 17:21:55', '0', 'A');
INSERT INTO `chat_group` VALUES ('25', '\0', '2019-10-08 17:28:13', '21', '2019-10-08 17:28:13', '0', 'B');

-- ----------------------------
-- Table structure for chat_group_sync
-- ----------------------------
DROP TABLE IF EXISTS `chat_group_sync`;
CREATE TABLE `chat_group_sync` (
  `SYNC_ID` bigint(20) DEFAULT NULL,
  `USER_ID` bigint(20) DEFAULT NULL,
  `TYPE` int(11) DEFAULT NULL COMMENT '类型，说明：1、新建组，2、修改组，3、解散组，4、（本人）加入组，5、（别人）加入组，6，修改人，7、移除人',
  `CHAT_GROUP_ID` bigint(20) DEFAULT NULL,
  `LAST_MODIFY_TIME` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chat_group_sync
-- ----------------------------
INSERT INTO `chat_group_sync` VALUES ('1', '21', '1', '24', '2019-10-08 17:21:55');
INSERT INTO `chat_group_sync` VALUES ('2', '21', '5', '24', '2019-10-08 17:22:55');
INSERT INTO `chat_group_sync` VALUES ('1', '20', '4', '24', '2019-10-08 17:22:55');
INSERT INTO `chat_group_sync` VALUES ('3', '21', '1', '25', '2019-10-08 17:28:13');
INSERT INTO `chat_group_sync` VALUES ('4', '21', '5', '25', '2019-10-08 17:28:31');
INSERT INTO `chat_group_sync` VALUES ('2', '20', '4', '25', '2019-10-08 17:28:31');
INSERT INTO `chat_group_sync` VALUES ('5', '21', '5', '25', '2019-10-08 17:49:33');
INSERT INTO `chat_group_sync` VALUES ('3', '20', '5', '25', '2019-10-08 17:49:33');
INSERT INTO `chat_group_sync` VALUES ('1', '23', '4', '25', '2019-10-08 17:49:33');

-- ----------------------------
-- Table structure for chat_group_user
-- ----------------------------
DROP TABLE IF EXISTS `chat_group_user`;
CREATE TABLE `chat_group_user` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `IS_DELETED` bit(1) NOT NULL DEFAULT b'0',
  `CREATE_TIME` datetime NOT NULL,
  `CREATE_BY` bigint(20) NOT NULL,
  `LAST_MODIFY_TIME` datetime NOT NULL,
  `LAST_MODIFY_BY` bigint(20) NOT NULL,
  `CHAT_GROUP_ID` bigint(20) NOT NULL,
  `CONTACT_ID` bigint(20) NOT NULL,
  `VCARD` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chat_group_user
-- ----------------------------
INSERT INTO `chat_group_user` VALUES ('27', '\0', '2019-10-08 17:21:55', '21', '2019-10-08 17:21:55', '21', '24', '21', 'LAN');
INSERT INTO `chat_group_user` VALUES ('28', '\0', '2019-10-08 17:22:55', '20', '2019-10-08 17:22:55', '20', '24', '20', 'JOHNNY');
INSERT INTO `chat_group_user` VALUES ('29', '\0', '2019-10-08 17:28:13', '21', '2019-10-08 17:28:13', '21', '25', '21', 'LAN');
INSERT INTO `chat_group_user` VALUES ('30', '\0', '2019-10-08 17:28:31', '20', '2019-10-08 17:28:31', '20', '25', '20', 'JOHNNY');
INSERT INTO `chat_group_user` VALUES ('31', '\0', '2019-10-08 17:49:33', '23', '2019-10-08 17:49:33', '23', '25', '23', 'XUN');

-- ----------------------------
-- Table structure for contact
-- ----------------------------
DROP TABLE IF EXISTS `contact`;
CREATE TABLE `contact` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USER_ID` bigint(20) NOT NULL,
  `CONTACT_ID` bigint(20) NOT NULL,
  `CONTACT_USERNAME` varchar(255) DEFAULT NULL,
  `REMARK` varchar(255) NOT NULL,
  `SUBSCRIBE_STATUS` int(11) NOT NULL COMMENT '好友添加状态，说明：0、未响应，1、已发送，2、被忽略，3、已忽略，4、被拒绝，5、已拒绝，6、已添加，7、被添加',
  `IS_DELETED` bit(1) NOT NULL DEFAULT b'0',
  `CREATE_TIME` datetime NOT NULL,
  `GROUP_NAME` varchar(255) DEFAULT '我的好友',
  `LAST_MODIFY_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of contact
-- ----------------------------
INSERT INTO `contact` VALUES ('29', '20', '21', '18702963722', 'LAN', '7', '\0', '2019-09-01 17:18:25', 'MY WIFE', '2019-09-01 17:30:55');
INSERT INTO `contact` VALUES ('30', '21', '20', '15091323262', 'JOHNNY', '8', '\0', '2019-09-01 17:18:25', 'MY HUSBAND', '2019-09-01 17:30:55');
INSERT INTO `contact` VALUES ('31', '23', '20', '15091323262', 'JOHNNY', '7', '\0', '2019-09-20 23:50:32', 'BROTHERS', '2019-09-20 23:51:09');
INSERT INTO `contact` VALUES ('32', '20', '23', '18791004529', 'XUN', '8', '\0', '2019-09-20 23:50:32', 'SISTERS', '2019-09-20 23:51:09');

-- ----------------------------
-- Table structure for login_logs
-- ----------------------------
DROP TABLE IF EXISTS `login_logs`;
CREATE TABLE `login_logs` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CREATE_TIME` datetime NOT NULL,
  `USERNAME` varchar(255) NOT NULL,
  `STATE` int(11) NOT NULL COMMENT '1、成功，0、失败',
  `IP` varchar(255) NOT NULL,
  `IP_LOCATION` varchar(255) NOT NULL,
  `GEOMETRY` geometry DEFAULT NULL,
  `MAC` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1654 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of login_logs
-- ----------------------------
INSERT INTO `login_logs` VALUES ('1636', '2019-10-08 10:48:41', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1637', '2019-10-08 11:01:41', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1638', '2019-10-08 11:04:03', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1639', '2019-10-08 11:05:33', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1640', '2019-10-08 11:39:43', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1641', '2019-10-08 11:48:43', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1642', '2019-10-08 11:49:44', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1643', '2019-10-08 16:32:01', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.557966)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1644', '2019-10-08 16:37:45', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.557966)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1645', '2019-10-08 16:46:57', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.557966)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1646', '2019-10-08 16:52:17', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.557966)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1647', '2019-10-08 16:59:36', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.557966)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1648', '2019-10-08 17:03:04', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.557966)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1649', '2019-10-08 17:05:44', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.557966)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1650', '2019-10-08 17:08:45', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936054 22.557973)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1651', '2019-10-08 17:21:37', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1652', '2019-10-08 17:27:40', '18702963722', '1', '10.208.61.195', '局域网', GeomFromText('POINT(113.936065 22.557922)'), '1A:A6:2B:76:4C:17');
INSERT INTO `login_logs` VALUES ('1653', '2019-10-08 17:48:51', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936048 22.557998)'), '3A:6E:A2:2C:CB:24');

-- ----------------------------
-- Table structure for offline_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `offline_chat_message`;
CREATE TABLE `offline_chat_message` (
  `ID` bigint(20) DEFAULT NULL,
  `CONTACT_ID` bigint(20) DEFAULT NULL,
  `USER_ID` bigint(20) DEFAULT NULL,
  `MD5` varchar(32) DEFAULT NULL,
  `CONTENT` varchar(255) DEFAULT NULL,
  `CONTENT_TYPE` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of offline_chat_message
-- ----------------------------
INSERT INTO `offline_chat_message` VALUES ('1568960182193', '21', '20', '7A7D04824B6C69ED85424952CDF0FC9F', '7A7D04824B6C69ED85424952CDF0FC9F', '2');
INSERT INTO `offline_chat_message` VALUES ('1568960182322', '21', '20', '2DD858EDB9EE90773693E82C8680C34E', '2DD858EDB9EE90773693E82C8680C34E', '2');
INSERT INTO `offline_chat_message` VALUES ('1568962318603', '21', '20', '3F1A492A2A2769F47D7CA2A7A64C5525', '5660', '3');

-- ----------------------------
-- Table structure for offline_group_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `offline_group_chat_message`;
CREATE TABLE `offline_group_chat_message` (
  `ID` bigint(20) DEFAULT NULL,
  `GROUP_ID` bigint(20) DEFAULT NULL,
  `CONTACT_ID` bigint(20) DEFAULT NULL,
  `USER_ID` bigint(20) DEFAULT NULL,
  `MD5` varchar(32) DEFAULT NULL,
  `CONTENT` varchar(255) DEFAULT NULL,
  `CONTENT_TYPE` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of offline_group_chat_message
-- ----------------------------
INSERT INTO `offline_group_chat_message` VALUES ('1568960242177', '1', '20', '20', '4A0169A762CF354E66AF18F6A60504DD', '4A0169A762CF354E66AF18F6A60504DD', '2');
INSERT INTO `offline_group_chat_message` VALUES ('1568960242782', '1', '20', '20', '494700C07BD705A0071EFF29CBDA0514', '494700C07BD705A0071EFF29CBDA0514', '2');
INSERT INTO `offline_group_chat_message` VALUES ('1568960242624', '1', '20', '20', 'BC39ADD06F57FAB1245F22EF134A57E6', 'BC39ADD06F57FAB1245F22EF134A57E6', '2');
INSERT INTO `offline_group_chat_message` VALUES ('1568960322967', '1', '20', '20', '90A1C4304CEC0CDC4C2200A4393CB843', '90A1C4304CEC0CDC4C2200A4393CB843', '2');
INSERT INTO `offline_group_chat_message` VALUES ('1568960322830', '1', '20', '20', '0F3AE06B4161001AE8E7A05EC7604866', '0F3AE06B4161001AE8E7A05EC7604866', '4');
INSERT INTO `offline_group_chat_message` VALUES ('1568960323007', '1', '20', '20', '7EA9A789B969B35755E6664A41ABC10B', '7EA9A789B969B35755E6664A41ABC10B', '2');
INSERT INTO `offline_group_chat_message` VALUES ('1568960353650', '1', '20', '20', '5D72CBDBC0735B96DBD657008891AF96', '6000', '3');
INSERT INTO `offline_group_chat_message` VALUES ('1569404409357', '1', '21', '21', 'DADB876665A7BC08654E3A88A78AF8A2', 'DADB876665A7BC08654E3A88A78AF8A2', '2');

-- ----------------------------
-- Table structure for offline_message
-- ----------------------------
DROP TABLE IF EXISTS `offline_message`;
CREATE TABLE `offline_message` (
  `CONTACT_ID` bigint(20) DEFAULT NULL,
  `RECEIVER_ID` bigint(20) DEFAULT NULL,
  `MD5` varchar(255) DEFAULT NULL,
  `CONTENT` varchar(255) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `CONTENT_TYPE` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of offline_message
-- ----------------------------

-- ----------------------------
-- Table structure for service
-- ----------------------------
DROP TABLE IF EXISTS `service`;
CREATE TABLE `service` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `HOST` varchar(255) NOT NULL,
  `PORT` int(11) NOT NULL,
  `SCHEMA` varchar(255) NOT NULL,
  `APPLICATION_ID` int(11) NOT NULL COMMENT '0、SQUIRREL',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of service
-- ----------------------------
INSERT INTO `service` VALUES ('1', '192.168.1.116', '8011', 'https', '0');
INSERT INTO `service` VALUES ('2', '192.168.1.116', '8012', 'tcp', '0');
INSERT INTO `service` VALUES ('3', '10.208.60.190', '8011', 'https', '0');
INSERT INTO `service` VALUES ('4', '10.208.60.190', '8012', 'tcp', '0');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USERNAME` varchar(255) NOT NULL,
  `PASSWORD` varchar(255) NOT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `NICKNAME` varchar(255) DEFAULT NULL,
  `ROLETYPE` int(11) DEFAULT NULL COMMENT '角色类型，0：普通用户，1：管理员',
  PRIMARY KEY (`ID`),
  KEY `INDEX_USER` (`ID`,`USERNAME`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('20', '15091323262', '8DDCFF3A80F4189CA1C9D4D902C3C909', null, 'JOHNNY', '0');
INSERT INTO `user` VALUES ('21', '18702963722', '8DDCFF3A80F4189CA1C9D4D902C3C909', null, 'LAN', '0');
INSERT INTO `user` VALUES ('22', '13309195750', '8DDCFF3A80F4189CA1C9D4D902C3C909', null, 'MIN', '0');
INSERT INTO `user` VALUES ('23', '18791004529', '8DDCFF3A80F4189CA1C9D4D902C3C909', null, 'XUN', '0');
INSERT INTO `user` VALUES ('24', '15029393930', '8DDCFF3A80F4189CA1C9D4D902C3C909', null, 'JULIUS', '0');
INSERT INTO `user` VALUES ('25', 'admin', '8DDCFF3A80F4189CA1C9D4D902C3C909', null, 'JOHNNY', '1');
INSERT INTO `user` VALUES ('26', '15809257128', '8DDCFF3A80F4189CA1C9D4D902C3C909', null, 'ZUFIER', '0');
INSERT INTO `user` VALUES ('27', '15891602599', '8DDCFF3A80F4189CA1C9D4D902C3C909', null, 'YUU', '0');
