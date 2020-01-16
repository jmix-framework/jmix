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

package io.jmix.core;

import io.jmix.core.entity.BaseEntityInternalAccess;
import io.jmix.core.entity.BaseGenericIdEntity;
import org.springframework.stereotype.Component;

@Component(EntitySystemStateSupport.NAME)
public class EntitySystemStateSupport {

    public static final String NAME = "jmix_EntitySystemStateSupport";

    public void copySystemState(BaseGenericIdEntity src, BaseGenericIdEntity dst) {
        BaseEntityInternalAccess.copySystemState(src, dst);
    }

    public void mergeSystemState(BaseGenericIdEntity src, BaseGenericIdEntity dst) {
        BaseEntityInternalAccess.copySystemState(src, dst);
    }
}
