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

package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.entity.HasUuid;
import io.jmix.core.EntityInitializer;
import io.jmix.core.JmixOrder;
import io.jmix.core.UuidProvider;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component(EntityUuidInitializer.NAME)
public class EntityUuidInitializer implements EntityInitializer, Ordered {
    public static final String NAME = "cuba_EntityUuidInitializer";

    @Override
    public void initEntity(Object entity) {
        if (entity instanceof HasUuid
                && ((HasUuid) entity).getUuid() == null) {
            ((HasUuid) entity).setUuid(UuidProvider.createUuid());
        }
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 10;
    }
}
