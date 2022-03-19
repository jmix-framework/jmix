/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.core.impl.method;

import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Resolves method parameters by delegating to a list of registered
 * {@link MethodArgumentResolver MethodArgumentResolvers}.
 * Previously resolved method parameters are cached for faster lookups.
 */
public class MethodArgumentResolverComposite extends CachedArgumentResolverComposite {

    private final List<MethodArgumentResolver> argumentResolvers = new LinkedList<>();

    /**
     * Add the given {@link MethodArgumentResolver}.
     */
    public MethodArgumentResolverComposite addResolver(MethodArgumentResolver resolver) {
        this.argumentResolvers.add(resolver);
        return this;
    }

    /**
     * Add the given {@link MethodArgumentResolver MethodArgumentResolvers}.
     */
    public MethodArgumentResolverComposite addResolvers(
            @Nullable MethodArgumentResolver... resolvers) {

        if (resolvers != null) {
            Collections.addAll(this.argumentResolvers, resolvers);
        }
        return this;
    }

    /**
     * Add the given {@link MethodArgumentResolver MethodArgumentResolvers}.
     */
    public MethodArgumentResolverComposite addResolvers(
            @Nullable List<? extends MethodArgumentResolver> resolvers) {

        if (resolvers != null) {
            this.argumentResolvers.addAll(resolvers);
        }
        return this;
    }

    /**
     * Return a read-only list with the contained resolvers, or an empty list.
     */
    public List<MethodArgumentResolver> getResolvers() {
        return Collections.unmodifiableList(this.argumentResolvers);
    }

    /**
     * Clear the list of configured resolvers.
     *
     * @since 4.3
     */
    public void clear() {
        this.argumentResolvers.clear();
    }
}
