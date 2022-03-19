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

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves method parameters by delegating to a list of registered
 * {@link MethodArgumentResolver MethodArgumentResolvers}.
 * Previously resolved method parameters are cached for faster lookups.
 */
public abstract class CachedArgumentResolverComposite implements MethodArgumentResolver, ArgumentResolverComposite {

    private final Map<MethodParameter, MethodArgumentResolver> argumentResolverCache =
            new ConcurrentHashMap<>(256);


    /**
     * Return a read-only list with the contained resolvers, or an empty list.
     */
    public abstract List<MethodArgumentResolver> getResolvers();

    /**
     * Whether the given {@linkplain MethodParameter method parameter} is
     * supported by any registered {@link MethodArgumentResolver}.
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return getArgumentResolver(parameter) != null;
    }

    /**
     * Iterate over registered
     * {@link MethodArgumentResolver MethodArgumentResolvers}
     * and invoke the one that supports it.
     *
     * @throws IllegalArgumentException if no suitable argument resolver is found
     */
    @Override
    @Nullable
    public Object resolveArgument(MethodParameter parameter) throws Exception {

        MethodArgumentResolver resolver = getArgumentResolver(parameter);
        if (resolver == null) {
            throw new IllegalArgumentException("Unsupported parameter type [" +
                    parameter.getParameterType().getName() + "]. supportsParameter should be called first.");
        }
        return resolver.resolveArgument(parameter);
    }

    /**
     * Find a registered {@link MethodArgumentResolver} that supports
     * the given method parameter.
     */
    @Nullable
    private MethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
        MethodArgumentResolver result = this.argumentResolverCache.get(parameter);
        if (result == null) {
            for (MethodArgumentResolver resolver : getResolvers()) {
                if (resolver.supportsParameter(parameter)) {
                    result = resolver;
                    this.argumentResolverCache.put(parameter, result);
                    break;
                }
            }
        }
        return result;
    }

}
