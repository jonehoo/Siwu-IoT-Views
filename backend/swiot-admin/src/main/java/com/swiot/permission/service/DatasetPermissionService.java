package com.swiot.permission.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.swiot.permission.domain.BigScreenPage;
import com.swiot.permission.domain.DatasetPermission;
import com.swiot.permission.mapper.DatasetPermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
@Service
public class DatasetPermissionService {
    @Autowired
    DatasetPermissionMapper datasetPermissionMapper;

    // 根据 create_by 查询大屏 code 列表
    public List<String> getUserDatasetIdListByCreateBy(String createBy) {
        // 使用 MyBatis-Plus 的 QueryWrapper 创建查询条件
        QueryWrapper<DatasetPermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("create_by", createBy)  // 根据 create_by 查询
                .eq("del_flag", 0);  // 只查询未删除的记录

        // 查询所有符合条件的记录并提取 code 列
        List<DatasetPermission> resultList = datasetPermissionMapper.selectList(queryWrapper);

        // 将 id 转换为 String 并返回
        return resultList.stream()
                .map(DatasetPermission::getId)  // 获取 id
                .filter(Objects::nonNull)       // 过滤掉 null 值
                .map(String::valueOf)           // 将 Long 转换为 String
                .collect(Collectors.toList());
    }
    public List<String> getAllDatasetIdListByCreateBy(String createBy) {
        // 使用 MyBatis-Plus 的 QueryWrapper 创建查询条件
        QueryWrapper<DatasetPermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);  // 只查询未删除的记录
        // 查询所有符合条件的记录并提取 code 列
        List<DatasetPermission> resultList = datasetPermissionMapper.selectList(queryWrapper);
        // 提取 `code` 列表
        return resultList.stream()
                .map(DatasetPermission::getId)  // 获取 id
                .filter(Objects::nonNull)       // 过滤掉 null 值
                .map(String::valueOf)           // 将 Long 转换为 String
                .collect(Collectors.toList());
    }
}
