/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.impl.builder;

import io.jmix.core.Metadata;
import io.jmix.reports.annotation.AvailableForRoles;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportRole;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.role.ResourceRoleRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component("reports_AnnotatedReportRoleExtractor")
public class AnnotatedReportRoleExtractorImpl implements AnnotatedReportRoleExtractor {

    protected final ResourceRoleRepository resourceRoleRepository;
    protected final Metadata metadata;

    public AnnotatedReportRoleExtractorImpl(ResourceRoleRepository resourceRoleRepository,
                                            Metadata metadata) {
        this.resourceRoleRepository = resourceRoleRepository;
        this.metadata = metadata;
    }

    @Override
    public Set<ReportRole> extractRoles(Object definitionInstance, Report report) {
        AvailableForRoles annotation = definitionInstance.getClass().getAnnotation(AvailableForRoles.class);
        if (annotation == null) {
            return Collections.emptySet();
        }

        Set<ReportRole> roles = new HashSet<>();

        for (String roleCode : annotation.roleCodes()) {
            ResourceRole role;
            try {
                role = resourceRoleRepository.getRoleByCode(roleCode);
            } catch (IllegalStateException ex) {
                throw new InvalidReportDefinitionException(
                        String.format("Resource role %s is not registered: %s", roleCode, annotation),
                        ex
                );
            }

            ReportRole reportRole = convertToReportRole(report, role);
            roles.add(reportRole);
        }

        for (Class<?> roleClass : annotation.roleClasses()) {
            String roleCode = getRoleCodeByClass(roleClass, annotation);

            ResourceRole role;
            try {
                role = resourceRoleRepository.getRoleByCode(roleCode);
            } catch (IllegalStateException ex) {
                throw new InvalidReportDefinitionException(
                        String.format("Resource role %s is not registered: %s", roleClass.getName(), annotation),
                        ex
                );
            }

            ReportRole reportRole = convertToReportRole(report, role);
            roles.add(reportRole);
        }

        return Collections.unmodifiableSet(roles);
    }

    protected String getRoleCodeByClass(Class<?> roleClass, AvailableForRoles annotation) {
        io.jmix.security.role.annotation.ResourceRole resourceRoleAnnotation
                = roleClass.getAnnotation(io.jmix.security.role.annotation.ResourceRole.class);

        if (resourceRoleAnnotation == null) {
            throw new InvalidReportDefinitionException(
                    String.format("Invalid value for roleClasses: '%s', class must be marked with @%s. %s",
                            roleClass.getName(), io.jmix.security.role.annotation.ResourceRole.class.getSimpleName(), annotation)
            );
        }
        return resourceRoleAnnotation.code();
    }

    protected ReportRole convertToReportRole(Report report, ResourceRole role) {
        ReportRole reportRole = metadata.create(ReportRole.class);
        reportRole.setReport(report);
        reportRole.setRoleCode(role.getCode());
        reportRole.setRoleName(role.getName());
        return reportRole;
    }
}
