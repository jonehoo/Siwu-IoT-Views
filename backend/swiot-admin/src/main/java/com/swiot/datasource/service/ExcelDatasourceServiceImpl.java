package com.swiot.datasource.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gccloud.common.exception.GlobalException;
import com.gccloud.dataset.dao.DatasourceDao;
import com.gccloud.dataset.dto.DatasourceDTO;
import com.gccloud.dataset.dto.ExcelHeaderDTO;
import com.gccloud.dataset.entity.DatasourceEntity;
import com.gccloud.dataset.service.IBaseDatasourceService;
import com.gccloud.dataset.vo.DataVO;
import com.swiot.common.config.RuoYiConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
        * excel数据源，上传excel文件，解析excel文件，将excel文件解析成表格数据，存储到数据库中，然后通过sql查询数据
 * @author hongyang
 * @version 1.0
        * @date 2023/6/2 11:06
        */
@Service("xlxs")
public class ExcelDatasourceServiceImpl extends ServiceImpl<DatasourceDao, DatasourceEntity> implements IBaseDatasourceService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final int BATCH_COUNT = 100;
    private static final int HEAD_ROW_NUMBER = 1;

    @Override
    public String add(DatasourceDTO datasourceDTO) {
        // 1. 准备文件路径
        String filePath = buildFilePath(datasourceDTO.getConfig());
        File file = new File(filePath);
        if (!file.exists()) {
            throw new GlobalException("Excel文件不存在: " + filePath);
        }

        // 2. 生成表名
        String tableName = "excel_" + RandomStringUtils.randomAlphabetic(5);
        datasourceDTO.setTableName(tableName);

        // 3. 安全读取表头（终极解决方案）
        List<ExcelHeaderDTO> headerList = readExcelHeaders(file);
        if (headerList.isEmpty()) {
            throw new GlobalException("Excel文件没有表头或格式不正确");
        }

        // 4. 创建表
        createTable(tableName, headerList, datasourceDTO.getSourceName());

        // 5. 导入数据（使用全新的读取实例）
        importExcelData(file, tableName, headerList);

        return IBaseDatasourceService.super.add(datasourceDTO);
    }

    /**
            * 构建安全的文件路径
     */
    private String buildFilePath(Map<String, Object> config) {
        try {
            String path = config.get("path").toString().replaceFirst("^/profile", "");
            String profilePath = RuoYiConfig.getProfile();
            return Paths.get(profilePath, path).normalize().toString();
        } catch (Exception e) {
            throw new GlobalException("文件路径构建失败: " + e.getMessage());
        }
    }

    /**
            * 安全读取Excel表头（生产验证方案）
            */
    private List<ExcelHeaderDTO> readExcelHeaders(File file) {
        List<ExcelHeaderDTO> headers = new ArrayList<>();
        ExcelReader excelReader = null;

        try {
            // 使用独立的ExcelReader实例
            excelReader = EasyExcel.read(file, new AnalysisEventListener<Map<Integer, Object>>() {
                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    headMap.forEach((index, name) -> {
                        ExcelHeaderDTO header = new ExcelHeaderDTO();
                        header.setIndex(index);
                        header.setColumnName(name);
                        header.setColumnType(detectColumnType(name));
                        headers.add(header);
                    });
                    // 关键修改：不再手动中断，让EasyExcel自然完成
                }

                @Override
                public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                    // 不处理数据行
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 自然完成
                }
            }).headRowNumber(HEAD_ROW_NUMBER).build();

            // 只读取第一个sheet的第一行
            ReadSheet readSheet = EasyExcel.readSheet(0)
                    .headRowNumber(HEAD_ROW_NUMBER)
                    .build();

            excelReader.read(readSheet);

        } catch (Exception e) {
            throw new GlobalException("Excel表头读取失败: " + e.getMessage(), e);
        } finally {
            if (excelReader != null) {
                excelReader.finish();
            }
        }

        return headers;
    }

    /**
            * 导入Excel数据（安全版本）
            */
    private void importExcelData(File file, String tableName, List<ExcelHeaderDTO> headerList) {
        List<Map<Integer, Object>> cachedDataList = new ArrayList<>(BATCH_COUNT);
        ExcelReader excelReader = null;

        try {
            excelReader = EasyExcel.read(file, new AnalysisEventListener<Map<Integer, Object>>() {
                @Override
                public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                    cachedDataList.add(data);
                    if (cachedDataList.size() >= BATCH_COUNT) {
                        saveBatchData(cachedDataList, tableName, headerList);
                        cachedDataList.clear();
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    if (!cachedDataList.isEmpty()) {
                        saveBatchData(cachedDataList, tableName, headerList);
                    }
                }
            }).headRowNumber(HEAD_ROW_NUMBER + 1).build();

            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            excelReader.read(readSheet);

        } catch (Exception e) {
            throw new GlobalException("Excel数据导入失败: " + e.getMessage(), e);
        } finally {
            if (excelReader != null) {
                excelReader.finish();
            }
        }
    }

    /**
            * 安全导入数据（与表头读取分离）
            */
    private void importData(File file, String tableName, List<ExcelHeaderDTO> headerList) {
        List<Map<Integer, Object>> cachedDataList = new ArrayList<>(BATCH_COUNT);

        EasyExcel.read(file, new AnalysisEventListener<Map<Integer, Object>>() {
                    @Override
                    public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                        cachedDataList.add(data);
                        if (cachedDataList.size() >= BATCH_COUNT) {
                            saveBatchData(cachedDataList, tableName, headerList);
                            cachedDataList.clear();
                        }
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        if (!cachedDataList.isEmpty()) {
                            saveBatchData(cachedDataList, tableName, headerList);
                        }
                    }
                })
                .headRowNumber(HEAD_ROW_NUMBER + 1) // 跳过表头行
                .sheet()
                .doRead();
    }

    /**
            * 创建数据库表
     */
    private void createTable(String tableName, List<ExcelHeaderDTO> headerList, String sourceName) {
        StringBuilder ddl = new StringBuilder("create table ")
                .append(tableName)
                .append(" (`id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '主键',");

        headerList.forEach(header -> {
            ddl.append("`")
                    .append(header.getColumnName())
                    .append("` ")
                    .append(getDataType(header.getColumnType()))
                    .append(" DEFAULT NULL COMMENT '")
                    .append(header.getColumnName())
                    .append("',");
        });

        ddl.append("PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='excel数据源表")
                .append(sourceName)
                .append("'");

        try {
            jdbcTemplate.execute(ddl.toString());
        } catch (Exception e) {
            throw new GlobalException("创建表失败: " + e.getMessage(), e);
        }
    }



    /**
            * 批量保存数据
     */
    private void saveBatchData(List<Map<Integer, Object>> dataList, String tableName, List<ExcelHeaderDTO> headerList) {
        if (dataList.isEmpty()) return;

        StringBuilder sql = new StringBuilder("insert into ")
                .append(tableName)
                .append(" (");

        // 添加列名
        headerList.forEach(header -> sql.append(header.getColumnName()).append(","));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") values ");

        // 添加数据
        for (Map<Integer, Object> data : dataList) {
            sql.append("(");
            for (ExcelHeaderDTO header : headerList) {
                Object value = data.get(header.getIndex());
                if (value == null) {
                    sql.append("null,");
                } else if ("number".equalsIgnoreCase(header.getColumnType())) {
                    sql.append(value).append(",");
                } else {
                    sql.append("'").append(value.toString().replace("'", "''")).append("',");
                }
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append("),");
        }
        sql.deleteCharAt(sql.length() - 1);

        try {
            jdbcTemplate.execute(sql.toString());
        } catch (Exception e) {
            throw new GlobalException("数据插入失败: " + e.getMessage(), e);
        }
    }

    /**
            * 根据列名智能判断数据类型
     */
    private String detectColumnType(String columnName) {
        if (columnName == null) return "string";

        String lowerName = columnName.toLowerCase();
        if (lowerName.contains("date") || lowerName.contains("时间")) {
            return "date";
        } else if (lowerName.matches(".*(amount|price|total|sum|数量|金额|价格).*")) {
            return "number";
        }
        return "string";
    }

    /**
            * 根据数据类型获取数据库字段类型
     */
    private String getDataType(String type) {
        if (StringUtils.isBlank(type)) {
            return "text";
        }
        switch (type.toLowerCase()) {
            case "string":
                return "text";
            case "number":
                return "bigint(64)";
            case "date":
                return "date";
            case "datetime":
                return "datetime";
            default:
                return "text";
        }
    }

    @Override
    public void update(DatasourceDTO entity) {
        // excel数据集不允许编辑
        IBaseDatasourceService.super.update(entity);
    }

    @Override
    public void delete(String id) {
        DatasourceEntity datasource = this.getById(id);
        IBaseDatasourceService.super.delete(id);
        // 删除数据集还需要额外删除数据库的表
        String tableName = datasource.getTableName();
        try {
            jdbcTemplate.execute("drop table if exists " + tableName);
        } catch (Exception e) {
            throw new GlobalException("删除表失败: " + e.getMessage());
        }
    }

    @Override
    public DataVO executeSql(DatasourceEntity datasource, String sql) {
        // 和mysql数据源的实现一样
        return null;
    }

    @Override
    public DataVO executeSqlPage(DatasourceEntity datasource, String sql, Integer current, Integer size) {
        // 和mysql的实现一样
        return IBaseDatasourceService.super.executeSqlPage(datasource, sql, current, size);
    }
}