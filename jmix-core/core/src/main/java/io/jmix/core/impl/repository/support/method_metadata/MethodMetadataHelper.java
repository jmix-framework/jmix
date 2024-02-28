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

import io.jmix.core.impl.repository.query.utils.LoaderHelper;
import io.jmix.core.repository.ApplyConstraints;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.QueryHints;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.MethodMetadata;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.QueryHint;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains {@link MethodMetadata} analyse logic such as {@link ApplyConstraints} determination.
 */
public class MethodMetadataHelper {

    public static CrudMethodMetadata collectMethodMetadata(Method method, Class<?> repositoryInterface) {
        ApplyConstraints applyConstraints = determineApplyConstraints(method, repositoryInterface);
        Map<String, Serializable> queryHints = determineQueryHints(method);
        return new CrudMethodMetadata(applyConstraints.value(), queryHints);
    }

    /**
     * Determines {@link ApplyConstraints} for particular repository query as follows:
     * <ol>
     *     <li>look for annotation on method hierarchy starting from {@code method};</li>
     *     <li>if not found, search through repository interface hierarchy starting from specified {@code repositoryInterface};</li>
     *     <li>annotation from base {@link JmixDataRepository} will be returned if no other annotation determined on application repositories </li>
     * </ol>
     * <p>
     * Based on logic for {@link Transactional} annotation as determined in {@code org.springframework.data.repository.core.support.TransactionalRepositoryProxyPostProcessor.RepositoryAnnotationTransactionAttributeSource#computeTransactionAttribute(java.lang.reflect.Method, java.lang.Class)}
     *
     * @param method              for which search performed
     * @param repositoryInterface that currently instantiating
     * @return ApplyConstraints
     * @throws RuntimeException in case of {@code repositoryInterface} is not inherited from JmixDataRepository
     */
    public static ApplyConstraints determineApplyConstraints(Method method, Class<?> repositoryInterface) {
        // Search through method hierarchy
        ApplyConstraints annotation = AnnotatedElementUtils.findMergedAnnotation(method, ApplyConstraints.class);
        if (annotation != null)
            return annotation;

        // Search through repository hierarchy
        annotation = AnnotatedElementUtils.findMergedAnnotation(repositoryInterface, ApplyConstraints.class);
        if (annotation != null)
            return annotation;

        throw new RuntimeException("Internal error: @ApplyConstraints should have been found at least on JmixDataRepository");
    }


    public static Map<String, Serializable> determineQueryHints(Method method) {
        QueryHints hints = AnnotatedElementUtils.findMergedAnnotation(method, QueryHints.class);
        if (hints != null) {
            Map<String, Serializable> result = new HashMap<>();
            for (QueryHint hint : hints.value()) {
                result.put(hint.name(), LoaderHelper.parseHint(hint.name(), hint.value()));
            }
            return result;
        } else {
            return Collections.emptyMap();
        }
    }
}
