/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.cdc.connectors.doris.utils;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.cdc.common.configuration.Configuration;
import org.apache.flink.cdc.common.event.TableId;
import org.apache.flink.cdc.common.schema.Schema;
import org.apache.flink.cdc.common.schema.Selectors;
import org.apache.flink.cdc.common.types.DataType;
import org.apache.flink.cdc.common.types.DateType;
import org.apache.flink.cdc.common.types.LocalZonedTimestampType;
import org.apache.flink.cdc.common.types.TimestampType;
import org.apache.flink.cdc.common.types.ZonedTimestampType;
import org.apache.flink.cdc.common.utils.StringUtils;
import org.apache.flink.cdc.connectors.doris.sink.DorisDataSinkOptions;

import java.util.Map;

import static org.apache.flink.cdc.connectors.doris.sink.DorisDataSinkOptions.TABLE_CREATE_AUTO_PARTITION_PROPERTIES_PREFIX;
import static org.apache.flink.cdc.connectors.doris.sink.DorisDataSinkOptions.TABLE_CREATE_DEFAULT_PARTITION_KEY;
import static org.apache.flink.cdc.connectors.doris.sink.DorisDataSinkOptions.TABLE_CREATE_DEFAULT_PARTITION_UNIT;
import static org.apache.flink.cdc.connectors.doris.sink.DorisDataSinkOptions.TABLE_CREATE_PARTITION_EXCLUDE;
import static org.apache.flink.cdc.connectors.doris.sink.DorisDataSinkOptions.TABLE_CREATE_PARTITION_INCLUDE;
import static org.apache.flink.cdc.connectors.doris.sink.DorisDataSinkOptions.TABLE_CREATE_PARTITION_KEY;
import static org.apache.flink.cdc.connectors.doris.sink.DorisDataSinkOptions.TABLE_CREATE_PARTITION_UNIT;

/** Utilities for doris schema. */
public class DorisSchemaUtils {

    public static final String DEFAULT_DATE = "1970-01-01";
    public static final String DEFAULT_DATETIME = "1970-01-01 00:00:00";

    /**
     * Get partition info by config. Currently only supports DATE/TIMESTAMP AUTO RANGE PARTITION and
     * doris version should greater than 2.1.6
     *
     * @param config
     * @param schema
     * @param tableId
     * @return
     */
    public static Tuple2<String, String> getPartitionInfo(
            Configuration config, Schema schema, TableId tableId) {
        Map<String, String> autoPartitionProperties =
                DorisDataSinkOptions.getPropertiesByPrefix(
                        config, TABLE_CREATE_AUTO_PARTITION_PROPERTIES_PREFIX);
        String excludes = autoPartitionProperties.get(TABLE_CREATE_PARTITION_EXCLUDE);
        if (!StringUtils.isNullOrWhitespaceOnly(excludes)) {
            Selectors selectExclude =
                    new Selectors.SelectorsBuilder().includeTables(excludes).build();
            if (selectExclude.isMatch(tableId)) {
                return null;
            }
        }

        String includes = autoPartitionProperties.get(TABLE_CREATE_PARTITION_INCLUDE);
        if (!StringUtils.isNullOrWhitespaceOnly(includes)) {
            Selectors selectInclude =
                    new Selectors.SelectorsBuilder().includeTables(includes).build();
            if (!selectInclude.isMatch(tableId)) {
                return null;
            }
        }

        String partitionKey =
                autoPartitionProperties.get(
                        tableId.identifier() + "." + TABLE_CREATE_PARTITION_KEY);
        String partitionUnit =
                autoPartitionProperties.get(
                        tableId.identifier() + "." + TABLE_CREATE_PARTITION_UNIT);
        if (StringUtils.isNullOrWhitespaceOnly(partitionKey)) {
            partitionKey = autoPartitionProperties.get(TABLE_CREATE_DEFAULT_PARTITION_KEY);
        }
        if (StringUtils.isNullOrWhitespaceOnly(partitionUnit)) {
            partitionUnit = autoPartitionProperties.get(TABLE_CREATE_DEFAULT_PARTITION_UNIT);
        }

        if (schema.getColumn(partitionKey).isPresent()
                && !StringUtils.isNullOrWhitespaceOnly(partitionKey)) {

            DataType dataType = schema.getColumn(partitionKey).get().getType();
            if (dataType instanceof LocalZonedTimestampType
                    || dataType instanceof TimestampType
                    || dataType instanceof ZonedTimestampType
                    || dataType instanceof DateType) {
                return new Tuple2<>(partitionKey, partitionUnit);
            }
        }
        return null;
    }
}
