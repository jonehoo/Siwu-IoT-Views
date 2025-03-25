package com.swiot.permission;

import com.gccloud.dataroom.core.permission.IDataRoomPermissionService;
import com.swiot.common.core.domain.model.LoginUser;
import com.swiot.common.utils.SecurityUtils;
import com.swiot.permission.service.CodeBigScreenPageService;
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

    /**
     * 在大屏分页查询时会调用，可在该方法中根据用户标识到关联表中查询出用户有权限的大屏编码列表，
     * 然后根据该列表将入参的大屏编码列表过滤，只保留有权限的部分
     */
    @Override
    public List<String> filterByPermission(List<String> allCode) {
        // 获取当前用户的权限
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Set<String> permissions = loginUser.getPermissions();
        if (permissions.contains("*:*:*")) {
            // 若依的全部权限标识
            return allCode;
        }else{
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
    }



    // 参数pageCode即为大屏页面的编码，在设计大屏时可在路由code参数中看到
    @Override
    public boolean verifyDataPermission(HttpServletRequest request, String pageCode) {
        // 获取当前登录用户可访问的大屏页面编码列表
        //目前有个设想，这里不拦截用访问受限
        List<String> res = bigScreenPageService.getAllDataRoomCodeListByCreateBy(getUsername());
        Set<String> pageCodes = new HashSet<>(res);
        return pageCodes.contains(pageCode);
    }

}
