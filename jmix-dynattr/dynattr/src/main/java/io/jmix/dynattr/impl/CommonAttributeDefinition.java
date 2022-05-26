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

package io.jmix.dynattr.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.OptionsLoaderType;
import io.jmix.dynattr.model.CategoryAttribute;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CommonAttributeDefinition implements AttributeDefinition, Serializable {
    private static final long serialVersionUID = -338980834303933684L;

    protected final CategoryAttribute attribute;
    protected final AttributeDefinition.Configuration configuration;
    protected final DynAttrMetaProperty metaProperty;

    protected static class CommonAttributeConfiguration implements AttributeDefinition.Configuration {
        private static final long serialVersionUID = 3088284638117897678L;

        protected final CategoryAttribute attribute;

        public CommonAttributeConfiguration(CategoryAttribute attribute) {
            this.attribute = attribute;
        }

        @Override
        public boolean isLookup() {
            return Boolean.TRUE.equals(attribute.getLookup());
        }

        @Override
        public Integer getRowsCount() {
            return attribute.getRowsCount();
        }

        @Override
        public String getFormWidth() {
            return attribute.getWidth();
        }

        @Override
        public Set<String> getScreens() {
            if (!Strings.isNullOrEmpty(attribute.getTargetScreens())) {
                return Sets.newHashSet(Splitter.on(",").omitEmptyStrings().split(attribute.getTargetScreens()));
            } else {
                return Collections.emptySet();
            }
        }

        @Override
        public String getJoinClause() {
            return attribute.getJoinClause();
        }

        @Override
        public String getWhereClause() {
            return attribute.getWhereClause();
        }

        @Override
        public String getColumnName() {
            return attribute.getConfiguration().getColumnName();
        }

        @Override
        public String getColumnAlignment() {
            return attribute.getConfiguration().getColumnAlignment();
        }

        @Override
        public String getNumberFormatPattern() {
            return attribute.getConfiguration().getNumberFormatPattern();
        }

        @Override
        public Integer getColumnWidth() {
            return attribute.getConfiguration().getColumnWidth();
        }

        @Override
        public String getValidatorGroovyScript() {
            return attribute.getConfiguration().getValidatorGroovyScript();
        }

        @Override
        public Integer getMinInt() {
            return attribute.getConfiguration().getMinInt();
        }

        @Override
        public Integer getMaxInt() {
            return attribute.getConfiguration().getMaxInt();
        }

        @Override
        public Double getMinDouble() {
            return attribute.getConfiguration().getMinDouble();
        }

        @Override
        public Double getMaxDouble() {
            return attribute.getConfiguration().getMaxDouble();
        }

        @Override
        public BigDecimal getMinDecimal() {
            return attribute.getConfiguration().getMinDecimal();
        }

        @Override
        public BigDecimal getMaxDecimal() {
            return attribute.getConfiguration().getMaxDecimal();
        }

        @Override
        public String getOptionsLoaderScript() {
            return attribute.getConfiguration().getOptionsLoaderScript();
        }

        @Override
        public OptionsLoaderType getOptionsLoaderType() {
            return attribute.getConfiguration().getOptionsLoaderType();
        }

        @Nullable
        @Override
        public String getRecalculationScript() {
            return attribute.getConfiguration().getRecalculationScript();
        }

        @Override
        public Integer getColumnNumber() {
            return attribute.getConfiguration().getColumnNumber();
        }

        @Override
        public Integer getRowNumber() {
            return attribute.getConfiguration().getRowNumber();
        }

        @Override
        public List<String> getDependsOnAttributeCodes() {
            return attribute.getConfiguration().getDependsOnAttributeCodes();
        }

        @Nullable
        @Override
        public String getLookupScreen() {
            return attribute.getScreen();
        }
    }

    public CommonAttributeDefinition(CategoryAttribute attribute, DynAttrMetaProperty metaProperty) {
        this.attribute = attribute;
        this.configuration = new CommonAttributeConfiguration(attribute);
        this.metaProperty = metaProperty;
    }

    @Override
    public String getId() {
        return attribute.getId().toString();
    }

    @Override
    public DynAttrMetaProperty getMetaProperty() {
        return metaProperty;
    }

    @Override
    public String getCode() {
        return attribute.getCode();
    }

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public String getDescription() {
        return attribute.getDescription();
    }

    @Override
    public AttributeType getDataType() {
        return attribute.getDataType();
    }

    @Override
    public Class<?> getJavaType() {
        return attribute.getJavaType();
    }

    @Override
    public boolean isCollection() {
        return Boolean.TRUE.equals(attribute.getIsCollection());
    }

    @Override
    public String getEnumeration() {
        return attribute.getEnumeration();
    }

    @Override
    public Object getDefaultValue() {
        AttributeType dataType = attribute.getDataType();
        if (dataType != null) {
            switch (dataType) {
                case INTEGER:
                    return attribute.getDefaultInt();
                case DOUBLE:
                    return attribute.getDefaultDouble();
                case DECIMAL:
                    return attribute.getDefaultDecimal();
                case BOOLEAN:
                    return attribute.getDefaultBoolean();
                case DATE:
                    return attribute.getDefaultDate();
                case DATE_WITHOUT_TIME:
                    return attribute.getDefaultDateWithoutTime();
                case STRING:
                case ENUMERATION:
                    return attribute.getDefaultString();
                case ENTITY:
                    return attribute.getDefaultEntity().getObjectEntityId();
            }
        }
        return null;
    }

    @Override
    public boolean isDefaultDateCurrent() {
        return Boolean.TRUE.equals(attribute.getDefaultDateIsCurrent());
    }

    @Override
    public int getOrderNo() {
        return attribute.getOrderNo() == null ? 0 : attribute.getOrderNo();
    }

    @Override
    public String getNameMsgBundle() {
        return attribute.getNameMsgBundle();
    }

    @Override
    public String getDescriptionsMsgBundle() {
        return attribute.getDescriptionsMsgBundle();
    }

    @Override
    public String getEnumerationMsgBundle() {
        return attribute.getEnumerationMsgBundle();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Object getSource() {
        return attribute;
    }

    @Override
    public boolean isReadOnly() {
        return !Strings.isNullOrEmpty(this.attribute.getConfiguration().getRecalculationScript());
    }

    @Override
    public boolean isRequired() {
        return Boolean.TRUE.equals(attribute.getRequired());
    }
}
