package com.swiot.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.swiot.common.utils.SecurityUtils;
import com.swiot.permission.domain.BigScreenShare;
import com.swiot.permission.mapper.BigScreenShareMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CodeBigScreenShareService {

    @Autowired
    private BigScreenShareMapper bigScreenShareMapper;

    @Value("${token.secret:swiot_secret_default_key}")
    private String secret;

    // 创建分享
    public BigScreenShare createShare(String pageCode, Integer shareType, String accessCode, Date expireTime, Integer maxViews, String remark, String username) {
        BigScreenShare share = new BigScreenShare();
        share.setCode(pageCode);
        share.setShareToken(UUID.randomUUID().toString().replace("-", ""));
        share.setShareType(shareType);
        if (shareType == 2 && accessCode != null && !accessCode.isEmpty()) {
            share.setAccessCode(SecurityUtils.encryptPassword(accessCode));
        }
        share.setExpireTime(expireTime);
        share.setMaxViews(maxViews);
        share.setViewCount(0);
        share.setStatus(1);
        share.setCreateBy(username);
        share.setCreateDate(new Date());
        share.setUpdateBy(username);
        share.setUpdateDate(new Date());
        share.setRemark(remark);
        share.setDelFlag(0);

        bigScreenShareMapper.insert(share);
        return share;
    }

    // 根据 token 获取分享记录
    public BigScreenShare getByShareToken(String shareToken) {
        QueryWrapper<BigScreenShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("share_token", shareToken)
                .eq("del_flag", 0);
        return bigScreenShareMapper.selectOne(queryWrapper);
    }

    // 根据 id 获取分享记录
    public BigScreenShare getById(Long id) {
        return bigScreenShareMapper.selectById(id);
    }

    // 更新分享状态
    public boolean updateStatus(Long id, Integer status, String username) {
        BigScreenShare share = bigScreenShareMapper.selectById(id);
        if (share != null) {
            share.setStatus(status);
            share.setUpdateBy(username);
            share.setUpdateDate(new Date());
            return bigScreenShareMapper.updateById(share) > 0;
        }
        return false;
    }

    // 删除分享（逻辑删除）
    public boolean deleteShare(Long id, String username) {
        BigScreenShare share = bigScreenShareMapper.selectById(id);
        if (share != null) {
            share.setDelFlag(1);
            share.setUpdateBy(username);
            share.setUpdateDate(new Date());
            return bigScreenShareMapper.updateById(share) > 0;
        }
        return false;
    }

    // 自增长查看次数
    public void incrementViewCount(Long id) {
        BigScreenShare share = bigScreenShareMapper.selectById(id);
        if (share != null) {
            share.setViewCount(share.getViewCount() + 1);
            bigScreenShareMapper.updateById(share);
        }
    }

    // 获取我的分享列表
    public List<BigScreenShare> getMyShares(String username) {
        QueryWrapper<BigScreenShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("create_by", username)
                .eq("del_flag", 0)
                .orderByDesc("create_date");
        return bigScreenShareMapper.selectList(queryWrapper);
    }

    // 生成只读临时访问 Token
    public String generateAccessToken(String shareToken, String pageCode) {
        long expireDuration = 4 * 3600 * 1000L; // 4小时有效
        return Jwts.builder()
                .claim("shareToken", shareToken)
                .claim("pageCode", pageCode)
                .setExpiration(new Date(System.currentTimeMillis() + expireDuration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 验证只读临时访问 Token，返回 pageCode
    public String verifyAccessToken(String token, String requiredPageCode) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            String pageCode = claims.get("pageCode", String.class);
            String shareToken = claims.get("shareToken", String.class);
            
            // 校验与请求的大屏编码是否一致
            if (requiredPageCode != null && !requiredPageCode.equals(pageCode)) {
                return null;
            }
            
            // 双重校验分享记录状态
            BigScreenShare share = getByShareToken(shareToken);
            if (share == null || share.getStatus() == 0 || share.getDelFlag() == 1) {
                return null;
            }
            if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
                return null;
            }
            
            return pageCode;
        } catch (Exception e) {
            return null;
        }
    }
}
