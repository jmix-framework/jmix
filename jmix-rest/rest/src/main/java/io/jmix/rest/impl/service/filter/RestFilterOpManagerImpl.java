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
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.jmix.core.querycondition.PropertyCondition.Operation.*;

@Component("rest_RestOpManager")
public class RestFilterOpManagerImpl implements RestFilterOpManager {

    @Autowired
    protected Metadata metadata;

    protected static final List<Class> DATE_TIME_CLASSES = ImmutableList.of(Date.class, LocalDate.class, LocalDateTime.class,
            OffsetDateTime.class, java.sql.Date.class, java.sql.Time.class);
    protected static final List<Class> TIME_CLASSES = ImmutableList.of(LocalTime.class, OffsetTime.class);

    @Override
    public Set<String> availableOperations(Class javaClass) {
        if (String.class.equals(javaClass))
            return Set.of(EQUAL,IN_LIST, NOT_IN_LIST, NOT_EQUAL, CONTAINS, NOT_CONTAINS, IS_SET, STARTS_WITH, ENDS_WITH);

        else if (DATE_TIME_CLASSES.contains(javaClass))
             return Set.of(EQUAL, IN_LIST, NOT_IN_LIST, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET);

        else if (TIME_CLASSES.contains(javaClass))
            return Set.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET);

        else if (Number.class.isAssignableFrom(javaClass))
            return Set.of(EQUAL, IN_LIST, NOT_IN_LIST, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET);

        else if (Boolean.class.equals(javaClass))
            return Set.of(EQUAL, NOT_EQUAL, IS_SET);

        else if (UUID.class.equals(javaClass)
                || Enum.class.isAssignableFrom(javaClass)
                || Entity.class.isAssignableFrom(javaClass))
            return Set.of(EQUAL, IN_LIST, NOT_IN_LIST, NOT_EQUAL, IS_SET);

        else
            throw new UnsupportedOperationException("Unsupported java class: " + javaClass);
    }
}
