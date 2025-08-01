package com.example.demo.entity;

import org.apache.calcite.sql.SqlDialect;

public class hangaodbDialect extends SqlDialect {
    public hangaodbDialect(Context context) {
        super(context);
    }

    public static final SqlDialect.Context HANGODB = SqlDialect.EMPTY_CONTEXT
            .withDatabaseProduct(DatabaseProduct.UNKNOWN)
            .withIdentifierQuoteString("\"")
            ;

    public static final SqlDialect HANGODB_DIALECT = new hangaodbDialect(HANGODB);

}
