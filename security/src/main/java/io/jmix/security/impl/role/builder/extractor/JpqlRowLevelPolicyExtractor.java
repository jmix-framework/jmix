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

package io.jmix.security.impl.role.builder.extractor;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.JpqlRowLevelPolicyContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component("sec_JpqlRowLevelPolicyExtractor")
public class JpqlRowLevelPolicyExtractor implements RowLevelPolicyExtractor {

    private Metadata metadata;

    @Autowired
    public JpqlRowLevelPolicyExtractor(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Collection<RowLevelPolicy> extractRowLevelPolicies(Method method) {
        Set<RowLevelPolicy> policies = new HashSet<>();
        Set<JpqlRowLevelPolicy> annotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(method,
                JpqlRowLevelPolicy.class, JpqlRowLevelPolicyContainer.class);
        for (JpqlRowLevelPolicy annotation : annotations) {
            Class<?> entityClass = annotation.entityClass();
            MetaClass metaClass = metadata.getClass(entityClass);
            RowLevelPolicy rowLevelPolicy = new RowLevelPolicy(metaClass.getName(),
                    annotation.where(),
                    annotation.join(),
                    Collections.singletonMap("uniqueKey", UUID.randomUUID().toString()));
            policies.add(rowLevelPolicy);
        }
        return policies;
    }
}
