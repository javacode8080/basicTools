package com.example.exceltosql.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author :sunjian23
 * @date : 2022/10/19 11:20
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SqlType {
    //Sql常用类型
    VARCHAR_255(" varchar(255)"),
    VARCHAR_1000(" varchar(1000)"),
    INTEGER_4(" int4"),
    INTEGER_8(" int8"),
    UUID(" uuid"),
    //自增主键
    SERIAL(" serial"),
    //UUID自增
    UUID_GENERATE(" uuid_generate_v4()"),
    //非空
    NOTNULL(" NOT NULL"),
    //主键id
    PRIMARYKEYBYID(" PRIMARY KEY (id)"),
    //增删改查等
    INSERT("INSERT INTO "),
    SELECT("SELECT "),
    ALTER("ALTER TABLE "),
    DELETE("DELETE "),
    CREATE("CREATE TABLE IF NOT EXISTS public."),
    DEFAULT(" DEFAULT"),
    CONSTRAINT("CONSTRAINT ");

    private String sql;

}
