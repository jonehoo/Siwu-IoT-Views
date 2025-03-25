package com.swiot.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiot.permission.domain.DatasetPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DatasetPermissionMapper  extends BaseMapper<DatasetPermission> {
    // 根据 create_by 查询大屏 code 列表
    List<String> selectCodeListByCreateBy(String createBy);
}
