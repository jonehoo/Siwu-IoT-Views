package com.swiot.datasource.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gccloud.common.vo.PageVO;
import com.gccloud.dataset.dao.DatasourceDao;
import com.gccloud.dataset.entity.DatasourceEntity;
import com.gccloud.dataset.service.IBaseDatasourceService;
import com.gccloud.dataset.utils.DBUtils;
import com.gccloud.dataset.vo.DataVO;
import com.gccloud.dataset.vo.DbDataVO;
import com.gccloud.dataset.vo.FieldInfoVO;
import com.gccloud.dataset.vo.TableInfoVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
/**
 * TDEngine数据源服务实现类
 * @author Su.Guichuan
 * @version 1.0
 * @date 2025/5/14 11:06
 */
@Slf4j
@Service("tdengine")

public class TDengineDatasourceServiceImpl extends ServiceImpl<DatasourceDao, DatasourceEntity> implements IBaseDatasourceService {

    @Override
    public DataVO executeSql(DatasourceEntity datasource, String sql) {
        DbDataVO dbDataVO = DBUtils.getSqlValue(sql, datasource);
        return new DataVO(dbDataVO.getData(), dbDataVO.getStructure());
    }

    @Override
    public DataVO executeSqlPage(DatasourceEntity datasource, String sql, Integer current, Integer size) {
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        // TDengine 兼容的 COUNT 查询写法
        String countSql = "SELECT COUNT(*) FROM (" + sql + ")";
        log.info("数据集数据详情计算总条数 sql语句：{}", countSql);
        DbDataVO countData = DBUtils.getSqlValue(countSql, datasource);
        Object count = countData.getData().get(0).get("count(*)");
        int total = Integer.parseInt(count.toString());

        // TDengine 分页SQL (使用LIMIT和OFFSET)
        int offset = (current - 1) * size;
        List<String> columns = DBUtils.getColumns(sql, datasource.getSourceType());
        String columnStr;
        if (columns == null || columns.size() == 0 || columns.contains("*")) {
            columnStr = "*";
        } else {
            columnStr = String.join(",", columns);
        }
        String pageSql = "SELECT " + columnStr + " FROM (" + sql + ") LIMIT " + size + " OFFSET " + offset;
        log.info("数据集数据详情分页 sql语句：{}", pageSql);
        DbDataVO pageData = DBUtils.getSqlValue(pageSql, datasource);

        PageVO<Map<String, Object>> page = new PageVO<>();
        page.setCurrent(current);
        page.setSize(size);
        page.setTotalCount(total);
        page.setTotalPage((total + size - 1) / size);
        page.setList(pageData.getData());
        return new DataVO(page, pageData.getStructure());
    }

    @Override
    public DataVO executeProcedure(DatasourceEntity datasource, String procedure, Integer current, Integer size) {
        // TDengine 不支持存储过程，直接抛出异常或返回空
        throw new UnsupportedOperationException("TDengine does not support stored procedures");
    }

    @Override
    public List<TableInfoVO> getTableList(DatasourceEntity datasource) {
        // TDengine 获取所有表信息
        String sql = "SHOW TABLES";
        DbDataVO dataVO = DBUtils.getSqlValueWithoutCheck(sql, datasource);
        List<Map<String, Object>> data = dataVO.getData();
        List<TableInfoVO> tableList = Lists.newArrayList();
        if (data == null || data.size() == 0) {
            return tableList;
        }

        // TDengine SHOW TABLES 结果集的列名是"table_name"
        for (Map<String, Object> map : data) {
            TableInfoVO tableInfoVO = new TableInfoVO();
            tableInfoVO.setName(map.get("table_name").toString());
            tableInfoVO.setStatus(0);
            tableList.add(tableInfoVO);
        }
        return tableList;
    }

    @Override
    public List<FieldInfoVO> getTableColumnList(DatasourceEntity datasource, String tableName) {
        // TDengine 获取表字段信息
        String sql = "DESCRIBE " + tableName;
        DbDataVO dataVO = DBUtils.getSqlValueWithoutCheck(sql, datasource);
        List<Map<String, Object>> data = dataVO.getData();
        List<FieldInfoVO> fieldList = Lists.newArrayList();
        if (data == null || data.size() == 0) {
            return fieldList;
        }

        // TDengine DESCRIBE 结果集的列名是"Field", "Type", "Length", "Note"
        for (Map<String, Object> map : data) {
            FieldInfoVO fieldInfoVO = new FieldInfoVO();
            fieldInfoVO.setColumnName(String.valueOf(map.get("Field")));
            fieldInfoVO.setColumnType(String.valueOf(map.get("Type")));
            fieldInfoVO.setColumnComment(String.valueOf(map.get("Note"))); // Note字段可能为空
            fieldList.add(fieldInfoVO);
        }
        return fieldList;
    }

    @Override
    public List<TableInfoVO> getViewList(DatasourceEntity datasource) {
        // TDengine 不支持视图，返回空列表
        return Lists.newArrayList();
    }

    @Override
    public List<FieldInfoVO> getViewColumnList(DatasourceEntity datasource, String viewName) {
        // TDengine 不支持视图，返回空列表
        return Lists.newArrayList();
    }
}