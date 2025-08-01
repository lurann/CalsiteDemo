package com.example.demo.selfSchemaFactory.selfSchema;

import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Map;

public class MemorySchema extends AbstractSchema implements Schema {
    @Override
    protected Map<String, Table> getTableMap() {
        return super.getTableMap();
    }


}
