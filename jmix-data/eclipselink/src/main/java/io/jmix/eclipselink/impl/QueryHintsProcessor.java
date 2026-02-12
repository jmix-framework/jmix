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

package io.jmix.eclipselink.impl;


import io.jmix.data.PersistenceHints;
import org.eclipse.persistence.jpa.JpaQuery;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Component("eclipselink_QueryHintsProcessor")
public class QueryHintsProcessor {

    protected Map<String, BiConsumer<JpaQuery, Object>> hintHandlers = new HashMap<>();

    @PostConstruct
    protected void init() {
        hintHandlers.put(PersistenceHints.SQL_HINT,
                (query, value) -> query.setHint(org.eclipse.persistence.config.QueryHints.HINT, value));
        hintHandlers.put(PersistenceHints.MSSQL_RECOMPILE_HINT,
                (query, value) -> query.setHint(org.eclipse.persistence.config.QueryHints.HINT, "OPTION(RECOMPILE)"));
    }

    public void applyQueryHint(JpaQuery query, String hintName, Object value) {
        BiConsumer<JpaQuery, Object> handler = hintHandlers.get(hintName);
        if (handler != null) {
            handler.accept(query, value);
        }
    }
}
