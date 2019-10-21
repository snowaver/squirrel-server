/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50722
Source Host           : localhost:3306
Source Database       : squirrel

Target Server Type    : MYSQL
Target Server Version : 50722
File Encoding         : 65001

Date: 2019-10-21 18:07:28
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chat_group
-- ----------------------------
INSERT INTO `chat_group` VALUES ('31', '\0', '2019-10-21 16:53:21', '20', '2019-10-21 16:53:21', '0', 'A');

-- ----------------------------
-- Table structure for chat_group_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_group_message`;
CREATE TABLE `chat_group_message` (
  `ID` bigint(20) DEFAULT NULL,
  `GROUP_ID` bigint(20) DEFAULT NULL,
  `SYNC_ID` bigint(20) DEFAULT NULL,
  `CONTACT_ID` bigint(20) DEFAULT NULL,
  `USER_ID` bigint(20) DEFAULT NULL,
  `MD5` varchar(32) DEFAULT NULL,
  `CONTENT` varchar(255) DEFAULT NULL,
  `CONTENT_TYPE` int(11) DEFAULT NULL,
  `TRANSPORT_STATE` int(11) DEFAULT NULL COMMENT '传输状态：1、已接收，3、已发送'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chat_group_message
-- ----------------------------

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
INSERT INTO `chat_group_sync` VALUES ('1', '20', '1', '31', '2019-10-21 16:53:21');
INSERT INTO `chat_group_sync` VALUES ('2', '20', '5', '31', '2019-10-21 16:53:39');
INSERT INTO `chat_group_sync` VALUES ('1', '21', '4', '31', '2019-10-21 16:53:39');
INSERT INTO `chat_group_sync` VALUES ('3', '20', '7', '31', '2019-10-21 16:53:50');
INSERT INTO `chat_group_sync` VALUES ('2', '21', '7', '31', '2019-10-21 16:53:50');
INSERT INTO `chat_group_sync` VALUES ('4', '20', '5', '31', '2019-10-21 16:54:02');
INSERT INTO `chat_group_sync` VALUES ('3', '21', '4', '31', '2019-10-21 16:54:02');

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
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chat_group_user
-- ----------------------------
INSERT INTO `chat_group_user` VALUES ('44', '\0', '2019-10-21 16:53:21', '20', '2019-10-21 16:53:21', '20', '31', '20', 'JOHNNY');
INSERT INTO `chat_group_user` VALUES ('45', '', '2019-10-21 16:53:39', '21', '2019-10-21 16:53:50', '21', '31', '21', 'LAN');
INSERT INTO `chat_group_user` VALUES ('46', '\0', '2019-10-21 16:54:02', '21', '2019-10-21 16:54:02', '21', '31', '21', 'LAN');

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `ID` bigint(20) DEFAULT NULL,
  `SYNC_ID` bigint(20) DEFAULT NULL,
  `CONTACT_ID` bigint(20) DEFAULT NULL,
  `USER_ID` bigint(20) DEFAULT NULL,
  `MD5` varchar(32) DEFAULT NULL,
  `CONTENT` varchar(255) DEFAULT NULL,
  `CONTENT_TYPE` int(11) DEFAULT NULL,
  `TRANSPORT_STATE` int(11) DEFAULT NULL COMMENT '传输状态：1、已接收，3、已发送'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES ('1571643521927', '1', '21', '20', '2DA58CF8378BDA797B67E4CFC2163D84', '2DA58CF8378BDA797B67E4CFC2163D84', '2', '3');
INSERT INTO `chat_message` VALUES ('1571643521927', '1', '20', '21', '2DA58CF8378BDA797B67E4CFC2163D84', '2DA58CF8378BDA797B67E4CFC2163D84', '2', '1');
INSERT INTO `chat_message` VALUES ('1571647884603', '2', '21', '20', 'F9729F60F77435009500F2DB9EAF1B27', 'F9729F60F77435009500F2DB9EAF1B27', '2', '3');
INSERT INTO `chat_message` VALUES ('1571647884603', '2', '20', '21', 'F9729F60F77435009500F2DB9EAF1B27', 'F9729F60F77435009500F2DB9EAF1B27', '2', '1');
INSERT INTO `chat_message` VALUES ('1571647914600', '3', '21', '20', 'E977EF9854202CD65750C472441C906E', 'E977EF9854202CD65750C472441C906E', '4', '3');
INSERT INTO `chat_message` VALUES ('1571647914600', '3', '20', '21', 'E977EF9854202CD65750C472441C906E', 'E977EF9854202CD65750C472441C906E', '4', '1');

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
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of contact
-- ----------------------------
INSERT INTO `contact` VALUES ('37', '21', '20', '15091323262', 'JOHNNY', '7', '\0', '2019-10-21 15:21:52', 'FAMILY', '2019-10-21 15:22:44');
INSERT INTO `contact` VALUES ('38', '20', '21', '18702963722', 'LAN', '8', '\0', '2019-10-21 15:21:52', 'FAMILY', '2019-10-21 15:22:44');

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
) ENGINE=InnoDB AUTO_INCREMENT=1787 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of login_logs
-- ----------------------------
INSERT INTO `login_logs` VALUES ('1731', '2019-10-21 15:17:00', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936052 22.557995)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1732', '2019-10-21 15:19:21', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1733', '2019-10-21 15:46:31', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1734', '2019-10-21 15:47:42', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1735', '2019-10-21 15:49:03', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1736', '2019-10-21 15:49:13', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1737', '2019-10-21 15:49:23', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1738', '2019-10-21 15:49:33', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1739', '2019-10-21 15:49:43', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1740', '2019-10-21 15:49:53', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1741', '2019-10-21 15:50:03', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1742', '2019-10-21 15:50:13', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1743', '2019-10-21 15:50:23', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1744', '2019-10-21 16:35:23', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1745', '2019-10-21 16:35:33', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1746', '2019-10-21 16:35:43', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1747', '2019-10-21 16:41:12', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1748', '2019-10-21 16:41:13', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1749', '2019-10-21 16:41:23', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1750', '2019-10-21 16:41:33', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1751', '2019-10-21 16:41:43', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1752', '2019-10-21 16:41:53', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1753', '2019-10-21 16:42:03', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1754', '2019-10-21 16:42:13', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1755', '2019-10-21 16:42:23', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1756', '2019-10-21 16:42:33', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1757', '2019-10-21 16:42:43', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1758', '2019-10-21 16:42:53', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1759', '2019-10-21 16:43:03', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1760', '2019-10-21 16:43:13', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1761', '2019-10-21 16:43:43', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1762', '2019-10-21 16:43:53', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1763', '2019-10-21 16:44:03', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1764', '2019-10-21 16:44:13', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1765', '2019-10-21 16:44:23', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1766', '2019-10-21 16:44:33', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1767', '2019-10-21 16:44:43', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1768', '2019-10-21 16:44:53', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1769', '2019-10-21 16:45:24', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1770', '2019-10-21 16:45:32', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1771', '2019-10-21 16:45:33', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1772', '2019-10-21 16:45:43', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1773', '2019-10-21 16:45:53', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1774', '2019-10-21 16:46:03', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1775', '2019-10-21 16:46:13', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1776', '2019-10-21 16:46:23', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1777', '2019-10-21 16:46:33', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1778', '2019-10-21 16:46:44', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1779', '2019-10-21 16:46:53', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1780', '2019-10-21 16:47:03', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1781', '2019-10-21 16:47:34', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936039 22.55796)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1782', '2019-10-21 16:47:42', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1783', '2019-10-21 17:27:07', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1784', '2019-10-21 17:28:24', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.93605 22.558006)'), '3A:6E:A2:2C:CB:24');
INSERT INTO `login_logs` VALUES ('1785', '2019-10-21 17:53:25', '18702963722', '1', '10.208.60.190', '局域网', null, '02:00:00:44:55:66');
INSERT INTO `login_logs` VALUES ('1786', '2019-10-21 17:53:35', '15091323262', '1', '10.208.60.236', '局域网', GeomFromText('POINT(113.936037 22.557968)'), '3A:6E:A2:2C:CB:24');

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
