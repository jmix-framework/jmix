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

import io.jmix.core.EntityInitializer;
import io.jmix.core.JmixEntity;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.EntityEntryHasUuid;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component(EntityUuidInitializer.NAME)
public class EntityUuidInitializer implements EntityInitializer, Ordered {
    public static final String NAME = "cuba_EntityUuidInitializer";

    @Override
    public void initEntity(JmixEntity entity) {
        if (entity.__getEntityEntry() instanceof EntityEntryHasUuid
                && ((EntityEntryHasUuid) entity.__getEntityEntry()).getUuid() == null) {
            ((EntityEntryHasUuid) entity).setUuid(UuidProvider.createUuid());
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 10;
    }
}
