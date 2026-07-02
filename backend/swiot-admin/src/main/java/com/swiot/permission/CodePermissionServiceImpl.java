package com.swiot.permission;

import com.gccloud.dataroom.core.permission.IDataRoomPermissionService;
import com.swiot.common.core.domain.model.LoginUser;
import com.swiot.common.utils.SecurityUtils;
import com.swiot.permission.service.CodeBigScreenPageService;
import com.swiot.permission.service.CodeBigScreenShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.swiot.common.utils.SecurityUtils.getUsername;

/**
 * @author gcpaas
 * @version 1.0
 * @date 2023/9/11 16:21
 */
@Service
public class CodePermissionServiceImpl implements IDataRoomPermissionService {
    @Autowired
    private CodeBigScreenPageService bigScreenPageService;

    @Autowired
    private CodeBigScreenShareService shareService;

    /**
     * 在大屏分页查询时会调用，可在该方法中根据用户标识到关联表中查询出用户有权限的大屏编码列表，
     * 然后根据该列表将入参的大屏编码列表过滤，只保留有权限的部分
     */
    @Override
    public List<String> filterByPermission(List<String> allCode) {
        try {
            // 获取当前用户的权限
            LoginUser loginUser = SecurityUtils.getLoginUser();
            Set<String> permissions = loginUser.getPermissions();
            if (permissions != null && permissions.contains("*:*:*")) {
                // 若依的全部权限标识
                return allCode;
            } else {
                // 入参allCode是原先符合大屏查询条件的大屏code列表
                List<String> res = bigScreenPageService.getUserDataRoomCodeListByCreateBy(getUsername());
                List<String> res_share = bigScreenPageService.getUserDataRoomCodeListFromShareByCreateBy(getUsername());
                //如果 res 或 res_share 为空，则可以直接返回 allCode
                if (res == null || res_share == null || allCode == null) {
                    return allCode;
                }
                //使用 LinkedHashSet 来保持插入顺序
                Set<String> mergedSet = new LinkedHashSet<>();
                mergedSet.addAll(res);
                mergedSet.addAll(res_share);
                // 转换回 List
                List<String> mergedList = new ArrayList<>(mergedSet);
                allCode.retainAll(mergedList);
                return allCode;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // 参数pageCode即为大屏页面的编码，在设计大屏时可在路由code参数中看到
    @Override
    public boolean verifyDataPermission(HttpServletRequest request, String pageCode) {
        // 1. 优先校验分享的只读临时 Token
        String shareAccessToken = request.getHeader("X-Share-Access-Token");
        if (shareAccessToken == null || shareAccessToken.isEmpty()) {
            shareAccessToken = request.getParameter("shareAccessToken");
        }
        if (shareAccessToken != null && !shareAccessToken.isEmpty()) {
            String verifiedPageCode = shareService.verifyAccessToken(shareAccessToken, pageCode);
            if (verifiedPageCode != null) {
                return true;
            }
        }

        // 2. 校验普通登录用户的权限
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null) {
                Set<String> permissions = loginUser.getPermissions();
                if (permissions != null && permissions.contains("*:*:*")) {
                    return true;
                }
                List<String> res = bigScreenPageService.getUserDataRoomCodeListByCreateBy(loginUser.getUsername());
                List<String> resShare = bigScreenPageService.getUserDataRoomCodeListFromShareByCreateBy(loginUser.getUsername());
                
                Set<String> pageCodes = new HashSet<>();
                if (res != null) {
                    pageCodes.addAll(res);
                }
                if (resShare != null) {
                    pageCodes.addAll(resShare);
                }
                return pageCodes.contains(pageCode);
            }
        } catch (Exception e) {
            // 未登录或登录信息过期
        }
        return false;
    }

}

