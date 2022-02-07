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

package io.jmix.security.impl.role.provider;

import io.jmix.core.DevelopmentException;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.security.SecurityProperties;
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.role.ResourceRoleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Role provider that gets roles from classes annotated with
 * {@link io.jmix.security.role.annotation.ResourceRole}.
 */
@Component("sec_AnnotatedResourceRoleProvider")
public class AnnotatedResourceRoleProvider implements ResourceRoleProvider {

    private final JmixModulesClasspathScanner classpathScanner;

    private final ResourceRoleDetector detector;

    private final AnnotatedRoleBuilder annotatedRoleBuilder;

    protected Map<String, ResourceRole> roles;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    public AnnotatedResourceRoleProvider(JmixModulesClasspathScanner classpathScanner,
                                         AnnotatedRoleBuilder annotatedRoleBuilder,
                                         ResourceRoleDetector detector) {
        this.classpathScanner = classpathScanner;
        this.annotatedRoleBuilder = annotatedRoleBuilder;
        this.detector = detector;

        buildRolesCache();
    }

    @Override
    public Collection<ResourceRole> getAllRoles() {
        return roles.values();
    }

    @Override
    @Nullable
    public ResourceRole findRoleByCode(String code) {
        return roles.get(code);
    }

    @Override
    public boolean deleteRole(ResourceRole role) {
        return this.roles.remove(role.getCode()) != null;
    }

    public void refreshRoles() {
        if (securityProperties.isAnnotatedRolesHotDeployEnabled()) {
            classpathScanner.refreshClassNames(detector);
            buildRolesCache();
            return;
        }
        throw new DevelopmentException("Annotated roles hot deploy is forbidden");
    }

    private void buildRolesCache() {
        Set<String> classNames = classpathScanner.getClassNames(ResourceRoleDetector.class);

        roles = classNames.stream()
                .map(annotatedRoleBuilder::createResourceRole)
                .collect(Collectors.toMap(ResourceRole::getCode, Function.identity()));
    }

}
