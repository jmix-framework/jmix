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

package io.jmix.reports.impl.repository;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation detail. Use {@link io.jmix.reports.ReportRepository} instead.
 */
@Component("report_ReportSecurityManager")
public class ReportSecurityManager {
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected ResourceRoleRepository resourceRoleRepository;

    @Autowired
    protected RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected DataManager dataManager;

    /**
     * Apply security constraints for query to select reports available by roles and screen restrictions
     *
     * @param lc load context
     * @param screen screen id
     * @param userDetails user details
     */
    public void applySecurityPolicies(LoadContext<Report> lc, @Nullable String screen, @Nullable UserDetails userDetails) {
        QueryTransformer transformer = queryTransformerFactory.transformer(lc.getQuery().getQueryString());
        if (screen != null) {
            transformer.addWhereAsIs("r.screensIdx like :screen escape '\\'");
            lc.getQuery().setParameter("screen", wrapCodeParameterForSearch(screen));
        }
        if (userDetails != null) {
            List<BaseRole> roles = roleAssignmentRepository.getAssignmentsByUsername(userDetails.getUsername()).stream()
                    .filter(roleAssignment -> roleAssignment.getRoleType().equals(RoleAssignmentRoleType.RESOURCE))
                    .map(roleAssignment -> resourceRoleRepository.findRoleByCode(roleAssignment.getRoleCode()))
                    .filter(Objects::nonNull)
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
     * @param lc load context
     * @param inputValueMetaClass meta class of input parameter value
     */
    protected void applyPoliciesByEntityParameters(LoadContext<Report> lc, @Nullable MetaClass inputValueMetaClass) {
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

    /**
     * Apply condition to the query: report must have at least one template with given output type.
     */
    protected void applyOutputType(LoadContext<Report> lc, @Nullable ReportOutputType outputType) {
        if (outputType != null) {
            QueryTransformer transformer = queryTransformerFactory.transformer(lc.getQuery().getQueryString());

            String join = "join {E}.templates rtmpl";
            String where = "rtmpl.reportOutputType = :templateOutputType";
            transformer.addJoinAndWhere(join, where);
            transformer.addDistinct(); // because we are joining one-to-many relation

            lc.getQuery().setQueryString(transformer.getResult());
            lc.getQuery().setParameter("templateOutputType", outputType);
        }
    }

    /**
     * Returns a sorted list of reports, available for certain screen, user and input parameter
     *
     * @param screenId            - id of the screen
     * @param user                - caller user
     * @param inputValueMetaClass - meta class of report input parameter
     * @param sort                - sorting type for the reports list
     * @param system              - system flag
     * @return list of available reports
     */
    public List<Report> getAvailableReports(@Nullable String screenId, @Nullable UserDetails user,
                                            @Nullable MetaClass inputValueMetaClass, @Nullable Boolean system,
                                            @Nullable ReportOutputType outputType,
                                            @Nullable Sort sort) {
        MetaClass metaClass = metadata.getClass(Report.class);
        LoadContext<Report> lc = new LoadContext<>(metaClass);
        lc.setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(Report.class, "report.view");

        lc.setFetchPlan(fetchPlan);
        String queryString = "select r from report_Report r";

        if (system != null) {
            if (system) {
                queryString += " where r.system = true";
            } else {
                queryString += " where r.system <> true";
            }
        }

        LoadContext.Query query = lc.setQueryString(queryString);
        if (sort != null) {
            query.setSort(sort);
        }
        applySecurityPolicies(lc, screenId, user);
        applyPoliciesByEntityParameters(lc, inputValueMetaClass);
        applyOutputType(lc, outputType);
        return dataManager.loadList(lc);
    }

    protected String wrapCodeParameterForSearch(String value) {
        return "%," + QueryUtils.escapeForLike(value) + ",%";
    }

}
