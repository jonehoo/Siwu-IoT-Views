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
@Slf4j
@Service("sqllite")

public class SqlLiteDatasourceServiceImpl extends ServiceImpl<DatasourceDao, DatasourceEntity> implements IBaseDatasourceService {

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
        // 组装计算总条数的sql（SQLite兼容写法）
        String countSql = "SELECT COUNT(1) AS COUNT FROM (" + sql + ") AS t";
        log.info("数据集数据详情计算总条数 sql语句：{}", countSql);
        DbDataVO countData = DBUtils.getSqlValue(countSql, datasource);
        Object count = countData.getData().get(0).get("COUNT");
        int total = Integer.parseInt(count.toString());

        // SQLite分页语法（使用LIMIT和OFFSET）
        int offset = (current - 1) * size;
        List<String> columns = DBUtils.getColumns(sql, datasource.getSourceType());
        String columnStr = (columns == null || columns.size() == 0 || columns.contains("*"))
                ? "*"
                : String.join(",", columns);

        String pageSql = "SELECT " + columnStr + " FROM (" + sql + ") AS t LIMIT " + size + " OFFSET " + offset;
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
        // SQLite不支持存储过程，改为直接执行SQL（保持与MySQL相同的入参处理）
        procedure = procedure.replaceAll(",\\s*\\?\\s*\\)", ")");
        if (!procedure.startsWith("{") && !procedure.endsWith("}")) {
            procedure = "{" + procedure + "}";
        }
        // 调用普通SQL执行（使用DBUtils中的限制逻辑）
        DbDataVO dbDataVO = DBUtils.call(procedure, datasource, current, size);
        boolean pageFlag = current != null && size != null;
        if (pageFlag) {
            return new DataVO(dbDataVO.getPageData(), dbDataVO.getStructure());
        }
        return new DataVO(dbDataVO.getData(), dbDataVO.getStructure());
    }

    @Override
    public List<TableInfoVO> getTableList(DatasourceEntity datasource) {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'";
        DbDataVO dataVO = DBUtils.getSqlValueWithoutCheck(sql, datasource);
        List<Map<String, Object>> data = dataVO.getData();
        List<TableInfoVO> tableList = Lists.newArrayList();
        if (data == null || data.size() == 0) {
            return tableList;
        }
        for (Map<String, Object> map : data) {
            TableInfoVO tableInfoVO = new TableInfoVO();
            tableInfoVO.setName(map.get("name").toString());
            tableInfoVO.setStatus(0);
            tableList.add(tableInfoVO);
        }
        return tableList;
    }

    @Override
    public List<FieldInfoVO> getTableColumnList(DatasourceEntity datasource, String tableName) {
        String sql = "PRAGMA table_info(" + tableName + ")";
        DbDataVO dataVO = DBUtils.getSqlValueWithoutCheck(sql, datasource);
        List<Map<String, Object>> data = dataVO.getData();
        List<FieldInfoVO> fieldList = Lists.newArrayList();
        if (data == null || data.size() == 0) {
            return fieldList;
        }
        for (Map<String, Object> map : data) {
            FieldInfoVO fieldInfoVO = new FieldInfoVO();
            fieldInfoVO.setColumnName(String.valueOf(map.get("name")));
            fieldInfoVO.setColumnType(String.valueOf(map.get("type")));
            fieldInfoVO.setColumnComment(""); // SQLite不存储列注释
            fieldList.add(fieldInfoVO);
        }
        return fieldList;
    }

    @Override
    public List<TableInfoVO> getViewList(DatasourceEntity datasource) {
        String sql = "SELECT name FROM sqlite_master WHERE type='view'";
        DbDataVO dataVO = DBUtils.getSqlValueWithoutCheck(sql, datasource);
        List<Map<String, Object>> data = dataVO.getData();
        List<TableInfoVO> tableList = Lists.newArrayList();
        if (data == null || data.size() == 0) {
            return tableList;
        }
        for (Map<String, Object> map : data) {
            TableInfoVO tableInfoVO = new TableInfoVO();
            tableInfoVO.setName(String.valueOf(map.get("name")));
            tableInfoVO.setStatus(0);
            tableList.add(tableInfoVO);
        }
        return tableList;
    }

    @Override
    public List<FieldInfoVO> getViewColumnList(DatasourceEntity datasource, String viewName) {
        // SQLite中视图的列信息获取方式与表相同
        return getTableColumnList(datasource, viewName);
    }
}
