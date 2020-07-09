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

import org.springframework.stereotype.Component;

@Component(EntitySystemStateSupport.NAME)
public class EntitySystemStateSupport {

    public static final String NAME = "core_EntitySystemStateSupport";

    public void copySystemState(JmixEntity src, JmixEntity dst) {
        dst.__getEntityEntry().copy(src.__getEntityEntry());
    }

    public void mergeSystemState(JmixEntity src, JmixEntity dst) {
        dst.__getEntityEntry().copy(src.__getEntityEntry());
    }
}
