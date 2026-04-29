-- ========================================
-- 智能旅游推荐系统数据库初始化脚本
-- 数据库名称：travel_recommendation
-- 版本：1.0
-- 创建时间：2026-03-26
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS travel_recommendation 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE travel_recommendation;

-- ========================================
-- 1. 用户表（users）
-- ========================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像图片路径',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 2. 管理员表（admins）
-- ========================================
DROP TABLE IF EXISTS admins;
CREATE TABLE admins (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员主键ID',
    username VARCHAR(50) NOT NULL COMMENT '管理员用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色：admin, super_admin',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ========================================
-- 3. 标签表（tags）
-- ========================================
DROP TABLE IF EXISTS tags;
CREATE TABLE tags (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签主键ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    type VARCHAR(20) NOT NULL COMMENT '标签类型：preference（偏好标签）、category（景点分类）',
    description VARCHAR(200) DEFAULT NULL COMMENT '标签描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name_type (name, type),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ========================================
-- 4. 景点表（destinations）
-- ========================================
DROP TABLE IF EXISTS destinations;
CREATE TABLE destinations (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '景点主键ID',
    name VARCHAR(100) NOT NULL COMMENT '景点名称',
    description TEXT NOT NULL COMMENT '景点详细描述',
    province VARCHAR(50) NOT NULL COMMENT '所在省份',
    city VARCHAR(50) NOT NULL COMMENT '所在城市',
    address VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
    latitude DECIMAL(10, 8) DEFAULT NULL COMMENT '纬度',
    longitude DECIMAL(11, 8) DEFAULT NULL COMMENT '经度',
    image_url VARCHAR(255) DEFAULT NULL COMMENT '主图片路径',
    image_urls TEXT DEFAULT NULL COMMENT '所有图片路径（JSON数组）',
    ticket_price DECIMAL(10, 2) DEFAULT NULL COMMENT '门票价格（元）',
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0.00 COMMENT '平均评分（0-5）',
    rating_count INT NOT NULL DEFAULT 0 COMMENT '评分人数',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    collect_count INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_province_city (province, city),
    INDEX idx_average_rating (average_rating),
    INDEX idx_view_count (view_count),
    INDEX idx_collect_count (collect_count),
    INDEX idx_status (status),
    INDEX idx_ticket_price (ticket_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点表';

-- ========================================
-- 5. 景点标签关联表（destination_tags）
-- ========================================
DROP TABLE IF EXISTS destination_tags;
CREATE TABLE destination_tags (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    destination_id BIGINT NOT NULL COMMENT '景点ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_destination_tag (destination_id, tag_id),
    INDEX idx_destination_id (destination_id),
    INDEX idx_tag_id (tag_id),
    CONSTRAINT fk_destination_tags_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
    CONSTRAINT fk_destination_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点标签关联表';

-- ========================================
-- 6. 用户偏好表（user_preferences）
-- ========================================
DROP TABLE IF EXISTS user_preferences;
CREATE TABLE user_preferences (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '偏好主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    budget_min DECIMAL(10, 2) DEFAULT NULL COMMENT '最低预算（元）',
    budget_max DECIMAL(10, 2) DEFAULT NULL COMMENT '最高预算（元）',
    weight INT NOT NULL DEFAULT 1 COMMENT '偏好权重（1-10）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_tag (user_id, tag_id),
    INDEX idx_user_id (user_id),
    INDEX idx_tag_id (tag_id),
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_preferences_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好表';

-- ========================================
-- 7. 用户行为表（user_behaviors）
-- ========================================
DROP TABLE IF EXISTS user_behaviors;
CREATE TABLE user_behaviors (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '行为记录主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    destination_id BIGINT NOT NULL COMMENT '景点ID',
    behavior_type VARCHAR(20) NOT NULL COMMENT '行为类型：view（浏览）、collect（收藏）、share（分享）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_destination_id (destination_id),
    INDEX idx_behavior_type (behavior_type),
    INDEX idx_user_destination (user_id, destination_id, behavior_type),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_user_behaviors_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_behaviors_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为表';

-- ========================================
-- 8. 评分表（ratings）
-- ========================================
DROP TABLE IF EXISTS ratings;
CREATE TABLE ratings (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    destination_id BIGINT NOT NULL COMMENT '景点ID',
    score TINYINT NOT NULL COMMENT '评分（1-5分）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_destination (user_id, destination_id),
    INDEX idx_destination_id (destination_id),
    INDEX idx_score (score),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_ratings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分表';

-- ========================================
-- 9. 评论表（comments）
-- ========================================
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    destination_id BIGINT NOT NULL COMMENT '景点ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID（用于回复）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_destination_id (destination_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ========================================
-- 插入初始数据
-- ========================================

-- 插入默认管理员账户
-- 密码：admin123（BCrypt加密后的值）
INSERT INTO admins (username, password, real_name, role, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'super_admin', 1);

-- 插入偏好标签
INSERT INTO tags (name, type, description, sort_order, status) VALUES 
('亲子游', 'preference', '适合家庭亲子出游的景点', 1, 1),
('自然景观', 'preference', '自然风光优美的景点', 2, 1),
('历史文化', 'preference', '具有历史和文化价值的景点', 3, 1),
('美食之旅', 'preference', '以美食体验为主的景点', 4, 1),
('休闲度假', 'preference', '适合休闲放松的度假场所', 5, 1),
('冒险探险', 'preference', '具有挑战性和探险性质的景点', 6, 1),
('摄影采风', 'preference', '适合摄影创作的景点', 7, 1),
('购物娱乐', 'preference', '购物和娱乐休闲场所', 8, 1),
('康养保健', 'preference', '养生保健类景点', 9, 1),
('研学旅行', 'preference', '适合学习和研究的景点', 10, 1);

-- 插入景点分类标签
INSERT INTO tags (name, type, description, sort_order, status) VALUES 
('5A级景区', 'category', '国家5A级旅游景区', 1, 1),
('4A级景区', 'category', '国家4A级旅游景区', 2, 1),
('3A级景区', 'category', '国家3A级旅游景区', 3, 1),
('自然保护区', 'category', '国家级自然保护区', 4, 1),
('历史古迹', 'category', '历史文化遗产和古迹', 5, 1),
('主题公园', 'category', '各类主题公园', 6, 1),
('博物馆', 'category', '各类博物馆和展览馆', 7, 1),
('公园广场', 'category', '城市公园和广场', 8, 1),
('特色小镇', 'category', '特色旅游小镇', 9, 1),
('乡村旅游', 'category', '乡村田园风光', 10, 1);

-- ========================================
-- 创建视图（便于查询）
-- ========================================

-- 景点详情视图（包含标签）
CREATE OR REPLACE VIEW v_destination_detail AS
SELECT 
    d.*,
    GROUP_CONCAT(t.name SEPARATOR ',') AS tag_names,
    GROUP_CONCAT(t.id SEPARATOR ',') AS tag_ids
FROM destinations d
LEFT JOIN destination_tags dt ON d.id = dt.destination_id
LEFT JOIN tags t ON dt.tag_id = t.id
WHERE d.status = 1
GROUP BY d.id;

-- 用户行为统计视图
CREATE OR REPLACE VIEW v_user_behavior_stats AS
SELECT 
    user_id,
    destination_id,
    COUNT(CASE WHEN behavior_type = 'view' THEN 1 END) AS view_count,
    COUNT(CASE WHEN behavior_type = 'collect' THEN 1 END) AS collect_count,
    COUNT(CASE WHEN behavior_type = 'share' THEN 1 END) AS share_count,
    MAX(create_time) AS last_behavior_time
FROM user_behaviors
GROUP BY user_id, destination_id;

-- ========================================
-- 创建存储过程（更新景点平均评分）
-- ========================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_update_destination_rating$$

CREATE PROCEDURE sp_update_destination_rating(IN p_destination_id BIGINT)
BEGIN
    UPDATE destinations d
    SET d.average_rating = (
        SELECT COALESCE(AVG(r.score), 0)
        FROM ratings r
        WHERE r.destination_id = p_destination_id
    ),
    d.rating_count = (
        SELECT COUNT(*)
        FROM ratings r
        WHERE r.destination_id = p_destination_id
    )
    WHERE d.id = p_destination_id;
END$$

DELIMITER ;

-- ========================================
-- 创建触发器（自动更新景点评分）
-- ========================================
DELIMITER $$

DROP TRIGGER IF EXISTS trg_after_rating_insert$$

CREATE TRIGGER trg_after_rating_insert
AFTER INSERT ON ratings
FOR EACH ROW
BEGIN
    CALL sp_update_destination_rating(NEW.destination_id);
END$$

DROP TRIGGER IF EXISTS trg_after_rating_update$$

CREATE TRIGGER trg_after_rating_update
AFTER UPDATE ON ratings
FOR EACH ROW
BEGIN
    CALL sp_update_destination_rating(NEW.destination_id);
END$$

DROP TRIGGER IF EXISTS trg_after_rating_delete$$

CREATE TRIGGER trg_after_rating_delete
AFTER DELETE ON ratings
FOR EACH ROW
BEGIN
    CALL sp_update_destination_rating(OLD.destination_id);
END$$

DELIMITER ;

-- ========================================
-- 数据库初始化完成
-- ========================================
