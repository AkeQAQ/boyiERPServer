/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.8.252
 Source Server Type    : MySQL
 Source Server Version : 50735
 Source Host           : 192.168.8.252:3306
 Source Schema         : boyi

 Target Server Type    : MySQL
 Target Server Version : 50735
 File Encoding         : 65001

 Date: 13/05/2023 10:46:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for analysis_request
-- ----------------------------
DROP TABLE IF EXISTS `analysis_request`;
CREATE TABLE `analysis_request`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `url` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求URL',
  `ip` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '来源IP',
  `class_method` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '执行的class方法',
  `cast` bigint(20) NULL DEFAULT NULL COMMENT '毫秒单位',
  `user_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求用户',
  `created_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 792597 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for base_customer
-- ----------------------------
DROP TABLE IF EXISTS `base_customer`;
CREATE TABLE `base_customer`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '客户名称',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_base_customer_name_unique`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 152 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_department
-- ----------------------------
DROP TABLE IF EXISTS `base_department`;
CREATE TABLE `base_department`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '部门名称。utf8 3字节，最多存储10个中文',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `base_department_name_uindex`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '基础模块-部门管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_material
-- ----------------------------
DROP TABLE IF EXISTS `base_material`;
CREATE TABLE `base_material`  (
  `group_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '物料分组CODE',
  `id` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT '主键，物料唯一编码',
  `name` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `unit` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `specs` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT 'NULL值为正常物料，-1为失效物料',
  `pic_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片路径',
  `searchId` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '由groupId+id组成的，提供搜索的ID',
  `sub_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `big_unit` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '入库大单位',
  `unit_radio` int(11) NULL DEFAULT NULL COMMENT '1入库大单位=N个基本单位的N数值',
  `low_warning_line` double NULL DEFAULT NULL COMMENT '低预警线',
  `video_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '视频url',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_material_group
-- ----------------------------
DROP TABLE IF EXISTS `base_material_group`;
CREATE TABLE `base_material_group`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `parent_id` int(11) NULL DEFAULT NULL COMMENT '分组的上级ID',
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `code` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '物料分组前缀编码',
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `auto_sub_id` int(11) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `repository_group_code_uindex`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 116 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '基础模块-物料分组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_material_same_group
-- ----------------------------
DROP TABLE IF EXISTS `base_material_same_group`;
CREATE TABLE `base_material_same_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '组名',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_bmsg_name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_material_same_group_detail
-- ----------------------------
DROP TABLE IF EXISTS `base_material_same_group_detail`;
CREATE TABLE `base_material_same_group_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `material_id` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `group_id` bigint(20) NULL DEFAULT NULL COMMENT 'same_group外键',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `fk_bmsgd_mid`(`material_id`) USING BTREE,
  INDEX `fk_bmsgd_gid`(`group_id`) USING BTREE,
  CONSTRAINT `fk_bmsgd_gid` FOREIGN KEY (`group_id`) REFERENCES `base_material_same_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_bmsgd_mid` FOREIGN KEY (`material_id`) REFERENCES `base_material` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_supplier
-- ----------------------------
DROP TABLE IF EXISTS `base_supplier`;
CREATE TABLE `base_supplier`  (
  `id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键，供应商唯一编码',
  `group_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '供应商分组CODE',
  `sub_id` int(11) NOT NULL,
  `group_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商分组名称',
  `name` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商公司名称',
  `address` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地址',
  `mobile` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `searchId` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '由groupId+id组成的，提供搜索的ID',
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `tax` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '含税',
  `zq` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '账期',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `fax` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '传真',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_supplier_group
