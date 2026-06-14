/*
 Navicat MySQL Dump SQL

 Source Server         : soulcapsule
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : soulcapsule_db

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 15/06/2026 02:40:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色: user(用户) 或 assistant(AI)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI聊天记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (1, 1, 'user', 'Hi, I am jack I had a great day  today answer me in chinese', '2026-06-15 01:15:46');
INSERT INTO `chat_message` VALUES (2, 1, 'assistant', '嗨，Jack，今天过得真不错啊！能和我分享一下具体有哪些开心的事吗？', '2026-06-15 01:15:46');
INSERT INTO `chat_message` VALUES (3, 1, 'user', 'I finish my report and got a good grade.', '2026-06-15 01:16:21');
INSERT INTO `chat_message` VALUES (4, 1, 'assistant', '太棒了，Jack！你的努力得到了回报。还遇到了什么特别的事情吗？', '2026-06-15 01:16:21');
INSERT INTO `chat_message` VALUES (5, 1, 'user', 'I will have exam tomorrow', '2026-06-15 01:16:53');
INSERT INTO `chat_message` VALUES (6, 1, 'assistant', '考试加油哦，Jack！你准备得怎么样了？', '2026-06-15 01:16:53');
INSERT INTO `chat_message` VALUES (7, 1, 'user', 'What do you think how I should prepare that ?', '2026-06-15 01:17:36');
INSERT INTO `chat_message` VALUES (8, 1, 'assistant', '复习重点，保持放松，相信自己。你有什么特别想复习的内容吗？', '2026-06-15 01:17:36');

-- ----------------------------
-- Table structure for mood_record
-- ----------------------------
DROP TABLE IF EXISTS `mood_record`;
CREATE TABLE `mood_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `score` int NULL DEFAULT NULL COMMENT '心情分数1-5',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因标签',
  `emotions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '具体感受',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细内容',
  `ai_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI给出的回复',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '心情附图URL',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mood_record
-- ----------------------------
INSERT INTO `mood_record` VALUES (4, 1, 5, NULL, '很好', 'I had a good day!', NULL, '2026-06-15 01:15:07', NULL);
INSERT INTO `mood_record` VALUES (5, 2, 4, NULL, '好', 'Woo55', NULL, '2026-06-15 01:49:53', NULL);
INSERT INTO `mood_record` VALUES (6, 2, 2, NULL, '不好', '666', NULL, '2026-06-15 01:51:02', NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'jack', '$2a$10$w4Yj3Sf.c1sm2KlKZ/8glel3s5w5DFt6A.hxePKzVg3u0OvIsFRSq', '2026-06-15 01:14:29');
INSERT INTO `user` VALUES (2, 'Amy', '$2a$10$59D14y9UuHjelxZukouhsObyR5yvkIDZeCwEWEZAHeT.Ta.qtlZ4G', '2026-06-15 01:21:36');

SET FOREIGN_KEY_CHECKS = 1;
