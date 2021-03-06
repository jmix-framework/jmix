/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.testsupport;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.Metadata;
import io.jmix.core.JmixEntity;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.authentication.CoreAuthenticationToken;
import io.jmix.core.security.impl.CoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;

@Component
public class TestSupport {
    @Autowired
    protected Persistence persistence;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected StandardSerialization standardSerialization;

    public <T> T reserialize(Serializable object) throws Exception {
        if (object == null)
            return null;

        return (T) standardSerialization.deserialize(standardSerialization.serialize(object));
    }

    public void deleteRecord(String table, Object... ids) {
        deleteRecord(table, "ID", ids);
    }

    public void deleteRecord(String table, String primaryKeyCol, Object... ids) {
        for (Object id : ids) {
            String sql = "delete from " + table + " where " + primaryKeyCol + " = '" + id.toString() + "'";
            JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
            try {
                jdbcTemplate.update(sql);
            } catch (DataAccessException e) {
                throw new RuntimeException(
                        String.format("Unable to delete record %s from %s", id.toString(), table), e);
            }
        }
    }

    public void deleteRecord(JmixEntity... entities) {
        if (entities == null) {
            return;
        }

        for (JmixEntity entity : entities) {
            if (entity == null) {
                continue;
            }

            MetaClass metaClass = metadata.getClass(entity.getClass());

            String table = metadataTools.getDatabaseTable(metaClass);
            String primaryKey = metadataTools.getPrimaryKeyName(metaClass);
            if (table == null || primaryKey == null) {
                throw new RuntimeException("Unable to determine table or primary key name for " + entity);
            }

            deleteRecord(table, primaryKey, EntityValues.getId(entity));
        }
    }

    public static void setAuthenticationToSecurityContext() {
        CoreUser user = new CoreUser("test_admin", "test_admin", "test_admin");
        CoreAuthenticationToken authentication = new CoreAuthenticationToken(user, Collections.emptyList());
        authentication.setLocale(Locale.US);
        SecurityContextHelper.setAuthentication(authentication);
    }
}
