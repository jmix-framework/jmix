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

package io.jmix.core.impl.method;

import java.util.List;

/**
 * Strategy interface to Resolves method parameters by delegating to a list of registered
 * {@link MethodArgumentResolver MethodArgumentResolvers}.
 */
public interface ArgumentResolverComposite extends MethodArgumentResolver {

    /**
     * Return a read-only list with the contained resolvers, or an empty list.
     */
    List<MethodArgumentResolver> getResolvers();
}
