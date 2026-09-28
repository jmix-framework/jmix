/*
 * Copyright 2026 Haulmont.
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

package test_support.role_localization;

import io.jmix.core.ClassManager;
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilderImpl;
import io.jmix.security.impl.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.security.impl.role.builder.extractor.RowLevelPolicyExtractor;
import io.jmix.security.model.BaseRole;

import java.util.Collection;

/**
 * Overrides the hook of the base parameters the way an application may, to check that the hook is still called.
 */
public class PrefixingRoleBuilder extends AnnotatedRoleBuilderImpl {

    public PrefixingRoleBuilder(Collection<ResourcePolicyExtractor> resourcePolicyExtractors,
                                Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors,
                                ClassManager classManager) {
        super(resourcePolicyExtractors, rowLevelPolicyExtractors, classManager);
    }

    @Override
    protected void initBaseParameters(BaseRole role, String name, String code, String description) {
        super.initBaseParameters(role, "[custom] " + name, code, description);
    }
}
