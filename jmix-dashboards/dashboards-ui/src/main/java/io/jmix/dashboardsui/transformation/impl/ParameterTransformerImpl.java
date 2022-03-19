/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboardsui.transformation.impl;

import io.jmix.core.DataManager;
import io.jmix.core.Entity;
import io.jmix.core.FluentLoader;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.dashboards.model.parameter.ParameterType;
import io.jmix.dashboards.model.parameter.type.*;
import io.jmix.dashboardsui.annotation.WidgetParam;
import io.jmix.dashboardsui.transformation.ParameterTransformer;
import io.jmix.ui.screen.ScreenFragment;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.jmix.dashboards.model.parameter.ParameterType.*;

@Component("dshbrd_ParameterTransformer")
public class ParameterTransformerImpl implements ParameterTransformer {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Metadata metadata;

    @Override
    public Object transform(ParameterValue parameterValue) {
        if (parameterValue == null) {
            return null;
        }
        if (parameterValue instanceof EntityListParameterValue) {
            return loadEntities((EntityListParameterValue) parameterValue);
        } else if (parameterValue instanceof EntityParameterValue) {
            return loadEntity((EntityParameterValue) parameterValue);
        } else {
            return ((HasPrimitiveValue) parameterValue).getValue();
        }
    }

    protected List<Entity> loadEntities(EntityListParameterValue parameter) {
        return parameter.getEntityValues().stream()
                .map(this::loadEntity)
                .collect(Collectors.toList());
    }

    protected Entity loadEntity(EntityParameterValue parameter) {
        Class entityClass = metadata.getClass(parameter.getMetaClassName()).getJavaClass();

        FluentLoader.ById loader = dataManager.load(entityClass)
                .id(java.util.UUID.fromString(parameter.getEntityId()));
        if (parameter.getFetchPlanName() != null) {
            loader.fetchPlan(parameter.getFetchPlanName());
        }
        return (Entity) loader.optional().orElse(null);
    }

    public boolean compareParameterTypes(ParameterType parameterType, Field field) {
        if (parameterType == ENTITY) {
            return Entity.class.isAssignableFrom(field.getType());
        } else if (parameterType == ENTITY_LIST) {
            return List.class.isAssignableFrom(field.getType());
        } else if (parameterType == ENUM) {
            return field.isEnumConstant();
        } else if (parameterType == DATE) {
            return Date.class.isAssignableFrom(field.getType());
        } else if (parameterType == DATETIME) {
            return Date.class.isAssignableFrom(field.getType());
        } else if (parameterType == TIME) {
            return Date.class.isAssignableFrom(field.getType());
        } else if (parameterType == ParameterType.UUID) {
            return UUID.class.isAssignableFrom(field.getType());
        } else if (parameterType == INTEGER) {
            return Integer.class.isAssignableFrom(field.getType()) || int.class.isAssignableFrom(field.getType());
        } else if (parameterType == STRING) {
            return String.class.isAssignableFrom(field.getType());
        } else if (parameterType == DECIMAL) {
            return BigDecimal.class.isAssignableFrom(field.getType());
        } else if (parameterType == BOOLEAN) {
            return Boolean.class.isAssignableFrom(field.getType()) || boolean.class.isAssignableFrom(field.getType());
        } else if (parameterType == LONG) {
            return Long.class.isAssignableFrom(field.getType()) || long.class.isAssignableFrom(field.getType());
        }
        return false;
    }

    public ParameterType getParameterType(Field field) {
        if (Entity.class.isAssignableFrom(field.getType())) {
            return ENTITY;
        } else if (List.class.isAssignableFrom(field.getType())) {
            return ENTITY_LIST;
        } else if (field.isEnumConstant()) {
            return ENUM;
        } else if (Date.class.isAssignableFrom(field.getType())) {
            return DATETIME;
        } else if (UUID.class.isAssignableFrom(field.getType())) {
            return ParameterType.UUID;
        } else if (Integer.class.isAssignableFrom(field.getType()) || int.class.isAssignableFrom(field.getType())) {
            return INTEGER;
        } else if (String.class.isAssignableFrom(field.getType())) {
            return STRING;
        } else if (BigDecimal.class.isAssignableFrom(field.getType())) {
            return DECIMAL;
        } else if (Boolean.class.isAssignableFrom(field.getType()) || boolean.class.isAssignableFrom(field.getType())) {
            return ParameterType.BOOLEAN;
        } else if (Long.class.isAssignableFrom(field.getType()) || long.class.isAssignableFrom(field.getType())) {
            return ParameterType.LONG;
        }
        return STRING;
    }

