-- 清理商品标题中的时间戳后缀
-- 这个SQL脚本会移除商品标题中以"-"后跟13位数字结尾的时间戳

-- 先查看需要清理的记录
SELECT id, title FROM product
WHERE title REGEXP '-[0-9]{13}$';

-- 更新记录，移除时间戳后缀
UPDATE product
SET title = REGEXP_REPLACE(title, '-[0-9]{13}$', '')
WHERE title REGEXP '-[0-9]{13}$';

-- 验证更新结果
SELECT id, title FROM product
WHERE title REGEXP '-[0-9]{13}$'; -- 应该返回0行