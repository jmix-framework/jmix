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

package com.haulmont.cuba.core.global.filter;

import com.google.common.collect.ImmutableList;
import io.jmix.core.Entity;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static com.haulmont.cuba.core.global.filter.Op.*;

@Component("cuba_OpManager")
public class OpManagerImpl implements OpManager {

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MetadataTools metadata;

    protected static final List<Class> dateTimeClasses = ImmutableList.of(Date.class, LocalDate.class, LocalDateTime.class,
            OffsetDateTime.class);
    protected static final List<Class> timeClasses = ImmutableList.of(LocalTime.class, OffsetTime.class);

    @Override
    public EnumSet<Op> availableOps(Class javaClass) {
        if (String.class.equals(javaClass))
            return EnumSet.of(EQUAL, IN, NOT_IN, NOT_EQUAL, CONTAINS, DOES_NOT_CONTAIN, NOT_EMPTY, STARTS_WITH, ENDS_WITH);

        else if (dateTimeClasses.contains(javaClass))
            return EnumSet.of(EQUAL, IN, NOT_IN, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESSER, LESSER_OR_EQUAL, NOT_EMPTY, DATE_INTERVAL);

        else if (timeClasses.contains(javaClass))
            return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESSER, LESSER_OR_EQUAL, NOT_EMPTY, DATE_INTERVAL);

        else if (Number.class.isAssignableFrom(javaClass))
            return EnumSet.of(EQUAL, IN, NOT_IN, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESSER, LESSER_OR_EQUAL, NOT_EMPTY);

        else if (Boolean.class.equals(javaClass))
            return EnumSet.of(EQUAL, NOT_EQUAL, NOT_EMPTY);

        else if (UUID.class.equals(javaClass)
                || Enum.class.isAssignableFrom(javaClass)
                || Entity.class.isAssignableFrom(javaClass))
            return EnumSet.of(EQUAL, IN, NOT_IN, NOT_EQUAL, NOT_EMPTY);

        else
            throw new UnsupportedOperationException("Unsupported java class: " + javaClass);
    }

    @Override
    public EnumSet<Op> availableOps(MetaClass metaClass, MetaProperty metaProperty) {
        Class javaClass = metaProperty.getJavaType();
        if (String.class.equals(javaClass)
                && metadataTools.isLob(metaProperty)
                && !metaClass.getStore().supportsLobSortingAndFiltering()) {
            return EnumSet.of(CONTAINS, DOES_NOT_CONTAIN, NOT_EMPTY, STARTS_WITH, ENDS_WITH);
        }
        return availableOps(javaClass);
    }
}