    @Override
    public ParameterValue createParameterValue(Field field, ScreenFragment widgetFragment) {
        ParameterValue parameterValue = null;
        ParameterType parameterType = getParameterType(field);
        try {
            if (parameterType == ENTITY) {
                Object entity = FieldUtils.readField(field, widgetFragment, true);
                if (entity != null) {
                    WidgetParam ann = field.getAnnotation(WidgetParam.class);
                    String fetchPlan = StringUtils.isNoneBlank(ann.fetchPlanName()) ? ann.fetchPlanName() : null;
                    parameterValue = new EntityParameterValue(metadata.getClass(entity).getName(),
                            EntityValues.getId(entity).toString(), fetchPlan);
                }
            } else if (parameterType == ENTITY_LIST) {
                List<Entity> listEntity = (List) FieldUtils.readField(field, widgetFragment, true);
                WidgetParam ann = field.getAnnotation(WidgetParam.class);
                if (listEntity != null) {
                    String fetchPlan = StringUtils.isNoneBlank(ann.fetchPlanName()) ? ann.fetchPlanName() : null;
                    List<EntityParameterValue> resultList = listEntity.stream()
                            .map(entity -> new EntityParameterValue(metadata.getClass(entity).getName(), EntityValues.getId(entity).toString(), fetchPlan))
                            .collect(Collectors.toList());
                    parameterValue = new EntityListParameterValue(resultList);
                }
            } else if (parameterType == ENUM) {
                EnumClass<String> rawValue = (EnumClass) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    String enumClassName = rawValue.getClass().toString();
                    parameterValue = new EnumParameterValue(enumClassName);
                }
            } else if (parameterType == DATE) {
                Date rawValue = (Date) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new DateParameterValue(rawValue);
                }
            } else if (parameterType == DATETIME) {
                Date rawValue = (Date) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new DateParameterValue(rawValue);
                }
            } else if (parameterType == TIME) {
                Date rawValue = (Date) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new DateParameterValue(rawValue);
                }
            } else if (parameterType == ParameterType.UUID) {
                UUID rawValue = (UUID) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new UuidParameterValue(rawValue);
                }
            } else if (parameterType == INTEGER) {
                Integer rawValue = (Integer) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new IntegerParameterValue(rawValue);
                }
            } else if (parameterType == STRING) {
                String rawValue = (String) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new StringParameterValue(rawValue);
                }
            } else if (parameterType == DECIMAL) {
                BigDecimal rawValue = (BigDecimal) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new DecimalParameterValue(rawValue);
                }
            } else if (parameterType == BOOLEAN) {
                Boolean rawValue = (Boolean) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new BooleanParameterValue(rawValue);
                }
            } else if (parameterType == LONG) {
                Long rawValue = (Long) FieldUtils.readField(field, widgetFragment, true);
                if (rawValue != null) {
                    parameterValue = new LongParameterValue(rawValue);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error on parameter init", e);
        }
        return parameterValue;
    }

    @Override
    @Nullable
    public ParameterValue createParameterValue(Object obj) {
        if (obj instanceof Boolean) {
            return new BooleanParameterValue((Boolean) obj);
        } else if (obj instanceof Date) {
            return new DateParameterValue((Date) obj);
        } else if (obj instanceof BigDecimal) {
            return new DecimalParameterValue((BigDecimal) obj);
        } else if (obj instanceof Long) {
            return new LongParameterValue((Long) obj);
        } else if (obj instanceof Integer) {
            return new IntegerParameterValue((Integer) obj);
        } else if (obj instanceof String) {
            return new StringParameterValue((String) obj);
        } else if (obj instanceof UUID) {
            return new UuidParameterValue((UUID) obj);
        } else if (obj instanceof EnumClass) {
            return new EnumParameterValue(obj.getClass().toString());
        } else if (obj instanceof Entity) {
            Entity entity = (Entity) obj;
            return new EntityParameterValue(metadata.getClass(entity).getName(), EntityValues.getId(entity).toString(), null);
        } else if (obj instanceof List) {
            List<?> list = (List) obj;
            List<EntityParameterValue> entityList = list.stream()
                    .filter(t -> t instanceof Entity)
                    .map(t -> (Entity) t)
                    .map(entity -> new EntityParameterValue(metadata.getClass(entity).getName(), EntityValues.getId(entity).toString(), null))
                    .collect(Collectors.toList());
            return new EntityListParameterValue(entityList);
        }
        return null;
    }

}
