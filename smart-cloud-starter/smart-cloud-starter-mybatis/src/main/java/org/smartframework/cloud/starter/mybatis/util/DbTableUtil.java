package org.smartframework.cloud.starter.mybatis.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.smartframework.cloud.utility.JacksonUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库表工具类
 * 
 * @author liyulin
 * @date 2019-06-04
 */
@UtilityClass
@Slf4j
public class DbTableUtil {

	/**
	 * （在当前库）复制表结构
	 * 
	 * @param sourceTableName   被复制的表名
	 * @param targetTableName   复制后的表名
	 * @param dataSource
	 */
	public static void copyTableSchema(String sourceTableName, String targetTableName, DataSource dataSource) {
		String copyTableSql = "CREATE TABLE " + targetTableName + " LIKE " + sourceTableName;
		try (Connection connection = dataSource.getConnection();
				PreparedStatement pstat = connection.prepareStatement(copyTableSql);) {
			pstat.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info("execute sql==>{}", copyTableSql);
		}
	}

	/**
	 * （在当前库）创建表（如果不存在）
	 * 
	 * @param sourceTableName   源表
	 * @param targetTableName   待创建的表
	 * @param dataSource
	 */
	public static void createTableIfAbsent(String sourceTableName, String targetTableName,
			DataSource dataSource) {
		try {
			boolean exist = existTable(targetTableName, dataSource);
			if (!exist) {
				copyTableSchema(sourceTableName, targetTableName, dataSource);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * （在当前库）判断表是否已存在
	 * 
	 * @param tableName         表名
	 * @param dataSource
	 * @return
	 * @throws Exception
	 */
	public static boolean existTable(String tableName, DataSource dataSource) {
		List<String> tables = queryTables(tableName, false, dataSource);
		return tables.contains(tableName);
	}

	/**
	 * 通过表名前缀查询所有的表名
	 * 
	 * @param tableNamePrefix   表名前缀
	 * @param dataSource
	 * @return
	 */
	public static List<String> queryTablesByPrefix(String tableNamePrefix, DataSource dataSource) {
		return queryTables(tableNamePrefix, true, dataSource);
	}

	/**
	 * 根据表名查询满足条件的表
	 * 
	 * @param tableName         表名
	 * @param prefix            是否表名前缀匹配
	 * @param dataSource
	 * @return
	 */
	private static List<String> queryTables(String tableName, boolean prefix, DataSource dataSource) {
		if (prefix) {
			tableName += "%";
		}
		List<String> tablesWithPrefix = new ArrayList<>();
		try (Connection connection = dataSource.getConnection()){
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet resultSet = databaseMetaData.getTables(null, null, tableName, null);
			while (resultSet.next()) {
				tablesWithPrefix.add(resultSet.getString(3));
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info("查询相关表tableName={}; result={}", tableName, JacksonUtil.toJson(tablesWithPrefix));
		}
		return tablesWithPrefix;
	}

}