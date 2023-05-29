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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of the <code>RowProcessor</code> interface.
 *
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @see RowProcessor
 */
public class BasicRowProcessor implements RowProcessor {

    /**
     * The default BeanProcessor instance to use if not supplied in the
     * constructor.
     */
    private static final BeanProcessor defaultConvert = new BeanProcessor();

    /**
     * The Singleton instance of this class.
     */
    private static final BasicRowProcessor instance = new BasicRowProcessor();

    /**
     * Returns the Singleton instance of this class.
     *
     * @return The single instance of this class.
     * @deprecated Create instances with the constructors instead.  This will
     * be removed after DbUtils 1.1.
     */
    public static BasicRowProcessor instance() {
        return instance;
    }

    /**
     * Use this to process beans.
     */
    private BeanProcessor convert;

    /**
     * BasicRowProcessor constructor.  Bean processing defaults to a
     * BeanProcessor instance.
     */
    public BasicRowProcessor() {
        this(defaultConvert);
    }

    /**
     * BasicRowProcessor constructor.
     * @param convert The BeanProcessor to use when converting columns to
     * bean properties.
     * @since DbUtils 1.1
     */
    public BasicRowProcessor(BeanProcessor convert) {
        super();
        this.convert = convert;
    }

    /**
     * Convert a <code>ResultSet</code> row into an <code>Object[]</code>.
     * This implementation copies column values into the array in the same
     * order they're returned from the <code>ResultSet</code>.  Array elements
     * will be set to <code>null</code> if the column was SQL NULL.
     *
     */
    @Override
    public Object[] toArray(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        Object[] result = new Object[cols];

        for (int i = 0; i < cols; i++) {
            result[i] = rs.getObject(i + 1);
        }

        return result;
    }

    /**
     * Convert a <code>ResultSet</code> row into a JavaBean.  This
     * implementation delegates to a BeanProcessor instance.
     */
    @Override
    public Object toBean(ResultSet rs, Class type) throws SQLException {
        return this.convert.toBean(rs, type);
    }

    /**
     * Convert a <code>ResultSet</code> into a <code>List</code> of JavaBeans.
     * This implementation delegates to a BeanProcessor instance.
     */
    @Override
    public List toBeanList(ResultSet rs, Class type) throws SQLException {
        return this.convert.toBeanList(rs, type);
    }

    /**
     * Convert a <code>ResultSet</code> row into a <code>Map</code>.  This
     * implementation returns a <code>Map</code> with case insensitive column
     * names as keys.  Calls to <code>map.get("COL")</code> and
     * <code>map.get("col")</code> return the same value.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map toMap(ResultSet rs) throws SQLException {
        Map result = new CaseInsensitiveHashMap();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        for (int i = 1; i <= cols; i++) {
            result.put(rsmd.getColumnName(i), rs.getObject(i));
        }

        return result;
    }

    /**
     * A Map that converts all keys to lowercase Strings for case insensitive
     * lookups.  This is needed for the toMap() implementation because
     * databases don't consistently handle the casing of column names.
     */
    private static class CaseInsensitiveHashMap extends HashMap {

        /**
         * @see Map#containsKey(Object)
         */
        @Override
        public boolean containsKey(Object key) {
            return super.containsKey(key.toString().toLowerCase());
        }

        /**
         * @see Map#get(Object)
         */
        @Override
        public Object get(Object key) {
            return super.get(key.toString().toLowerCase());
        }

        /**
         * @see Map#put(Object, Object)
         */
        @SuppressWarnings("unchecked")
        @Override
        public Object put(Object key, Object value) {
            return super.put(key.toString().toLowerCase(), value);
        }

        /**
         * @see Map#putAll(Map)
         */
        @SuppressWarnings("unchecked")
        @Override
        public void putAll(Map m) {
            for (Entry entry : (Iterable<Entry>) m.entrySet()) {
                Object value = entry.getValue();
                // put as lowerCase
                this.put(entry.getKey(), value);
            }
        }

        /**
         * @see Map#remove(Object)
         */
        @Override
        public Object remove(Object key) {
            return super.remove(key.toString().toLowerCase());
        }
    }
}