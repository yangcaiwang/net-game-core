
package com.ycw.core.internal.db.sql;

import com.ycw.core.internal.db.entity.DBEntity;
import com.ycw.core.internal.db.entity.DBEntityUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

public final class INSERT {

	public static <T extends DBEntity> String BUILDSQL(Class<T> entityType, String table) {

		return BUILDVALUESSQL(entityType, table);
	}

	/**
	 * 去掉字段,直接传值,提高效率,如果合服导数据结构不一致就蛋疼了
	 */
	private static <T extends DBEntity> String BUILDVALUESSQL(Class<T> entityType, String table) {

		StringBuilder builder = new StringBuilder();
		builder.append("insert into `").append(table).append("` (");
		LinkedHashSet<Field> fields = DBEntityUtils.fieldSet(entityType);
		int i = 0;
		for (Field field : fields) {
			String name = DBEntityUtils.columnName(field);
			if (i < fields.size() - 1) {
				builder.append("`").append(name).append("`,");
			} else {
				builder.append("`").append(name).append("`)");
			}
			i++;
		}
		builder.append(" values(");
		for (int j = 0; j < fields.size(); j++) {
			if (j < fields.size() - 1) {
				builder.append("?,");
			} else {
				builder.append("?)");
			}
		}
		return builder.toString();
	}
}
