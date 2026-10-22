CREATE DATABASE IF NOT EXISTS shengda DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shengda;

-- 用户表
CREATE TABLE `user`
(
    `user_name`               VARCHAR(50)  NOT NULL COMMENT '用户名（主键）',
    `user_password`           VARCHAR(255) NOT NULL COMMENT '用户密码（加密存储）',
    `user_register_time`      DATE         DEFAULT NULL COMMENT '注册时间',
    `user_email`              VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `user_gender`             VARCHAR(10)  DEFAULT NULL COMMENT '性别',
    `role`                    VARCHAR(20)  DEFAULT 'user' COMMENT '角色：admin-管理员，user-普通用户',
    `continuous_checkin_days` INT          DEFAULT 0 COMMENT '最大连续签到天数',
    `last_checkin_date`       TIMESTAMP    NULL DEFAULT NULL COMMENT '上一次签到时间',
    `user_prompt_json`        TEXT COMMENT 'LLM用户画像JSON（三层Prompt）',
    `last_prompt_update_time` TIMESTAMP    NULL DEFAULT NULL COMMENT '最后Prompt更新时间',
    PRIMARY KEY (`user_name`),
    UNIQUE KEY `uk_email` (`user_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 帖子表
CREATE TABLE `tie`
(
    `tie_id`          INT          NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `tie_title`       VARCHAR(200) NOT NULL COMMENT '帖子标题',
    `tie_content`     TEXT COMMENT '帖子内容',
    `tie_create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `user_name`       VARCHAR(50)  NOT NULL COMMENT '发帖用户名',
    `tie_image`       VARCHAR(500) DEFAULT NULL COMMENT '帖子图片URL',
    `tie_type`        VARCHAR(20)  DEFAULT 'academic' COMMENT '帖子类型：academic-学术帖，product-商品帖',
    `product_id`      INT          DEFAULT NULL COMMENT '关联商品ID',
    PRIMARY KEY (`tie_id`),
    KEY               `idx_user_name` (`user_name`),
    KEY               `idx_create_time` (`tie_create_time`),
    CONSTRAINT `fk_tie_user` FOREIGN KEY (`user_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 回复表
CREATE TABLE `reply`
(
    `reply_id`          INT         NOT NULL AUTO_INCREMENT COMMENT '回复ID',
    `reply_content`     TEXT        NOT NULL COMMENT '回复内容',
    `reply_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
    `user_name`         VARCHAR(50) NOT NULL COMMENT '回复用户名',
    `tie_id`            INT         NOT NULL COMMENT '所属帖子ID',
    PRIMARY KEY (`reply_id`),
    KEY                 `idx_tie_id` (`tie_id`),
    KEY                 `idx_user_name` (`user_name`),
    CONSTRAINT `fk_reply_tie` FOREIGN KEY (`tie_id`) REFERENCES `tie` (`tie_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reply_user` FOREIGN KEY (`user_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回复表';

-- 个人网盘文件表
CREATE TABLE `user_file`
(
    `file_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `user_name`   VARCHAR(50)  NOT NULL COMMENT '用户名',
    `file_name`   VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path`   VARCHAR(500) NOT NULL COMMENT '文件存储路径（UUID命名）',
    `file_size`   BIGINT       DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type`   VARCHAR(100) DEFAULT NULL COMMENT '文件类型（MIME类型）',
    `upload_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`file_id`),
    KEY           `idx_user_name` (`user_name`),
    KEY           `idx_upload_time` (`upload_time`),
    CONSTRAINT `fk_userfile_user` FOREIGN KEY (`user_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人网盘文件表';

-- 二手商品表
CREATE TABLE `second_hand_product`
(
    `product_id`          INT            NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `user_name`           VARCHAR(50)    NOT NULL COMMENT '卖家用户名',
    `product_name`        VARCHAR(200)   NOT NULL COMMENT '商品名称',
    `product_description` TEXT COMMENT '商品描述',
    `product_condition`   VARCHAR(50)    DEFAULT NULL COMMENT '几成新',
    `price`               DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `original_price`      DECIMAL(10, 2) DEFAULT NULL COMMENT '原价',
    `product_images`      VARCHAR(1000)  DEFAULT NULL COMMENT '商品图片URL（多张逗号分隔）',
    `category`            VARCHAR(50)    DEFAULT NULL COMMENT '商品分类',
    `status`              VARCHAR(20)    DEFAULT 'on_sale' COMMENT '状态：on_sale-在售，sold-已售出，reserved-已预订',
    `view_count`          INT            DEFAULT 0 COMMENT '浏览次数',
    `want_count`          INT            DEFAULT 0 COMMENT '想要人数',
    `create_time`         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`product_id`),
    KEY                   `idx_user_name` (`user_name`),
    KEY                   `idx_status` (`status`),
    KEY                   `idx_category` (`category`),
    KEY                   `idx_create_time` (`create_time`),
    CONSTRAINT `fk_product_user` FOREIGN KEY (`user_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品表';

-- 订单表
CREATE TABLE `product_order`
(
    `order_id`      INT            NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`      VARCHAR(50)    NOT NULL COMMENT '订单编号',
    `product_id`    INT            NOT NULL COMMENT '商品ID',
    `seller_name`   VARCHAR(50)    NOT NULL COMMENT '卖家用户名',
    `buyer_name`    VARCHAR(50)    NOT NULL COMMENT '买家用户名',
    `product_name`  VARCHAR(200)   NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品主图',
    `price`         DECIMAL(10, 2) NOT NULL COMMENT '成交价格',
    `status`        VARCHAR(20)  DEFAULT 'pending_payment' COMMENT '订单状态：pending_payment-待支付，paid-已支付，completed-已完成，cancelled-已取消',
    `payment_time`  TIMESTAMP NULL DEFAULT NULL COMMENT '支付时间',
    `complete_time` TIMESTAMP NULL DEFAULT NULL COMMENT '完成时间',
    `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`order_id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY             `idx_product_id` (`product_id`),
    KEY             `idx_seller_name` (`seller_name`),
    KEY             `idx_buyer_name` (`buyer_name`),
    KEY             `idx_status` (`status`),
    CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `second_hand_product` (`product_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_buyer` FOREIGN KEY (`buyer_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 私信消息表
CREATE TABLE `private_message`
(
    `message_id`      INT         NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `sender_name`     VARCHAR(50) NOT NULL COMMENT '发送者用户名',
    `receiver_name`   VARCHAR(50) NOT NULL COMMENT '接收者用户名',
    `message_content` TEXT        NOT NULL COMMENT '消息内容',
    `message_type`    VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text-文本，image-图片，product-商品链接',
    `product_id`      INT         DEFAULT NULL COMMENT '关联商品ID',
    `is_read`         TINYINT     DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `send_time`       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`message_id`),
    KEY               `idx_sender_name` (`sender_name`),
    KEY               `idx_receiver_name` (`receiver_name`),
    KEY               `idx_send_time` (`send_time`),
    KEY               `idx_is_read` (`is_read`),
    CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE,
    CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- 作业题目表
CREATE TABLE `homework_question`
(
    `question_id`      INT          NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `admin_name`       VARCHAR(50)  NOT NULL COMMENT '发布管理员用户名',
    `course_name`      VARCHAR(100) NOT NULL COMMENT '课程名称',
    `question_title`   VARCHAR(200) NOT NULL COMMENT '题目标题',
    `question_content` TEXT         NOT NULL COMMENT '题目内容',
    `reference_answer` TEXT COMMENT '参考答案',
    `total_score` DOUBLE DEFAULT 100.0 COMMENT '总分',
    `create_time`      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`question_id`),
    KEY                `idx_admin_name` (`admin_name`),
    KEY                `idx_course_name` (`course_name`),
    CONSTRAINT `fk_question_admin` FOREIGN KEY (`admin_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业题目表';

-- 作业提交表
CREATE TABLE `homework_submission`
(
    `submission_id`   INT         NOT NULL AUTO_INCREMENT COMMENT '提交ID',
    `question_id`     INT         NOT NULL COMMENT '题目ID',
    `user_name`       VARCHAR(50) NOT NULL COMMENT '提交用户名',
    `answer_content`  TEXT        NOT NULL COMMENT '答案内容',
    `answer_language` VARCHAR(20) DEFAULT 'txt' COMMENT '答案语言：txt, java, javascript, python, cpp',
    `status`          VARCHAR(20) DEFAULT 'submitted' COMMENT '状态：submitted-已提交，grading-批改中，graded-已批改',
    `submit_time`     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (`submission_id`),
    KEY               `idx_question_id` (`question_id`),
    KEY               `idx_user_name` (`user_name`),
    KEY               `idx_status` (`status`),
    CONSTRAINT `fk_submission_question` FOREIGN KEY (`question_id`) REFERENCES `homework_question` (`question_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_submission_user` FOREIGN KEY (`user_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交表';

-- 作业批改表
CREATE TABLE `homework_grading`
(
    `grading_id`              INT         NOT NULL AUTO_INCREMENT COMMENT '批改ID',
    `submission_id`           INT         NOT NULL COMMENT '提交ID',
    `user_name`               VARCHAR(50) NOT NULL COMMENT '提交用户名',
    `score` DOUBLE DEFAULT NULL COMMENT '得分',
    `ai_feedback`             TEXT COMMENT 'AI反馈',
    `correctness_analysis`    TEXT COMMENT '正确性分析',
    `improvement_suggestions` TEXT COMMENT '改进建议',
    `knowledge_points`        TEXT COMMENT '知识点（JSON数组）',
    `grade_time`              TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '批改时间',
    PRIMARY KEY (`grading_id`),
    UNIQUE KEY `uk_submission_id` (`submission_id`),
    KEY                       `idx_user_name` (`user_name`),
    CONSTRAINT `fk_grading_submission` FOREIGN KEY (`submission_id`) REFERENCES `homework_submission` (`submission_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_grading_user` FOREIGN KEY (`user_name`) REFERENCES `user` (`user_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业批改表';

-- 初始化默认账号：admin/admin123、test/test123（明文存储，登录时直接比对）
INSERT INTO `user` (`user_name`, `user_password`, `user_register_time`, `user_email`, `user_gender`, `role`)
VALUES ('admin', 'admin123', CURDATE(), 'admin@shengda.edu.cn', '男', 'admin') ON DUPLICATE KEY
UPDATE `user_password` = 'admin123';

INSERT INTO `user` (`user_name`, `user_password`, `user_register_time`, `user_email`, `user_gender`, `role`)
VALUES ('test', 'test123', CURDATE(), 'test@shengda.edu.cn', '男', 'user') ON DUPLICATE KEY
UPDATE `user_password` = 'test123';

-- 红包主表
CREATE TABLE IF NOT EXISTS red_packet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '红包ID',
    packet_no VARCHAR(50) NOT NULL UNIQUE COMMENT '红包编号',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    total_count INT NOT NULL COMMENT '总个数',
    remain_count INT NOT NULL DEFAULT 0 COMMENT '剩余个数',
    remain_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '剩余金额',
    publisher VARCHAR(50) NOT NULL COMMENT '发布者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_time DATETIME COMMENT '过期时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-进行中,1-已结束',
    INDEX idx_packet_no (packet_no),
    INDEX idx_publisher (publisher)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包主表';

-- 红包金额明细表
CREATE TABLE IF NOT EXISTS red_packet_amount (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    packet_no VARCHAR(50) NOT NULL COMMENT '红包编号',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    is_grabbed TINYINT NOT NULL DEFAULT 0 COMMENT '是否已被抢:0-未抢,1-已抢',
    grab_order INT COMMENT '抢的顺序',
    INDEX idx_packet_no (packet_no),
    INDEX idx_is_grabbed (is_grabbed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包金额明细表';

-- 红包领取记录表
CREATE TABLE IF NOT EXISTS red_packet_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    packet_no VARCHAR(50) NOT NULL COMMENT '红包编号',
    user_name VARCHAR(50) NOT NULL COMMENT '抢红包用户',
    amount DECIMAL(10,2) NOT NULL COMMENT '抢到金额',
    grab_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抢红包时间',
    INDEX idx_packet_no (packet_no),
    INDEX idx_user_name (user_name),
    UNIQUE KEY uk_packet_user (packet_no, user_name) COMMENT '防止重复抢同一个红包'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包领取记录表';
