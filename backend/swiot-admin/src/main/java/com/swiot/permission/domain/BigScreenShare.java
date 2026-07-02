package com.swiot.permission.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("big_screen_share")
public class BigScreenShare implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;  // 自增主键
    private String code;  // 页面编码 / 允许访问列表 (原有设计中code映射大屏编码)
    private String user;  // 允许访问用户 (原有设计中user映射用户名)
    private String createBy;  // 创建人
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;  // 创建时间
    
    private String updateBy;  // 更新人
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;  // 更新时间
    
    private String remark;  // 备注
    private Integer delFlag;  // 删除标识符

    // 新增安全分享字段
    private String shareToken;  // 分享令牌（UUID）
    private Integer shareType;  // 分享类型：0=指定用户 1=公开 2=密码保护
    private String accessCode;  // 访问密码（BCrypt加密）
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;  // 过期时间
    
    private Integer maxViews;  // 最大查看次数
    private Integer viewCount;  // 已查看次数
    private Integer status;  // 状态：0=禁用 1=启用
}

