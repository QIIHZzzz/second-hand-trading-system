-- 购物车模块数据库表创建脚本
-- 请使用MySQL客户端连接到数据库后执行此脚本
-- 数据库名: second-hand-trading-system

-- 购物车主表 (与用户一对一)
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 购物车项表
CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL COMMENT '购物车ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_title VARCHAR(255) NOT NULL COMMENT '商品标题快照',
    product_image_url VARCHAR(500) COMMENT '商品图片快照',
    price_at_add DECIMAL(10,2) NOT NULL COMMENT '加入时价格',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES cart(id),
    FOREIGN KEY (product_id) REFERENCES product(id),
    UNIQUE KEY uk_cart_product (cart_id, product_id) -- 防止重复添加
);

-- 表创建完成后，可以验证表结构
DESC cart;
DESC cart_item;