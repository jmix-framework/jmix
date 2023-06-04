/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.eclipselink.impl.dbms;

import io.jmix.data.persistence.DbmsFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.HashMap;
import java.util.Map;

@Component("oracleDbmsFeatures")
public class OracleDbmsFeatures implements DbmsFeatures {

    private static final Logger log = LoggerFactory.getLogger(OracleDbmsFeatures.class);

    @Override
    public Map<String, String> getJpaParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("eclipselink.target-database", "io.jmix.eclipselink.impl.dbms.JmixOraclePlatform");
        return params;
    }

    @Override
    public String getTimeStampType() {
        return "timestamp";
    }

    @Nullable
    @Override
    public String getUuidTypeClassName() {
        return null;
    }

    @Nullable
    @Override
    public String getTransactionTimeoutStatement() {
        return null;
    }

    @Override
    public String getUniqueConstraintViolationPattern() {
        return "ORA-00001: (?:.*) \\((?:.*\\.)(.+)\\)";
    }

    @Override
    public boolean isNullsLastSorting() {
        return true;
    }

    @Override
    public boolean supportsLobSortingAndFiltering() {
        return false;
    }

    @Override
    public Integer getMaxIdsBatchSize() {
        return 1000;
    }

    @Override
    public String getTypeAndVersion() {
        return "oracle";
    }
}
