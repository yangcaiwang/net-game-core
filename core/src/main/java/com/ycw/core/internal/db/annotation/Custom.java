
package com.ycw.core.internal.db.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface Custom {

	/** 无参数,返回类型为String; 相当于saveToDB(),将对象序列化成字符串后保存到数据库 */
	String getter();

	/**
	 * 参数为String,返回类型为void;相当于loadFromDB(String
	 * dbDeSerialStr)，将数据库读出来的字符串数据反序列化成此对象
	 */
	String setter();
}
