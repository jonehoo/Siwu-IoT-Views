-- 升级 big_screen_share 表，支持公开与密码保护安全分享
ALTER TABLE `big_screen_share`
ADD COLUMN `share_token` VARCHAR(64) NULL DEFAULT NULL COMMENT '分享令牌',
ADD COLUMN `share_type` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '分享类型：0=指定用户 1=公开 2=密码保护',
ADD COLUMN `access_code` VARCHAR(64) NULL DEFAULT NULL COMMENT '访问密码',
ADD COLUMN `expire_time` DATETIME NULL DEFAULT NULL COMMENT '过期时间',
ADD COLUMN `max_views` INT NULL DEFAULT NULL COMMENT '最大查看次数',
ADD COLUMN `view_count` INT NOT NULL DEFAULT 0 COMMENT '已查看次数',
ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
ADD UNIQUE KEY `uk_share_token` (`share_token`);
