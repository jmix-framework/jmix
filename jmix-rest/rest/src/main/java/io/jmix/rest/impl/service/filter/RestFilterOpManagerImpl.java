/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.impl.service.filter;

import com.google.common.collect.ImmutableList;
import io.jmix.core.Metadata;
import io.jmix.core.Entity;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.*;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static io.jmix.rest.impl.service.filter.RestFilterOp.*;

@Component("rest_RestOpManager")
public class RestFilterOpManagerImpl implements RestFilterOpManager {

    @Autowired
    protected Metadata metadata;

    protected static final List<Class> DATE_TIME_CLASSES = ImmutableList.of(Date.class, LocalDate.class, LocalDateTime.class,
            OffsetDateTime.class);
    protected static final List<Class> TIME_CLASSES = ImmutableList.of(LocalTime.class, OffsetTime.class);

    @Override
    public EnumSet<RestFilterOp> availableOps(Class javaClass) {
        if (String.class.equals(javaClass))
            return EnumSet.of(EQUAL, IN, NOT_IN, NOT_EQUAL, CONTAINS, DOES_NOT_CONTAIN, NOT_EMPTY, STARTS_WITH, ENDS_WITH, IS_NULL);

        else if (DATE_TIME_CLASSES.contains(javaClass))
            return EnumSet.of(EQUAL, IN, NOT_IN, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESSER, LESSER_OR_EQUAL, NOT_EMPTY, DATE_INTERVAL, IS_NULL);

        else if (TIME_CLASSES.contains(javaClass))
            return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESSER, LESSER_OR_EQUAL, NOT_EMPTY, DATE_INTERVAL, IS_NULL);

        else if (Number.class.isAssignableFrom(javaClass))
            return EnumSet.of(EQUAL, IN, NOT_IN, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESSER, LESSER_OR_EQUAL, NOT_EMPTY, IS_NULL);

        else if (Boolean.class.equals(javaClass))
            return EnumSet.of(EQUAL, NOT_EQUAL, NOT_EMPTY, IS_NULL);

        else if (UUID.class.equals(javaClass)
                || Enum.class.isAssignableFrom(javaClass)
                || Entity.class.isAssignableFrom(javaClass))
            return EnumSet.of(EQUAL, IN, NOT_IN, NOT_EQUAL, NOT_EMPTY, IS_NULL);

        else
            throw new UnsupportedOperationException("Unsupported java class: " + javaClass);
    }
}
