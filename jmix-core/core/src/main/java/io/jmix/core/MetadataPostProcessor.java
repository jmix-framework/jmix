/*
 * Copyright 2024 Haulmont.
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

import io.jmix.core.metamodel.model.Session;

/**
 * Interface to be implemented by beans in add-ons and applications for processing metadata right after it is built.
 * <p>
 * The implementation can have the {@link org.springframework.core.annotation.Order} annotation
 * with a {@link JmixOrder} value.
 */
public interface MetadataPostProcessor {

    /**
     * Called by the framework right after metadata session is built and before the application start.
     */
    void process(Session session);
}
