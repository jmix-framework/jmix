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

package io.jmix.core.impl;

import io.jmix.core.UnsafeDataManager;
import io.jmix.core.constraint.AccessConstraint;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("core_UnsafeDataManager")
public class UnsafeDataManagerImpl extends DataManagerImpl implements UnsafeDataManager {
    @Override
    protected List<AccessConstraint<?>> getRegisteredConstraints() {
        return Collections.emptyList();
    }
}
