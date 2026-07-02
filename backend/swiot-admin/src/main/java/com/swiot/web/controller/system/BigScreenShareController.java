package com.swiot.web.controller.system;

import com.swiot.common.core.controller.BaseController;
import com.swiot.common.core.domain.AjaxResult;
import com.swiot.common.utils.SecurityUtils;
import com.swiot.permission.domain.BigScreenShare;
import com.swiot.permission.service.CodeBigScreenShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BigScreenShareController extends BaseController {

    @Autowired
    private CodeBigScreenShareService shareService;

    // 创建分享链接 (需要登录)
    @PostMapping("/api/share/create")
    public AjaxResult createShare(@RequestBody Map<String, Object> params) {
        String pageCode = (String) params.get("pageCode");
        Integer shareType = (Integer) params.get("shareType"); // 1=公开 2=密码保护
        String accessCode = (String) params.get("accessCode");
        
        Long expireTimeVal = params.get("expireTime") != null ? ((Number) params.get("expireTime")).longValue() : null;
        Date expireTime = expireTimeVal != null ? new Date(expireTimeVal) : null;
        
        Integer maxViews = (Integer) params.get("maxViews");
        String remark = (String) params.get("remark");

        if (pageCode == null || pageCode.isEmpty()) {
            return error("大屏编码不能为空");
        }
        if (shareType == null || (shareType != 1 && shareType != 2)) {
            return error("分享类型无效");
        }

        String username = SecurityUtils.getUsername();
        BigScreenShare share = shareService.createShare(pageCode, shareType, accessCode, expireTime, maxViews, remark, username);
        
        return success(share);
    }

    // 查询当前登录用户的分享列表 (需要登录)
    @GetMapping("/api/share/list")
    public AjaxResult listShares() {
        String username = SecurityUtils.getUsername();
        List<BigScreenShare> list = shareService.getMyShares(username);
        return success(list);
    }

    // 修改分享启用状态 (需要登录)
    @PutMapping("/api/share/{id}/status")
    public AjaxResult updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status"); // 0=禁用 1=启用
        if (status == null || (status != 0 && status != 1)) {
            return error("状态无效");
        }
        String username = SecurityUtils.getUsername();
        boolean ok = shareService.updateStatus(id, status, username);
        return ok ? success() : error("操作失败，分享记录不存在");
    }

    // 删除分享链接 (需要登录)
    @DeleteMapping("/api/share/{id}")
    public AjaxResult deleteShare(@PathVariable Long id) {
        String username = SecurityUtils.getUsername();
        boolean ok = shareService.deleteShare(id, username);
        return ok ? success() : error("操作失败，分享记录不存在");
    }

    // 校验分享链接有效性-公开接口 (无需登录)
    @GetMapping("/api/public/share/verify/{shareToken}")
    public AjaxResult verifyShare(@PathVariable String shareToken) {
        BigScreenShare share = shareService.getByShareToken(shareToken);
        if (share == null || share.getDelFlag() == 1) {
            return error("分享链接不存在或已被删除");
        }
        if (share.getStatus() == 0) {
            return error("该分享链接已被禁用");
        }
        if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
            return error("分享链接已过期");
        }
        if (share.getMaxViews() != null && share.getViewCount() >= share.getMaxViews()) {
            return error("分享链接已达到最大访问次数");
        }

        // 如果是密码保护，则只返回需要密码，不返回真正的 token
        if (share.getShareType() == 2) {
            Map<String, Object> res = new HashMap<>();
            res.put("needPassword", true);
            return success(res);
        }

        // 增加查看次数
        shareService.incrementViewCount(share.getId());

        // 生成临时只读 Token 供前端后续请求数据
        String accessToken = shareService.generateAccessToken(shareToken, share.getCode());
        Map<String, Object> res = new HashMap<>();
        res.put("needPassword", false);
        res.put("shareAccessToken", accessToken);
        res.put("pageCode", share.getCode());
        return success(res);
    }

    // 校验密码并获取只读 Token-公开接口 (无需登录)
    @PostMapping("/api/public/share/verify/{shareToken}")
    public AjaxResult verifyShareWithPassword(@PathVariable String shareToken, @RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.isEmpty()) {
            return error("密码不能为空");
        }

        BigScreenShare share = shareService.getByShareToken(shareToken);
        if (share == null || share.getDelFlag() == 1) {
            return error("分享链接不存在或已被删除");
        }
        if (share.getStatus() == 0) {
            return error("该分享链接已被禁用");
        }
        if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
            return error("分享链接已过期");
        }
        if (share.getMaxViews() != null && share.getViewCount() >= share.getMaxViews()) {
            return error("分享链接已达到最大访问次数");
        }
        if (share.getShareType() != 2) {
            return error("当前分享无需密码校验");
        }

        // 校验密码
        if (!SecurityUtils.matchesPassword(password, share.getAccessCode())) {
            return error("访问密码错误");
        }

        // 增加查看次数
        shareService.incrementViewCount(share.getId());

        // 生成只读 Token
        String accessToken = shareService.generateAccessToken(shareToken, share.getCode());
        Map<String, Object> res = new HashMap<>();
        res.put("shareAccessToken", accessToken);
        res.put("pageCode", share.getCode());
        return success(res);
    }
}
