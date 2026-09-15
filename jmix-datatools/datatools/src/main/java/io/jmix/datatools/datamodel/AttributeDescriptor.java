/*
 * Copyright 2026 Haulmont.
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

package io.jmix.datatools.datamodel;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Description of an attribute contributed to the data model.
 *
 * @param name         attribute name
 * @param javaType     displayed Java type
 * @param mandatory    whether a value is required
 * @param dynamic      whether the attribute is defined at runtime rather than on the classpath
 * @param column       physical column, or {@code null} when the attribute has no column of its own
 * @param noColumnText text shown in place of a column name when {@code column} is {@code null}
 * @param relation     relation to another entity, or {@code null} when the attribute is not a reference
 */
@NullMarked
public record AttributeDescriptor(String name,
                                  String javaType,
                                  boolean mandatory,
                                  boolean dynamic,
                                  @Nullable ColumnDescriptor column,
                                  @Nullable String noColumnText,
                                  @Nullable RelationDescriptor relation) {

    /**
     * Physical column of a contributed attribute.
     *
     * @param tableName  table the column belongs to
     * @param columnName column name
     * @param dbType     database type, or {@code null} to look it up in the database metadata
     */
    public record ColumnDescriptor(String tableName, String columnName, @Nullable String dbType) {
    }

    /**
     * Relation declared by a contributed attribute.
     *
     * @param type       relation type
     * @param entityName name of the entity at the other end
     */
    public record RelationDescriptor(RelationType type, String entityName) {
    }
}
