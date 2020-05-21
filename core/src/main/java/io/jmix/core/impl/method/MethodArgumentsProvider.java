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

import io.jmix.core.MetadataTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Utility class to provide {@link Method} argument values from {@link MethodArgumentResolver}
 */
public class MethodArgumentsProvider {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private MethodArgumentResolver resolver;

    private final Logger logger = LoggerFactory.getLogger(MetadataTools.class);

    public MethodArgumentsProvider(MethodArgumentResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Get the method argument values for the current request, checking the provided
     * argument values and falling back to the configured argument resolvers.
     * <p>The resulting array will be passed into method annotated by {@link io.jmix.core.metamodel.annotation.InstanceName}.
     */
    public Object[] getMethodArgumentValues(Method method, Object... providedArgs) throws Exception {

        MethodParameter[] parameters = getMethodParameters(method);
        if (ObjectUtils.isEmpty(parameters)) {
            return EMPTY_ARGS;
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            args[i] = findProvidedArgument(parameter, providedArgs);
            if (args[i] != null) {
                continue;
            }
            if (!this.resolver.supportsParameter(parameter)) {
                throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
            }
            try {
                args[i] = this.resolver.resolveArgument(parameter);
            } catch (Exception ex) {
                // Leave stack trace for later, exception may actually be resolved and handled...
                if (logger.isDebugEnabled()) {
                    String exMsg = ex.getMessage();
                    if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
                        logger.debug(formatArgumentError(parameter, exMsg));
                    }
                }
                throw ex;
            }
        }
        return args;
    }

    private MethodParameter[] getMethodParameters(Method method) {
        MethodParameter[] methodParameters = new MethodParameter[method.getParameterCount()];
        for (int i = 0; i < method.getParameterCount(); i++) {
            methodParameters[i] = new MethodParameter(method, i);
        }
        return methodParameters;
    }

    @Nullable
    protected static Object findProvidedArgument(MethodParameter parameter, @Nullable Object... providedArgs) {
        if (!ObjectUtils.isEmpty(providedArgs)) {
            for (Object providedArg : providedArgs) {
                if (parameter.getParameterType().isInstance(providedArg)) {
                    return providedArg;
                }
            }
        }
        return null;
    }

    protected static String formatArgumentError(MethodParameter param, String message) {
        return "Could not resolve parameter [" + param.getParameterIndex() + "] in " +
                param.getExecutable().toGenericString() + (org.springframework.util.StringUtils.hasText(message) ? ": " + message : "");
    }
}
