/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.impl.repository.support.method_metadata;

import io.jmix.core.repository.JmixDataRepository;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link RepositoryProxyPostProcessor} to add interceptor designated for determining and storing {@link CrudMethodMetadata}
 * information for currently invoked {@link JmixDataRepository} method.
 * Implements {@link CrudMethodMetadata.Accessor} to access stored metadata.
 * <p>
 * Based on {@code org.springframework.data.jpa.repository.support.CrudMethodMetatdataPostProcessor} logic.
 */
public class CrudMethodMetadataAccessingPostProcessor implements RepositoryProxyPostProcessor, CrudMethodMetadata.Accessor {
    private static final Logger log = LoggerFactory.getLogger(QueryLookupStrategy.class);

    private static ThreadLocal<CrudMethodMetadata> currentMetadata = new ThreadLocal<>();

    @Override
    public void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {
        factory.addAdvice(new CrudMethodMetadataPopulatingMethodInterceptor(repositoryInformation));
    }

    @Override
    public CrudMethodMetadata getCrudMethodMetadata() {
        return currentMetadata.get();
    }

    static class CrudMethodMetadataPopulatingMethodInterceptor implements MethodInterceptor {

        private RepositoryInformation repositoryInformation;
        private final ConcurrentMap<Method, CrudMethodMetadata> metadataCache = new ConcurrentHashMap<>();
        private final Set<Method> implementations = new HashSet<>();

        public CrudMethodMetadataPopulatingMethodInterceptor(RepositoryInformation repositoryInformation) {
            this.repositoryInformation = repositoryInformation;
            ReflectionUtils.doWithMethods(repositoryInformation.getRepositoryInterface(), implementations::add,
                    method -> !repositoryInformation.isQueryMethod(method));
        }

        @Nullable
        @Override
        public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();
            if (!implementations.contains(method)) {
                return invocation.proceed();
            }

            CrudMethodMetadata methodMetadata = metadataCache.get(method);

            if (methodMetadata == null) {
                methodMetadata = MethodMetadataHelper.collectMethodMetadata(method, repositoryInformation.getRepositoryInterface());
                CrudMethodMetadata contained = metadataCache.putIfAbsent(method, methodMetadata);
                if (contained != null) {
                    methodMetadata = contained;
                } else {
                    log.debug("Metadata for '{}.{}' cached: '{}' ",
                            repositoryInformation.getRepositoryInterface().getName(), method.getName(), methodMetadata);
                }
            }

            CrudMethodMetadata oldMetadata = currentMetadata.get();
            currentMetadata.set(methodMetadata);
            try {
                Object result = invocation.proceed();
                return result;
            } finally {
                currentMetadata.set(oldMetadata);
            }
        }
    }

}
