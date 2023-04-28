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

package io.jmix.dynattr;

import io.jmix.core.metamodel.model.MetaProperty;

import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface AttributeDefinition extends Serializable {
    interface Configuration extends Serializable {
        boolean isLookup();

        @Nullable
        Integer getRowsCount();

        @Nullable
        String getFormWidth();

        Set<String> getScreens();

        @Nullable
        String getJoinClause();

        @Nullable
        String getWhereClause();

        @Nullable
        String getColumnName();

        @Nullable
        String getColumnAlignment();

        @Nullable
        String getNumberFormatPattern();

        @Nullable
        Integer getColumnWidth();

        @Nullable
        String getValidatorGroovyScript();

        @Nullable
        Integer getMinInt();

        @Nullable
        Integer getMaxInt();

        @Nullable
        Double getMinDouble();

        @Nullable
        Double getMaxDouble();

        @Nullable
        BigDecimal getMinDecimal();

        @Nullable
        BigDecimal getMaxDecimal();

        @Nullable
        String getOptionsLoaderScript();

        @Nullable
        OptionsLoaderType getOptionsLoaderType();

        @Nullable
        String getRecalculationScript();

        @Nullable
        Integer getColumnNumber();

        @Nullable
        Integer getRowNumber();

        @Nullable
        List<String> getDependsOnAttributeCodes();

        @Nullable
        String getLookupScreen();
    }

    String getId();

    String getCode();

    MetaProperty getMetaProperty();

    @Nullable
    String getName();

    @Nullable
    String getDescription();

    AttributeType getDataType();

    @Nullable
    Class<?> getJavaType();

    boolean isCollection();

    boolean isReadOnly();

    boolean isRequired();

    @Nullable
    String getEnumeration();

    @Nullable
    Object getDefaultValue();

    boolean isDefaultDateCurrent();

    int getOrderNo();

    @Nullable
    String getNameMsgBundle();

    @Nullable
    String getDescriptionsMsgBundle();

    @Nullable
    String getEnumerationMsgBundle();

    Configuration getConfiguration();

    Object getSource();
}
