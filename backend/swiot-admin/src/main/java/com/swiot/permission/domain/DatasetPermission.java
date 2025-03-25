package com.swiot.permission.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ds_dataset")
public class DatasetPermission implements  Serializable {
    private Long id;
    private String name;
    private String code;
    private String typeId;
    private String remark;
    private String datasetType;
    private String moduleCode;
    private Integer editable;
    private Long sourceId;
    private Integer cache;
    private String config;
    private LocalDateTime updateDate;
    private LocalDateTime createDate;
    private Integer delFlag;
    private String updateBy;
    private String createBy;
}