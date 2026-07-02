package com.swiot.permission;

import com.gccloud.dataset.permission.IDatasetPermissionService;
import com.google.common.collect.Lists;
import com.swiot.common.core.domain.model.LoginUser;
import com.swiot.common.utils.SecurityUtils;
import com.swiot.permission.service.CodeBigScreenPageService;
import com.swiot.permission.service.DatasetPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.swiot.common.utils.SecurityUtils.getUsername;

@Service
public class DatasetPermissionServiceImpl implements IDatasetPermissionService {
    @Autowired
    private DatasetPermissionService datasetPermissionService;

    @Override
    public List<String> filterByPermission(List<String> allId, List<String> datasetTypeList) {
        // 获取当前用户的权限
        LoginUser loginUser = null;
        try {
            loginUser = SecurityUtils.getLoginUser();
        } catch (Exception e) {
            // 未登录（分享预览等匿名访问场景），返回空列表
            return new ArrayList<>();
        }
        Set<String> permissions = loginUser.getPermissions();
        if (permissions.contains("*:*:*")) {
            // 若依的全部权限标识
            allId.retainAll(datasetPermissionService.getAllDatasetIdListByCreateBy(getUsername()));
            return allId;
        }else{
            // 入参allCode是原先符合大屏查询条件的大屏code列表
            return datasetPermissionService.getUserDatasetIdListByCreateBy(getUsername());
        }
    }

    private List<String> getUserDatasetIdList() {
        // 模拟获取当前用户有权限的数据集id列表，需要根据实际情况自行实现
        return Lists.newArrayList("1", "2", "3");
    }

    @Override
    public void addPermission(String id) {
        // 新增数据集后的权限处理，需要根据实际情况自行实现
    }

    @Override
    public void deletePermission(String id) {
        // 删除数据集后的权限处理，需要根据实际情况自行实现
    }
}