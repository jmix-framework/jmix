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

package io.jmix.core.entity;

import io.jmix.core.Id;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.annotation.JmixGeneratedValue;

import java.util.UUID;

/**
 * Used to clearly identify entity.<br>
 * Property selection performs as follows:
 * <ol>
 *     <li>{@link Id} property used if it has {@link UUID} type and {@link JmixGeneratedValue} annotation,</li>
 *     <li>any other UUID @JmixGeneratedValue property chosen if @Id property doesn't satisfy conditions above, </li>
 *     <li>interface isn't applied otherwise.</li>
 * </ol>
 */

@Internal
public interface EntityEntryHasUuid {

    UUID getUuid();

    void setUuid(UUID uuid);
}
