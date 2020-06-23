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

package io.jmix.security.role.builder.extractor;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Component(PredicateRowLevelPolicyExtractor.NAME)
public class PredicateRowLevelPolicyExtractor implements RowLevelPolicyExtractor {

    public static final String NAME = "sec_InMemoryRowLevelPolicyExtractor";

    protected Metadata metadata;

    @Autowired
    public PredicateRowLevelPolicyExtractor(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Collection<RowLevelPolicy> extractRowLevelPolicies(Method method) {
        Set<RowLevelPolicy> policies = new HashSet<>();
        //todo MG check parameter types
        PredicateRowLevelPolicy[] annotations = method.getAnnotationsByType(PredicateRowLevelPolicy.class);
        for (PredicateRowLevelPolicy annotation : annotations) {
            for (RowLevelPolicyAction action : annotation.actions()) {
                Class<? extends Entity> entityClass = annotation.entityClass();
                MetaClass metaClass = metadata.getClass(entityClass);
                Predicate<? extends Entity> predicate;
                try {
                    predicate = (Predicate<? extends Entity>) method.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Cannot evaluate row level policy predicate", e);
                }
                RowLevelPolicy rowLevelPolicy = new RowLevelPolicy(metaClass.getName(), action, predicate);
                policies.add(rowLevelPolicy);
            }
        }
        return policies;
    }
}
