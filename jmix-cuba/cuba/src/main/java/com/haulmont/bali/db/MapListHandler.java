/*
 * Copyright 2019 Haulmont.
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
package com.haulmont.bali.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapListHandler implements ResultSetHandler<List<Map<String, Object>>> {
    /**
     * The RowProcessor implementation to use when converting rows
     * into Maps.
     */
    private RowProcessor convert = ArrayHandler.ROW_PROCESSOR;

    public MapListHandler() {
    }

    public MapListHandler(RowProcessor convert) {
        this.convert = convert;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        while (rs.next()) {
            result.add(convert.toMap(rs));
        }
        return result;
    }
}