-- ----------------------------
DROP TABLE IF EXISTS `base_supplier_group`;
CREATE TABLE `base_supplier_group`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `parent_id` int(11) NULL DEFAULT NULL COMMENT '分组的上级ID',
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商分组前缀编码',
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `auto_sub_id` int(11) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `repository_group_code_uindex`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '基础模块-供应商分组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_supplier_material
-- ----------------------------
DROP TABLE IF EXISTS `base_supplier_material`;
CREATE TABLE `base_supplier_material`  (
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '物料ID',
  `price` double NULL DEFAULT NULL COMMENT '单价',
  `start_date` date NULL DEFAULT NULL COMMENT '生效日期（包含）',
  `end_date` date NULL DEFAULT NULL COMMENT '失效日期（包含）默认100年之后，有同供应商，同物料第二条记录，需要修改该字段',
  `comment` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `base_supplier_material_supplier_id_material_id_index`(`supplier_id`, `material_id`) USING BTREE,
  INDEX `base_supplier_material_material_id_index`(`material_id`) USING BTREE,
  INDEX `base_supplier_material_supplier_id_index`(`supplier_id`) USING BTREE,
  INDEX `bsm_index_startdate`(`start_date`) USING BTREE,
  INDEX `bsm_index_enddate`(`end_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44729 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '供应商-物料报价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_supplier_material_copy
-- ----------------------------
DROP TABLE IF EXISTS `base_supplier_material_copy`;
CREATE TABLE `base_supplier_material_copy`  (
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '物料ID',
  `price` double NULL DEFAULT NULL COMMENT '单价',
  `start_date` date NULL DEFAULT NULL COMMENT '生效日期（包含）',
  `end_date` date NULL DEFAULT NULL COMMENT '失效日期（包含）默认100年之后，有同供应商，同物料第二条记录，需要修改该字段',
  `comment` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `base_supplier_material_copy_supplier_id_material_id_index`(`supplier_id`, `material_id`) USING BTREE,
  INDEX `base_supplier_material_copy_material_id_index`(`material_id`) USING BTREE,
  INDEX `base_supplier_material_copy_supplier_id_index`(`supplier_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42991 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '供应商-物料报价-copy表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for base_unit
-- ----------------------------
DROP TABLE IF EXISTS `base_unit`;
CREATE TABLE `base_unit`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '单位唯一编码',
  `name` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL COMMENT '创建日期',
  `updated` datetime NULL DEFAULT NULL COMMENT '修改日期',
  `priority` int(11) NULL DEFAULT -1 COMMENT '优先级，默认-1，越大优先级越高，显示越靠前',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 90 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '基础模块-计量单位管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for buy_material_supplier
-- ----------------------------
DROP TABLE IF EXISTS `buy_material_supplier`;
CREATE TABLE `buy_material_supplier`  (
  `inner_material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '内部物料ID',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '供应商名称，冗余字段',
  `supplier_material_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '供应商的物料编码',
  `supplier_material_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商的物料名称',
  `supplier_material_price` double NULL DEFAULT NULL COMMENT '供应商的物料价格',
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `created` datetime NULL DEFAULT NULL COMMENT '创建日期',
  `updated` datetime NULL DEFAULT NULL COMMENT '修改日期',
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idex_bms_material`(`inner_material_id`, `supplier_id`, `supplier_material_id`) USING BTREE COMMENT '唯一索引',
  INDEX `index_bms_materialId`(`inner_material_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for cost_of_labour
-- ----------------------------
DROP TABLE IF EXISTS `cost_of_labour`;
CREATE TABLE `cost_of_labour`  (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `produce_product_constituent_id` bigint(20) NULL DEFAULT NULL COMMENT '组成结构外键',
  `cost_of_labour_type_id` bigint(20) NULL DEFAULT NULL COMMENT '工价类型外键',
  `price_date` date NULL DEFAULT NULL COMMENT '工价日期',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cost_of_labour_detail
-- ----------------------------
DROP TABLE IF EXISTS `cost_of_labour_detail`;
CREATE TABLE `cost_of_labour_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `foreign_id` bigint(20) NULL DEFAULT NULL COMMENT '工价表外键',
  `cost_of_labour_processes_id` bigint(20) NULL DEFAULT NULL COMMENT '工序标外键',
  `pieces` decimal(10, 2) NULL DEFAULT NULL COMMENT '片数',
  `real_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '实际最终价格',
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 97 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cost_of_labour_processes
-- ----------------------------
DROP TABLE IF EXISTS `cost_of_labour_processes`;
CREATE TABLE `cost_of_labour_processes`  (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `cost_of_labour_type_id` int(11) NULL DEFAULT NULL COMMENT 'cost_of_labour_type的外键ID',
  `processes_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工序名称',
  `low_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '保底价格/双',
  `pieces_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '片数价格/双',
  `start_date` date NULL DEFAULT NULL COMMENT '起始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核，1：未审核',
  `created` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建时间',
  `updated` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最后修改时间',
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最后修改人',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cost_of_labour_type
-- ----------------------------
DROP TABLE IF EXISTS `cost_of_labour_type`;
CREATE TABLE `cost_of_labour_type`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `type_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工价类别名称',
  `role_id` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色IDS',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `seq` int(11) NULL DEFAULT NULL COMMENT '部门顺序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_colt_unique_type_name`(`type_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for external_account_base_department
-- ----------------------------
DROP TABLE IF EXISTS `external_account_base_department`;
CREATE TABLE `external_account_base_department`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '部门名称。utf8 3字节，最多存储10个中文',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ea_base_department_name_uindex`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '基础模块-部门管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_base_material
-- ----------------------------
DROP TABLE IF EXISTS `external_account_base_material`;
CREATE TABLE `external_account_base_material`  (
  `group_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '物料分组CODE',
  `id` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT '主键，物料唯一编码',
  `name` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `unit` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `specs` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `pic_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片路径',
  `searchId` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '由groupId+id组成的，提供搜索的ID',
  `sub_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `big_unit` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '入库大单位',
  `unit_radio` int(11) NULL DEFAULT NULL COMMENT '1入库大单位=N个基本单位的N数值',
  `low_warning_line` double NULL DEFAULT NULL COMMENT '低预警线',
  `video_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '视频URL',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_base_material_group
-- ----------------------------
DROP TABLE IF EXISTS `external_account_base_material_group`;
CREATE TABLE `external_account_base_material_group`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `parent_id` int(11) NULL DEFAULT NULL COMMENT '分组的上级ID',
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `code` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '物料分组前缀编码',
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `auto_sub_id` int(11) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `external_repository_group_code_uindex`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '基础模块-物料分组表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_base_supplier
-- ----------------------------
DROP TABLE IF EXISTS `external_account_base_supplier`;
CREATE TABLE `external_account_base_supplier`  (
  `id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键，供应商唯一编码',
  `group_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '供应商分组CODE',
  `sub_id` int(11) NOT NULL,
  `group_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商分组名称',
  `name` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商公司名称',
  `address` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地址',
  `mobile` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `searchId` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '由groupId+id组成的，提供搜索的ID',
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `tax` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '含税',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `zq` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '账期',
  `fax` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '传真',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_base_supplier_group
-- ----------------------------
DROP TABLE IF EXISTS `external_account_base_supplier_group`;
CREATE TABLE `external_account_base_supplier_group`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `parent_id` int(11) NULL DEFAULT NULL COMMENT '分组的上级ID',
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商分组前缀编码',
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `auto_sub_id` int(11) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ea_repository_group_code_uindex`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '基础模块-供应商分组表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_base_supplier_material
-- ----------------------------
DROP TABLE IF EXISTS `external_account_base_supplier_material`;
CREATE TABLE `external_account_base_supplier_material`  (
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '物料ID',
  `price` double NULL DEFAULT NULL COMMENT '单价',
  `start_date` date NULL DEFAULT NULL COMMENT '生效日期（包含）',
  `end_date` date NULL DEFAULT NULL COMMENT '失效日期（包含）默认100年之后，有同供应商，同物料第二条记录，需要修改该字段',
  `comment` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ea_base_supplier_material_supplier_id_material_id_index`(`supplier_id`, `material_id`) USING BTREE,
  INDEX `ea_base_supplier_material_material_id_index`(`material_id`) USING BTREE,
  INDEX `ea_base_supplier_material_supplier_id_index`(`supplier_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 131 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '供应商-物料报价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_base_unit
-- ----------------------------
DROP TABLE IF EXISTS `external_account_base_unit`;
CREATE TABLE `external_account_base_unit`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '单位唯一编码',
  `name` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL COMMENT '创建日期',
  `updated` datetime NULL DEFAULT NULL COMMENT '修改日期',
  `priority` int(11) NULL DEFAULT -1 COMMENT '优先级,默认-1',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '基础模块-计量单位管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_repository_buyin_document
-- ----------------------------
DROP TABLE IF EXISTS `external_account_repository_buyin_document`;
CREATE TABLE `external_account_repository_buyin_document`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID外键',
  `supplier_document_num` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商单据编号',
  `buy_in_date` date NULL DEFAULT NULL COMMENT '入库日期',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `source_type` int(11) NULL DEFAULT NULL COMMENT '入库来源',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ea_rbd_index_supplierId`(`supplier_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2305130001 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-采购入库单据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_repository_buyin_document_detail
-- ----------------------------
DROP TABLE IF EXISTS `external_account_repository_buyin_document_detail`;
CREATE TABLE `external_account_repository_buyin_document_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '物料id',
  `document_id` bigint(20) NOT NULL COMMENT '采购入库单的外键',
  `num` double NULL DEFAULT NULL COMMENT '采购入库数量',
  `comment` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '冗余一个供应商ID',
  `order_seq` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单下推才有-订单号',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '订单下推才有-采购订单的ID',
  `price_date` date NULL DEFAULT NULL COMMENT '价格对应的日期。\n采购入库的，price_date=buy_in_date\n采购订单入库的，price_date=order_date',
  `order_detail_id` bigint(20) NULL DEFAULT NULL COMMENT '订单详情ID',
  `radio_num` double NULL DEFAULT NULL COMMENT '假如有换算系数的话，存储换算后的数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 245 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-采购入库单-详情内容' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_repository_pick_material
-- ----------------------------
DROP TABLE IF EXISTS `external_account_repository_pick_material`;
CREATE TABLE `external_account_repository_pick_material`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pick_date` date NULL DEFAULT NULL COMMENT '领料日期',
  `status` int(11) NULL DEFAULT NULL,
  `department_id` bigint(20) NOT NULL COMMENT '领料部门ID',
  `pick_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '领料人名',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `produce_doc_num` int(11) NULL DEFAULT NULL COMMENT '生产计划单号',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注，目前用与填写批次号',
  `batch_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'produce_batch表的batch_id(生产序号)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2305130001 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-领料模块' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_repository_pick_material_detail
-- ----------------------------
DROP TABLE IF EXISTS `external_account_repository_pick_material_detail`;
CREATE TABLE `external_account_repository_pick_material_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '详情信息ID主键',
  `document_id` bigint(20) NOT NULL COMMENT '关联的领料单据表外键ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '领料的物料ID',
  `num` double NOT NULL COMMENT '领料数目',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 159 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-领料模块-详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_repository_send_out_goods
-- ----------------------------
DROP TABLE IF EXISTS `external_account_repository_send_out_goods`;
CREATE TABLE `external_account_repository_send_out_goods`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `send_date` date NULL DEFAULT NULL COMMENT '调整日期',
  `customer_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客户',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '状态，0：已审核，1：暂存，2：审核中，3：重新审核',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2305130001 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_repository_send_out_goods_details
-- ----------------------------
DROP TABLE IF EXISTS `external_account_repository_send_out_goods_details`;
CREATE TABLE `external_account_repository_send_out_goods_details`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `send_id` bigint(20) NULL DEFAULT NULL COMMENT 'send表外键',
  `product_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品编码',
  `num` decimal(8, 2) NULL DEFAULT NULL COMMENT '数量',
  `price` decimal(7, 2) NULL DEFAULT NULL COMMENT '单价',
  `amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '金额',
  `product_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `unit` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_repository_stock
-- ----------------------------
DROP TABLE IF EXISTS `external_account_repository_stock`;
CREATE TABLE `external_account_repository_stock`  (
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '物料ID',
  `num` double NULL DEFAULT NULL COMMENT '库存数量',
  `updated` datetime NULL DEFAULT NULL COMMENT '最后修改日期',
  PRIMARY KEY (`material_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '库存表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for external_account_sys_init_stock
-- ----------------------------
DROP TABLE IF EXISTS `external_account_sys_init_stock`;
CREATE TABLE `external_account_sys_init_stock`  (
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `num` double NULL DEFAULT NULL,
  PRIMARY KEY (`material_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for finance_close
-- ----------------------------
DROP TABLE IF EXISTS `finance_close`;
CREATE TABLE `finance_close`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `close_date` date NULL DEFAULT NULL,
  `created` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库关账模块' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_summary
-- ----------------------------
DROP TABLE IF EXISTS `finance_summary`;
CREATE TABLE `finance_summary`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `summary_date` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '对账月份',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `buy_net_in_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '净入库（采购入库-采购退料）',
  `pay_shoes_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '赔鞋金额',
  `fine_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '罚款金额',
  `test_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '检测费金额',
  `tax_supplement` decimal(9, 2) NULL DEFAULT NULL COMMENT '补税点金额',
  `tax_deduction` decimal(9, 2) NULL DEFAULT NULL COMMENT '扣税点金额',
  `round_down` decimal(9, 2) NULL DEFAULT NULL COMMENT '抹零',
  `need_pay_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '应付金额',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '0:已结账，1：未结账',
  `pic_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '结账单图片',
  `change_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '调整金额',
  `buy_in_amount` decimal(9, 2) NULL DEFAULT NULL,
  `buy_out_amount` decimal(9, 2) NULL DEFAULT NULL,
  `settle_date` date NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 643 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for finance_summary_details
-- ----------------------------
DROP TABLE IF EXISTS `finance_summary_details`;
CREATE TABLE `finance_summary_details`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `summary_id` bigint(20) NULL DEFAULT NULL COMMENT 'finance_summary的外键',
  `pay_date` datetime NULL DEFAULT NULL COMMENT '付款时间',
  `pay_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '付款金额',
  `pay_type` int(11) NULL DEFAULT NULL COMMENT '0:对公转账,1:对公承兑,2:对私转账,3:对私承兑',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `document_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_fsd_id`(`summary_id`) USING BTREE,
  CONSTRAINT `fk_fsd_id` FOREIGN KEY (`summary_id`) REFERENCES `finance_summary` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for finance_summary_filters
-- ----------------------------
DROP TABLE IF EXISTS `finance_summary_filters`;
CREATE TABLE `finance_summary_filters`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_change
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_change`;
CREATE TABLE `finance_supplier_change`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `change_date` date NULL DEFAULT NULL COMMENT '调整日期',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '状态，0：已审核，1：暂存，2：审核中，3：重新审核',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fsc_fk_sid`(`supplier_id`) USING BTREE,
  CONSTRAINT `fsc_fk_sid` FOREIGN KEY (`supplier_id`) REFERENCES `base_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_change_details
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_change_details`;
CREATE TABLE `finance_supplier_change_details`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `change_id` bigint(20) NULL DEFAULT NULL COMMENT 'change表外键',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '物料ID外键',
  `num` decimal(8, 2) NULL DEFAULT NULL COMMENT '数量',
  `change_price` decimal(7, 2) NULL DEFAULT NULL COMMENT '调整单价',
  `change_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '调整金额',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_fine
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_fine`;
CREATE TABLE `finance_supplier_fine`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `fine_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '罚款金额',
  `fine_date` date NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `document_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `pic_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `fine_reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '罚款原因',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `fsrd_uniq_docN`(`document_num`) USING BTREE,
  INDEX `fsrd_fk_sid`(`supplier_id`) USING BTREE,
  CONSTRAINT `finance_supplier_fine_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `base_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_payshoes
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_payshoes`;
CREATE TABLE `finance_supplier_payshoes`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID',
  `pay_date` date NULL DEFAULT NULL COMMENT '罚款日期',
  `take_status` int(11) NULL DEFAULT NULL COMMENT '状态。0：已拿, 1: 未拿',
  `pic_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '单据图片',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核、1:暂存、2：审核中、3：重新审核',
  `document_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '罚款单号，唯一',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_fsp_docNum`(`document_num`) USING BTREE,
  INDEX `document_num`(`document_num`, `id`) USING BTREE,
  INDEX `fsp_fk_sid`(`supplier_id`) USING BTREE,
  CONSTRAINT `fsp_fk_sid` FOREIGN KEY (`supplier_id`) REFERENCES `base_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_payshoes_details
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_payshoes_details`;
CREATE TABLE `finance_supplier_payshoes_details`  (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `pay_shoes_id` bigint(20) NULL DEFAULT NULL COMMENT '赔鞋主表外键',
  `customer_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客户货号',
  `pay_number` decimal(7, 2) NULL DEFAULT NULL COMMENT '赔鞋数量',
  `pay_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '赔鞋金额',
  `pay_type` int(11) NULL DEFAULT NULL COMMENT '赔鞋类型。0：大货，1：残鞋',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE,
  INDEX `fk_fsp_psi`(`pay_shoes_id`) USING BTREE,
  CONSTRAINT `fk_fsp_psi` FOREIGN KEY (`pay_shoes_id`) REFERENCES `finance_supplier_payshoes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_round_down
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_round_down`;
CREATE TABLE `finance_supplier_round_down`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `round_down_amount` decimal(6, 2) NULL DEFAULT NULL COMMENT '抹零金额',
  `round_down_date` date NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fsrd_fk_sid`(`supplier_id`) USING BTREE,
  CONSTRAINT `fsrd_fk_sid` FOREIGN KEY (`supplier_id`) REFERENCES `base_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_tax_deduction
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_tax_deduction`;
CREATE TABLE `finance_supplier_tax_deduction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `company` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '开票单位',
  `document_date` date NULL DEFAULT NULL COMMENT '开票日期',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '审核状态',
  `document_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票号',
  `pic_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片',
  `deduction_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '税额扣款',
  `tax_point` decimal(5, 3) NULL DEFAULT NULL COMMENT '税点',
  `document_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '开票金额',
  `pay_status` int(11) NULL DEFAULT NULL COMMENT '1:未付、0：已付',
  `pay_date` date NULL DEFAULT NULL COMMENT '付款状态',
  `tax_cal_amount` decimal(7, 2) NULL DEFAULT NULL COMMENT '含税未开票金额',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `fsrd_uniq_docN`(`document_num`) USING BTREE,
  INDEX `fsrd_fk_sid`(`supplier_id`) USING BTREE,
  CONSTRAINT `finance_supplier_tax_deduction_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `base_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_tax_supplement
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_tax_supplement`;
CREATE TABLE `finance_supplier_tax_supplement`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `company` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '开票单位',
  `document_date` date NULL DEFAULT NULL COMMENT '开票日期',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '审核状态',
  `document_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票号',
  `pic_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片',
  `tax_supplement_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '税金额',
  `tax_point` decimal(5, 3) NULL DEFAULT NULL COMMENT '税点',
  `document_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '开票金额',
  `document_no_tax_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '不含税开票金额',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `fsrd_uniq_docN`(`document_num`) USING BTREE,
  INDEX `fsrd_fk_sid`(`supplier_id`) USING BTREE,
  CONSTRAINT `finance_supplier_tax_supplement_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `base_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for finance_supplier_test
-- ----------------------------
DROP TABLE IF EXISTS `finance_supplier_test`;
CREATE TABLE `finance_supplier_test`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `test_amount` decimal(9, 2) NULL DEFAULT NULL COMMENT '检测费金额',
  `test_date` date NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `document_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `fsrd_fk_docNum`(`document_num`) USING BTREE,
  INDEX `fsrd_fk_sid`(`supplier_id`) USING BTREE,
  CONSTRAINT `finance_supplier_test_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `base_supplier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for his_base_supplier_material
-- ----------------------------
DROP TABLE IF EXISTS `his_base_supplier_material`;
CREATE TABLE `his_base_supplier_material`  (
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '物料ID',
  `price` double NULL DEFAULT NULL COMMENT '单价',
  `start_date` date NULL DEFAULT NULL COMMENT '生效日期（包含）',
  `end_date` date NULL DEFAULT NULL COMMENT '失效日期（包含）默认100年之后，有同供应商，同物料第二条记录，需要修改该字段',
  `comment` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '历史-供应商-物料报价表\n' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for his_order_product_order
-- ----------------------------
DROP TABLE IF EXISTS `his_order_product_order`;
CREATE TABLE `his_order_product_order`  (
  `id` bigint(20) NOT NULL COMMENT '自增主键',
  `order_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '我们的订单号',
  `customer_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客户货号',
  `product_num` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '公司货号',
  `product_brand` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品品牌',
  `product_color` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品颜色',
  `product_region` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '品牌区域',
  `order_type` int(11) NULL DEFAULT NULL COMMENT '0：订单\n1: 回单',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `order_number` int(11) NULL DEFAULT NULL COMMENT '订单数量',
  `prepared` int(11) NULL DEFAULT NULL COMMENT '备料完成状态',
  `end_date` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_hopo_order_num_uniqueIndex`(`order_num`) USING BTREE COMMENT '订单号'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for his_produce_batch
-- ----------------------------
DROP TABLE IF EXISTS `his_produce_batch`;
CREATE TABLE `his_produce_batch`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `batch_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '投产序号',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `order_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单序号',
  `status` int(11) NULL DEFAULT NULL,
  `size34` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size35` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size36` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size37` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size38` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size39` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size40` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size41` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size42` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size43` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size44` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size45` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size46` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size47` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `push` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_his_pb_unique`(`batch_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for his_produce_batch_delay
-- ----------------------------
DROP TABLE IF EXISTS `his_produce_batch_delay`;
CREATE TABLE `his_produce_batch_delay`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `material_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `material_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `date` datetime NULL DEFAULT NULL,
  `produce_batch_id` bigint(20) NULL DEFAULT NULL COMMENT '外键',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cost_of_labour_type_id` bigint(20) NULL DEFAULT NULL,
  `cost_of_labour_type_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for his_produce_batch_progress
-- ----------------------------
DROP TABLE IF EXISTS `his_produce_batch_progress`;
CREATE TABLE `his_produce_batch_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `supplier_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `material_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `send_foreign_product_date` datetime NULL DEFAULT NULL,
  `back_foreign_product_date` datetime NULL DEFAULT NULL,
  `out_date` datetime NULL DEFAULT NULL,
  `produce_batch_id` bigint(20) NULL DEFAULT NULL COMMENT 'produce_batch_id外键',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `cost_of_labour_type_id` bigint(20) NULL DEFAULT NULL COMMENT '工序类别外键',
  `cost_of_labour_type_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_accept` int(11) NULL DEFAULT 1 COMMENT '0：代表接受，1:代表没接受。是否下部门接收',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for his_produce_order_material_progress
-- ----------------------------
DROP TABLE IF EXISTS `his_produce_order_material_progress`;
CREATE TABLE `his_produce_order_material_progress`  (
  `order_id` bigint(11) NULL DEFAULT NULL COMMENT '产品订单的ID号，有ID 的是订单报，没ID是补单报',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备料的物料编码',
  `prepared_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '已经报备料的数量',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `in_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '已入库数量',
  `cal_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应报备用料',
  `progress_percent` int(11) NULL DEFAULT NULL COMMENT '进度，0-100',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `complement_status` int(11) NULL DEFAULT NULL COMMENT '补数备料状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1234 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_before_production_progress
-- ----------------------------
DROP TABLE IF EXISTS `order_before_production_progress`;
CREATE TABLE `order_before_production_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_num` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工厂货号',
  `product_brand` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '品牌',
  `created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '状态，0：已审核，1：待审核',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_unique_obpp_numBrand`(`product_num`, `product_brand`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_before_production_progress_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_before_production_progress_detail`;
CREATE TABLE `order_before_production_progress_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `foreign_id` bigint(20) NULL DEFAULT NULL COMMENT '外键',
  `type_id` int(11) NULL DEFAULT NULL COMMENT '10:确认订单，20:客户要求，30:确认鞋',
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最后修改人',
  `is_current` int(64) NULL DEFAULT NULL COMMENT '0：进度再当前，1：进度不在当前',
  `content` varchar(6144) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述内容',
  `created` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `updated` datetime NULL DEFAULT NULL COMMENT '结束日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_buyorder_document
-- ----------------------------
DROP TABLE IF EXISTS `order_buyorder_document`;
CREATE TABLE `order_buyorder_document`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已完成\r\n1: 待完成',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID外键',
  `order_date` date NULL DEFAULT NULL COMMENT '采购日期',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `obd_index_supplierId`(`supplier_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2305130001 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单模块-采购订单单据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_buyorder_document_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_buyorder_document_detail`;
CREATE TABLE `order_buyorder_document_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '物料id',
  `document_id` bigint(20) NOT NULL COMMENT '采购订单的外键',
  `num` double NULL DEFAULT NULL COMMENT '采购入库数量',
  `comment` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '冗余一个供应商ID',
  `done_date` date NULL DEFAULT NULL COMMENT '交货日期',
  `order_seq` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `status` int(11) NULL DEFAULT NULL COMMENT '0:已下推\n1：未下推',
  `order_date` date NULL DEFAULT NULL COMMENT '采购日期',
  `radio_num` double NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `obdd_index_docId`(`document_id`) USING BTREE,
  INDEX `obdd_index_matId`(`material_id`) USING BTREE,
  INDEX `obdd_index_supid`(`supplier_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 57875 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单模块-采购订单-详情内容' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_product_order
-- ----------------------------
DROP TABLE IF EXISTS `order_product_order`;
CREATE TABLE `order_product_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '我们的订单号',
  `customer_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客户货号',
  `product_num` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '公司货号',
  `product_brand` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品品牌',
  `product_color` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品颜色',
  `product_region` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '品牌区域',
  `order_type` int(11) NULL DEFAULT NULL COMMENT '0：订单\n1: 回单,2:取消',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `order_number` int(11) NULL DEFAULT NULL COMMENT '订单数量',
  `prepared` int(11) NULL DEFAULT NULL COMMENT '备料完成状态:0:备料完成,1:备料未确认,2:备料已确认',
  `end_date` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '货期',
  `shoe_last` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `material_bom_id` bigint(20) NULL DEFAULT NULL,
  `technology_bom_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_order_num_uniqueIndex`(`order_num`) USING BTREE COMMENT '订单号',
  INDEX `index_fk_mbom_id`(`material_bom_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4128 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_productprice_pre
-- ----------------------------
DROP TABLE IF EXISTS `order_productprice_pre`;
CREATE TABLE `order_productprice_pre`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `company_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公司货号',
  `customer` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客户公司名称',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `save_path` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件存储路径',
  `status` int(11) NULL DEFAULT NULL,
  `price` double NULL DEFAULT NULL,
  `upload_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `excel_json` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `real_json` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `real_price` double NULL DEFAULT NULL,
  `price_last_update_date` datetime NULL DEFAULT NULL,
  `price_last_update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `real_price_last_update_date` datetime NULL DEFAULT NULL,
  `real_price_last_update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `deal_price` double(10, 2) NULL DEFAULT NULL,
  `yk_status` int(11) NULL DEFAULT NULL COMMENT '0:代表 正常，1:代表 无法计算，2:代表 盈利，3:代表 亏损',
  `caiduan_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '裁断价格',
  `zhenche_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '针车价格',
  `cx_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '成型价格',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 648 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '新产品成本核算-报价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for produce_batch
-- ----------------------------
DROP TABLE IF EXISTS `produce_batch`;
CREATE TABLE `produce_batch`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `batch_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '投产序号',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `order_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单序号',
  `status` int(11) NULL DEFAULT NULL,
  `size34` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size35` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size36` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size37` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size38` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size39` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size40` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size41` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size42` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size43` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size44` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size45` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size46` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size47` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `push` int(11) NULL DEFAULT 1 COMMENT '0:已下推，1：未下推采购订单',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_pb_unique`(`batch_id`) USING BTREE,
  INDEX `index_pb_on`(`order_num`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5140 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for produce_batch_delay
-- ----------------------------
DROP TABLE IF EXISTS `produce_batch_delay`;
CREATE TABLE `produce_batch_delay`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `material_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `material_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `date` datetime NULL DEFAULT NULL,
  `produce_batch_id` bigint(20) NULL DEFAULT NULL COMMENT '外键',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cost_of_labour_type_id` bigint(20) NULL DEFAULT NULL,
  `cost_of_labour_type_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pbd_foreign_key_coltid`(`cost_of_labour_type_id`) USING BTREE,
  INDEX `index_pbd_pbid`(`produce_batch_id`) USING BTREE,
  CONSTRAINT `pbd_foreign_key_coltid` FOREIGN KEY (`cost_of_labour_type_id`) REFERENCES `cost_of_labour_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for produce_batch_progress
-- ----------------------------
DROP TABLE IF EXISTS `produce_batch_progress`;
CREATE TABLE `produce_batch_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `supplier_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `material_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `send_foreign_product_date` datetime NULL DEFAULT NULL,
  `back_foreign_product_date` datetime NULL DEFAULT NULL,
  `out_date` datetime NULL DEFAULT NULL,
  `produce_batch_id` bigint(20) NULL DEFAULT NULL COMMENT 'produce_batch_id外键',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `cost_of_labour_type_id` bigint(20) NULL DEFAULT NULL COMMENT '工序类别外键',
  `cost_of_labour_type_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_accept` int(11) NULL DEFAULT 1 COMMENT '0：代表接受，1:代表没接受。是否下部门接收',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_pbp_pbid`(`produce_batch_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2235 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for produce_batch_zc_progress
-- ----------------------------
DROP TABLE IF EXISTS `produce_batch_zc_progress`;
CREATE TABLE `produce_batch_zc_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `send_date` datetime NULL DEFAULT NULL,
  `out_date` datetime NULL DEFAULT NULL,
  `produce_batch_id` bigint(20) NULL DEFAULT NULL COMMENT 'produce_batch_id外键',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_accept` int(11) NULL DEFAULT 1 COMMENT '0：代表接受，1:代表没接受。是否下部门接收',
  `zc_group_id` bigint(20) NULL DEFAULT NULL COMMENT '针车组别外键',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pb_fk_groupid`(`zc_group_id`) USING BTREE,
  CONSTRAINT `pb_fk_groupid` FOREIGN KEY (`zc_group_id`) REFERENCES `produce_zc_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for produce_craft
-- ----------------------------
DROP TABLE IF EXISTS `produce_craft`;
CREATE TABLE `produce_craft`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `company_num` int(11) NOT NULL COMMENT '公司货号',
  `customer` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客户公司名称',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `excel_json` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `real_json` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `dev_last_update_date` datetime NULL DEFAULT NULL,
  `dev_last_update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `last_update_date` datetime NULL DEFAULT NULL,
  `last_update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 113 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '生产模块-工艺单模块' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for produce_order_material_progress
-- ----------------------------
DROP TABLE IF EXISTS `produce_order_material_progress`;
CREATE TABLE `produce_order_material_progress`  (
  `order_id` bigint(11) NULL DEFAULT NULL COMMENT '产品订单的ID号，有ID 的是订单报，没ID是补单报',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备料的物料编码',
  `prepared_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '已经报备料的数量',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `in_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '已入库数量',
  `cal_num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应报备用料',
  `progress_percent` int(11) NULL DEFAULT NULL COMMENT '进度，0-100',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `complement_status` int(11) NULL DEFAULT NULL COMMENT '补数备料状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pomp_idx_mi`(`material_id`) USING BTREE,
  INDEX `pomp_idx_orderId`(`order_id`) USING BTREE,
  INDEX `pomp_idx_created`(`created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30245 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for produce_product_constituent
-- ----------------------------
DROP TABLE IF EXISTS `produce_product_constituent`;
CREATE TABLE `produce_product_constituent`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `product_num` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '公司货号',
  `product_brand` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品品牌',
  `product_color` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品颜色',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `pic_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `video_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `designer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_ppc_product_brand`(`product_brand`) USING BTREE,
  CONSTRAINT `fk_ppc_product_brand` FOREIGN KEY (`product_brand`) REFERENCES `base_customer` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1997 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for produce_product_constituent_detail
-- ----------------------------
DROP TABLE IF EXISTS `produce_product_constituent_detail`;
CREATE TABLE `produce_product_constituent_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '组成的物料ID',
  `dosage` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '一双的用量',
  `constituent_id` int(11) NOT NULL COMMENT '外键ID',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `can_show_print` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '0:可显示，1：不显示',
  `content` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `special_content1` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `special_content2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_pcd_cid`(`constituent_id`) USING BTREE COMMENT '外键索引',
  INDEX `index_pcdd_mid`(`material_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37414 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for produce_return_shoes
-- ----------------------------
DROP TABLE IF EXISTS `produce_return_shoes`;
CREATE TABLE `produce_return_shoes`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `package_no` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_art_no` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `num` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_request` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `deal_situation` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `back_package` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `return_date` date NULL DEFAULT NULL,
  `region` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `department_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_prs_user_name`(`user_name`) USING BTREE,
  CONSTRAINT `fk_prs_user_name` FOREIGN KEY (`user_name`) REFERENCES `base_customer` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 713 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for produce_technology_bom
-- ----------------------------
DROP TABLE IF EXISTS `produce_technology_bom`;
CREATE TABLE `produce_technology_bom`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `product_num` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '公司货号',
  `product_brand` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品品牌',
  `product_color` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品颜色',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `shoe_height` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '后帮定位高度',
  `shoe_needle_distance` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '针距',
  `shoe_doudi` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '斗底',
  `shoe_labang` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '拉帮',
  `shoe_suotou` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '锁头',
  `shoe_baobian` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '包边',
  `shoe_chebubian` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '车布边',
  `shoe_tangsongjing` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '烫松紧',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ptb_fk_product_brand`(`product_brand`) USING BTREE,
  CONSTRAINT `produce_technology_bom_ibfk_1` FOREIGN KEY (`product_brand`) REFERENCES `base_customer` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for produce_technology_bom_detail
-- ----------------------------
DROP TABLE IF EXISTS `produce_technology_bom_detail`;
CREATE TABLE `produce_technology_bom_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '组成的物料ID',
  `dosage` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '一双的用量',
  `constituent_id` bigint(11) NOT NULL COMMENT '外键ID',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `can_show_print` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '0:能显示，1：不能显示',
  `content` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注说明',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `special_content1` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `special_content2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_ptbd_cid`(`constituent_id`) USING BTREE COMMENT '外键索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for produce_zc_group
-- ----------------------------
DROP TABLE IF EXISTS `produce_zc_group`;
CREATE TABLE `produce_zc_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_unique_pzcg_gn`(`group_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for repository_buyin_document
-- ----------------------------
DROP TABLE IF EXISTS `repository_buyin_document`;
CREATE TABLE `repository_buyin_document`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核\n1: 待审核',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID外键',
  `supplier_document_num` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商单据编号',
  `buy_in_date` date NULL DEFAULT NULL COMMENT '入库日期',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `source_type` int(11) NULL DEFAULT NULL COMMENT '入库来源',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_rbd_sid`(`supplier_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2305130001 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-采购入库单据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_buyin_document_detail
-- ----------------------------
DROP TABLE IF EXISTS `repository_buyin_document_detail`;
CREATE TABLE `repository_buyin_document_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '物料id',
  `document_id` bigint(20) NOT NULL COMMENT '采购入库单的外键',
  `num` double NULL DEFAULT NULL COMMENT '采购入库数量',
  `comment` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '冗余一个供应商ID',
  `order_seq` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单下推才有-订单号',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '订单下推才有-采购订单的ID',
  `price_date` date NULL DEFAULT NULL COMMENT '价格对应的日期。\n采购入库的，price_date=buy_in_date\n采购订单入库的，price_date=order_date',
  `order_detail_id` bigint(20) NULL DEFAULT NULL COMMENT '订单详情ID',
  `radio_num` double NULL DEFAULT NULL COMMENT '假如有换算系数的话，存储换算后的数量',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `rbdd_docId_index`(`document_id`) USING BTREE,
  INDEX `rbdd_materialid_index`(`material_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43507 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-采购入库单-详情内容' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_buyout_document
-- ----------------------------
DROP TABLE IF EXISTS `repository_buyout_document`;
CREATE TABLE `repository_buyout_document`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `status` int(11) NULL DEFAULT NULL COMMENT '0：已审核 1: 待审核',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '供应商ID外键',
  `buy_out_date` date NULL DEFAULT NULL COMMENT '退料日期',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2305130001 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-采购退料单据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_buyout_document_detail
-- ----------------------------
DROP TABLE IF EXISTS `repository_buyout_document_detail`;
CREATE TABLE `repository_buyout_document_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '物料id',
  `document_id` bigint(20) NOT NULL COMMENT '采购退料单的外键',
  `num` double NULL DEFAULT NULL COMMENT '采购退料数量',
  `comment` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `supplier_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '冗余一个供应商ID',
  `price_date` date NULL DEFAULT NULL COMMENT '价目日期',
  `radio_num` double NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 283 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-采购退料单-详情内容' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_check
-- ----------------------------
DROP TABLE IF EXISTS `repository_check`;
CREATE TABLE `repository_check`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_date` date NULL DEFAULT NULL COMMENT '盘点日期',
  `status` int(11) NULL DEFAULT NULL,
  `check_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '盘点人名',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stock_end_date` date NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-盘点模块' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_check_detail
-- ----------------------------
DROP TABLE IF EXISTS `repository_check_detail`;
CREATE TABLE `repository_check_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '详情信息ID主键',
  `document_id` bigint(20) NOT NULL COMMENT '关联的领料单据表外键ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '盘点的物料ID',
  `check_num` double NOT NULL COMMENT '盘点数目',
  `change_num` double NOT NULL COMMENT '纠正数目',
  `stock_num` double NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1593 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-盘点模块-详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_close
-- ----------------------------
DROP TABLE IF EXISTS `repository_close`;
CREATE TABLE `repository_close`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `close_date` date NULL DEFAULT NULL,
  `created` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库关账模块' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_pick_material
-- ----------------------------
DROP TABLE IF EXISTS `repository_pick_material`;
CREATE TABLE `repository_pick_material`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pick_date` date NULL DEFAULT NULL COMMENT '领料日期',
  `status` int(11) NULL DEFAULT NULL,
  `department_id` bigint(20) NOT NULL COMMENT '领料部门ID',
  `pick_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '领料人名',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `produce_doc_num` int(11) NULL DEFAULT NULL COMMENT '生产计划单号',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注，目前用与填写批次号',
  `batch_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生产序号（对应produce_batch表得batch_id）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_rpm_batchId_nor`(`batch_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2305130001 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-领料模块' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_pick_material_detail
-- ----------------------------
DROP TABLE IF EXISTS `repository_pick_material_detail`;
CREATE TABLE `repository_pick_material_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '详情信息ID主键',
  `document_id` bigint(20) NOT NULL COMMENT '关联的领料单据表外键ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '领料的物料ID',
  `num` double NOT NULL COMMENT '领料数目',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `rpm_docid_index`(`document_id`) USING BTREE,
  INDEX `rpm_materialid_index`(`material_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42479 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-领料模块-详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_return_material
-- ----------------------------
DROP TABLE IF EXISTS `repository_return_material`;
CREATE TABLE `repository_return_material`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `return_date` date NULL DEFAULT NULL COMMENT '退料日期',
  `status` int(11) NULL DEFAULT NULL,
  `department_id` bigint(20) NOT NULL COMMENT '退料部门ID',
  `return_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '退料人名',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `created_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updated_user` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `batch_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2305130001 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-退料模块' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_return_material_detail
-- ----------------------------
DROP TABLE IF EXISTS `repository_return_material_detail`;
CREATE TABLE `repository_return_material_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '详情信息ID主键',
  `document_id` bigint(20) NOT NULL COMMENT '关联的退料单据表外键ID',
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '退料的物料ID',
  `num` double NOT NULL COMMENT '退料数目',
  `reason` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '退料原因',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `repository_return_material_detail_document_id_index`(`document_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 366 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '仓库模块-退料模块-详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_stock
-- ----------------------------
DROP TABLE IF EXISTS `repository_stock`;
CREATE TABLE `repository_stock`  (
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '物料ID',
  `num` double NULL DEFAULT NULL COMMENT '库存数量',
  `updated` datetime NULL DEFAULT NULL COMMENT '最后修改日期',
  PRIMARY KEY (`material_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_stock_history
-- ----------------------------
DROP TABLE IF EXISTS `repository_stock_history`;
CREATE TABLE `repository_stock_history`  (
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主键',
  `num` double NULL DEFAULT NULL COMMENT '库存数量',
  `date` date NULL DEFAULT NULL COMMENT '日期',
  UNIQUE INDEX `stock_history`(`material_id`, `date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_stock_lost
-- ----------------------------
DROP TABLE IF EXISTS `repository_stock_lost`;
CREATE TABLE `repository_stock_lost`  (
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '物料ID',
  `num` double NULL DEFAULT NULL COMMENT '库存数量',
  `created_date` date NULL DEFAULT NULL COMMENT '生成日期',
  `need_num` decimal(9, 2) NULL DEFAULT NULL,
  `no_pick_num` decimal(9, 2) NULL DEFAULT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lost_num` decimal(9, 2) NULL DEFAULT NULL COMMENT '当前废库存数量',
  `latest_price` decimal(7, 2) NULL DEFAULT NULL COMMENT '最近单价',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 115 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for spread_demo
-- ----------------------------
DROP TABLE IF EXISTS `spread_demo`;
CREATE TABLE `spread_demo`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` int(11) NULL DEFAULT NULL,
  `demo_json` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '模板内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'spread 模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_init_stock
-- ----------------------------
DROP TABLE IF EXISTS `sys_init_stock`;
CREATE TABLE `sys_init_stock`  (
  `material_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `num` double NULL DEFAULT NULL,
  PRIMARY KEY (`material_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '父菜单ID，一级菜单为0',
  `menu_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `URL` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单URL',
  `authority` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '授权(多个用逗号分隔，如：user:list,user:create)',
  `component` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(5) NOT NULL COMMENT '类型     0：目录   1：菜单   2：按钮',
  `icon` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `orderType` int(11) NULL DEFAULT NULL COMMENT '排序',
  `created` datetime NOT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `status` int(5) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`menu_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 257 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '超级管理员ID是6，和前端有地方写死了，不能改',
  `role_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `content` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `status` int(5) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `roleName`(`role_name`) USING BTREE,
  UNIQUE INDEX `code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1653250018673803275 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'admin 用户的ID是1，前端那边写死了，不能改',
  `user_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `email` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `city` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created` datetime NULL DEFAULT NULL,
  `updated` datetime NULL DEFAULT NULL,
  `last_login` datetime NULL DEFAULT NULL,
  `status` int(5) NOT NULL,
  `mobile` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_USERNAME`(`user_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1632596524019343363 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `tag_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL COMMENT '1:采购入库，2：采购退料，3：生产领料，4：生产退料',
  `created` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `created_time` datetime NULL DEFAULT NULL,
  `search_field` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `search_str` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `search_start_date` date NULL DEFAULT NULL,
  `search_end_date` date NULL DEFAULT NULL,
  `search_status` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `search_other` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  UNIQUE INDEX `tag_index_unique`(`tag_name`, `created`, `type`) USING BTREE,
  INDEX `tag_index_type`(`type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
