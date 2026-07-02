package com.swiot.permission;


import com.gccloud.common.permission.IApiPermissionService;
import com.swiot.common.core.domain.model.LoginUser;
import com.swiot.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author dataroom
 * @version 1.0
 * @date 2023/10/20 10:26
 */
@Service
public class PermissionServiceImpl implements IApiPermissionService {

    @Override
    public boolean verifyApiPermission(HttpServletRequest request, String... permission) {
        // 1. 尝试获取登录用户
        LoginUser loginUser = null;
        try {
            loginUser = SecurityUtils.getLoginUser();
        } catch (Exception e) {
            // 未登录，继续检查是否为分享访问
        }

        // 2. 如果未登录，检查是否携带了分享临时令牌
        if (loginUser == null) {
            String shareAccessToken = request.getHeader("X-Share-Access-Token");
            if (shareAccessToken == null || shareAccessToken.isEmpty()) {
                shareAccessToken = request.getParameter("shareAccessToken");
            }
            // 携带了分享令牌，放行只读数据接口（具体校验由 CodePermissionServiceImpl.verifyDataPermission 负责）
            if (shareAccessToken != null && !shareAccessToken.isEmpty()) {
                return true;
            }
            return false;
        }

        // 3. 已登录用户，走正常权限校验
        Set<String> permissions = loginUser.getPermissions();
        if (permissions.contains("*:*:*")) {
            // 若依的全部权限标识
            return true;
        }
        // 判断当前用户是否拥有对应的权限
        for (String s : permission) {
            if (!permissions.contains(s)) {
                return false;
            }
        }
        // 返回值true标识校验通过
        return true;
    }
}
