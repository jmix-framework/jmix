/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports;

import io.jmix.core.LoadContext;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.core.QueryUtils;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@Component("report_ReportSecurityManager")
public class ReportSecurityManager {
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected ResourceRoleRepository resourceRoleRepository;

    @Autowired
    protected RoleAssignmentRepository roleAssignmentRepository;

    /**
     * Apply security constraints for query to select reports available by roles and screen restrictions
     */
    public void applySecurityPolicies(LoadContext lc, @Nullable String screen, @Nullable UserDetails userDetails) {
        QueryTransformer transformer = queryTransformerFactory.transformer(lc.getQuery().getQueryString());
        if (screen != null) {
            transformer.addWhereAsIs("r.screensIdx like :screen escape '\\'");
            lc.getQuery().setParameter("screen", wrapCodeParameterForSearch(screen));
        }
        if (userDetails != null) {
            List<BaseRole> roles = roleAssignmentRepository.getAssignmentsByUsername(userDetails.getUsername()).stream()
                    .map(roleAssignment -> resourceRoleRepository.findRoleByCode(roleAssignment.getRoleCode()))
                    .collect(Collectors.toList());

            StringBuilder roleCondition = new StringBuilder("r.rolesIdx is null");
            for (int i = 0; i < roles.size(); i++) {
                BaseRole role = roles.get(i);
                String paramName = "role" + (i + 1);
                roleCondition.append(" or r.rolesIdx like :").append(paramName).append(" escape '\\'");
                lc.getQuery().setParameter(paramName, wrapCodeParameterForSearch(role.getCode()));
            }
            transformer.addWhereAsIs(roleCondition.toString());
        }
        lc.getQuery().setQueryString(transformer.getResult());
    }

    /**
     * Apply constraints for query to select reports which have input parameter with class matching inputValueMetaClass
     */
    public void applyPoliciesByEntityParameters(LoadContext lc, @Nullable MetaClass inputValueMetaClass) {
        if (inputValueMetaClass != null) {
            QueryTransformer transformer = queryTransformerFactory.transformer(lc.getQuery().getQueryString());
            StringBuilder parameterTypeCondition = new StringBuilder("r.inputEntityTypesIdx like :type escape '\\'");
            lc.getQuery().setParameter("type", wrapCodeParameterForSearch(inputValueMetaClass.getName()));
            List<MetaClass> ancestors = inputValueMetaClass.getAncestors();
            for (int i = 0; i < ancestors.size(); i++) {
                MetaClass metaClass = ancestors.get(i);
                String paramName = "type" + (i + 1);
                parameterTypeCondition.append(" or r.inputEntityTypesIdx like :").append(paramName).append(" escape '\\'");
                lc.getQuery().setParameter(paramName, wrapCodeParameterForSearch(metaClass.getName()));
            }
            transformer.addWhereAsIs(String.format("(%s)", parameterTypeCondition.toString()));
            lc.getQuery().setQueryString(transformer.getResult());
        }
    }

    protected String wrapCodeParameterForSearch(String value) {
        return "%," + QueryUtils.escapeForLike(value) + ",%";
    }
}
