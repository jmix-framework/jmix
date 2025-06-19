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

package io.jmix.reports.impl.repository;

import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.EntityOp;
import io.jmix.reports.ReportFilter;
import io.jmix.reports.ReportLoadContext;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.entity.*;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reports.util.MsgBundleTools;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("report_ReportRepository")
public class ReportRepositoryImpl implements ReportRepository {
    /**
     * Fetch plan enough to passing report to the running engine.
     */
    public static final String RUN_FETCH_PLAN = "report.run";

    protected final AnnotatedReportHolder annotatedReportHolder;
    protected final AnnotatedReportScanner reportScanner;
    protected final DataManager dataManager;
    protected final ReportsPersistence reportsPersistence;
    protected final RepositoryUtil repositoryUtil;
    protected final MsgBundleTools msgBundleTools;
    protected final ReportSecurityManager reportSecurityManager;
    protected final ResourceRoleRepository resourceRoleRepository;
    protected final RoleAssignmentRepository roleAssignmentRepository;
    protected final EntityStates entityStates;
    protected final Metadata metadata;
    protected final AccessManager accessManager;

    public ReportRepositoryImpl(AnnotatedReportHolder annotatedReportHolder, AnnotatedReportScanner reportScanner, DataManager dataManager,
                                ReportsPersistence reportsPersistence, RepositoryUtil repositoryUtil, MsgBundleTools msgBundleTools,
                                ReportSecurityManager reportSecurityManager, ResourceRoleRepository resourceRoleRepository,
                                RoleAssignmentRepository roleAssignmentRepository, EntityStates entityStates, Metadata metadata,
                                AccessManager accessManager) {
        this.annotatedReportHolder = annotatedReportHolder;
        this.reportScanner = reportScanner;
        this.dataManager = dataManager;
        this.reportsPersistence = reportsPersistence;
        this.repositoryUtil = repositoryUtil;
        this.msgBundleTools = msgBundleTools;
        this.reportSecurityManager = reportSecurityManager;
        this.resourceRoleRepository = resourceRoleRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.entityStates = entityStates;
        this.metadata = metadata;
        this.accessManager = accessManager;
    }

    @Override
    public Collection<Report> getAllReports() {
        return loadList(new ReportLoadContext(new ReportFilter()));
    }

    @Override
    public List<Report> loadList(ReportLoadContext loadContext) {
        if (!isReadPermitted()) {
            return Collections.emptyList();
        }
        Collection<Report> annotatedReports = annotatedReportHolder.getAllReports();
        List<Report> reportsFromDb = loadReportsFromDatabase(loadContext.getFilter());

        Stream<Report> stream = Stream.concat(
                annotatedReports.stream(),
                reportsFromDb.stream()
        );

        List<Report> result = applyFilterSortPagination(stream, loadContext);
        return result;
    }

    private List<Report> applyFilterSortPagination(Stream<Report> infoStream, ReportLoadContext loadContext) {
        FilteringContext filteringContext = fillFilteringContext(loadContext.getFilter());
        Stream<Report> stream = infoStream.filter(r -> satisfies(r, loadContext.getFilter(), filteringContext));

        if (loadContext.getSort() != null && !loadContext.getSort().getOrders().isEmpty()) {
            stream = stream.sorted(createReportComparator(loadContext.getSort()));
        }

        if (loadContext.getFirstResult() != 0) {
            stream = stream.skip(loadContext.getFirstResult());
        }

        if (loadContext.getMaxResults() != 0) {
            stream = stream.limit(loadContext.getMaxResults());
        }

        return stream.toList();
    }

    private Comparator<Report> createReportComparator(Sort sort) {
        return repositoryUtil.comparatorBuilder(Report.class)
                // built-in comparator (AbstractComparator) works incorrectly for this attribute
                .customProperty(ReportLoadContext.GROUP_SORT_KEY, r -> {
                    return r.getGroup() != null ? r.getGroup().getInstanceName(msgBundleTools) : null;
                })
                .customProperty(ReportLoadContext.LOCALIZED_NAME_SORT_KEY, r -> {
                    return r.getInstanceName(msgBundleTools);
                })
                .build(sort);
    }

    private FilteringContext fillFilteringContext(ReportFilter filter) {
        FilteringContext filteringContext = new FilteringContext();
        if (filter.getInputValueMetaClass() != null) {
            Set<String> metaClassNames = new HashSet<>();
            metaClassNames.add(filter.getInputValueMetaClass().getName());
            for (MetaClass ancestor : filter.getInputValueMetaClass().getAncestors()) {
                metaClassNames.add(ancestor.getName());
            }
            filteringContext.inputValueMetaClassNames = metaClassNames;
        }

        if (filter.getUser() != null) {
            Set<String> codes = roleAssignmentRepository.getAssignmentsByUsername(filter.getUser().getUsername()).stream()
                    .filter(roleAssignment -> roleAssignment.getRoleType().equals(RoleAssignmentRoleType.RESOURCE))
                    .map(roleAssignment -> resourceRoleRepository.findRoleByCode(roleAssignment.getRoleCode()))
                    .filter(Objects::nonNull)
                    .map(BaseRole::getCode)
                    .collect(Collectors.toSet());

            filteringContext.resourceRoleCodes = codes;
        }
        return filteringContext;
    }

    private boolean satisfies(Report report, ReportFilter filter, FilteringContext filteringContext) {
        return repositoryUtil.containsIgnoreCase(report.getCode(), filter.getCodeContains())
               && repositoryUtil.containsIgnoreCase(report.getInstanceName(msgBundleTools), filter.getNameContains())
               && repositoryUtil.entityEquals(report.getGroup(), filter.getGroup())
               && repositoryUtil.dateAfterOrEquals(report.getUpdateTs(), filter.getUpdatedAfter())
               && repositoryUtil.equalsTo(report.getRestAccess(), filter.getRestAccessible())
               && repositoryUtil.equalsTo(report.getSystem(), filter.getSystem())
               && viewIdSatisfies(report, filter.getViewId())
               && roleSatisfies(report, filter.getUser(), filteringContext)
               && inputValueMetaClassSatisfies(report, filter.getInputValueMetaClass(), filteringContext)
               && outputTypeSatisfies(report, filter.getOutputType());
    }

    protected boolean viewIdSatisfies(Report report, @Nullable String viewId) {
        if (report.getSource() == ReportSource.DATABASE) {
            // we rely on this condition being checked in ReportSecurityManager, on data loading stage.
            // moreover, report screens aren't loaded from db here
            return true;
        }
        if (viewId != null) {
            List<ReportScreen> screenList = report.getReportScreens() != null
                    ? report.getReportScreens() : Collections.emptyList();

            return screenList.stream()
                    .anyMatch(rs -> rs.getScreenId().equals(viewId));
        }
        return true;
    }

    protected boolean roleSatisfies(Report report, @Nullable UserDetails user, FilteringContext filteringContext) {
        if (report.getSource() == ReportSource.DATABASE) {
            // we rely on this condition being checked in ReportSecurityManager, on data loading stage.
            // moreover, report roles aren't loaded from db here
            return true;
        }
        if (user != null) {
            Set<ReportRole> roleSet = report.getReportRoles();
            if (CollectionUtils.isEmpty(roleSet)) {
                // no report roles means no filtering
                return true;
            }
            return roleSet.stream()
                    .anyMatch(rr -> filteringContext.resourceRoleCodes.contains(rr.getRoleCode()));
        }
        return true;
    }

    protected boolean inputValueMetaClassSatisfies(Report report, @Nullable MetaClass inputValueMetaClass, FilteringContext filteringContext) {
        if (report.getSource() == ReportSource.DATABASE) {
            // we rely on this condition being checked in ReportSecurityManager, on data loading stage.
            // moreover, input parameters aren't loaded from db here
            return true;
        }
        if (inputValueMetaClass != null) {
            List<ReportInputParameter> parameterList = report.getInputParameters() != null
                    ? report.getInputParameters() : Collections.emptyList();
            return parameterList.stream()
                    .anyMatch(ip -> ip.getEntityMetaClass() != null
                                    && filteringContext.inputValueMetaClassNames.contains(ip.getEntityMetaClass()));
        }
        return true;
    }

    protected boolean outputTypeSatisfies(Report report, @Nullable ReportOutputType outputType) {
        if (report.getSource() == ReportSource.DATABASE) {
            // we rely on this condition being checked in ReportSecurityManager, on data loading stage.
            // moreover, templates aren't loaded from db here
            return true;
        }
        if (outputType != null) {
            return report.getTemplates().stream()
                    .anyMatch(t -> t.getReportOutputType() == outputType);
        }
        return true;
    }

    @Override
    public int getTotalCount(ReportFilter filter) {
        if (!isReadPermitted()) {
            return 0;
        }
        Collection<Report> annotatedReports = annotatedReportHolder.getAllReports();
        List<Report> reportsFromDb = loadReportsFromDatabase(filter);

        FilteringContext filteringContext = fillFilteringContext(filter);
        long count = Stream.concat(annotatedReports.stream(), reportsFromDb.stream())
                .filter(rg -> satisfies(rg, filter, filteringContext))
                .count();

        return (int) count;
    }

    protected List<Report> loadReportsFromDatabase(ReportFilter filter) {
        // some of filtering conditions are applied right now, at the moment of loading from DB
        //  later they are skipped during in-memory filtering phase
        List<Report> reportsFromDb = reportSecurityManager.getAvailableReports(
                filter.getViewId(),
                filter.getUser(),
                filter.getInputValueMetaClass(),
                filter.getSystem(),
                filter.getOutputType(),
                null
        );
        return reportsFromDb;
    }

    @Nullable
    @Override
    public Report loadForRunningByCode(String reportCode) {
        if (!isReadPermitted()) {
            return null;
        }
        Report report = annotatedReportHolder.getByCode(reportCode);
        if (report != null) {
            return report;
        }
        report = dataManager.load(Report.class)
                .condition(PropertyCondition.equal("code", reportCode))
                .fetchPlan(RUN_FETCH_PLAN)
                .optional()
                .orElse(null);

        return report;
    }

    @Override
    public boolean existsReportByGroup(ReportGroupInfo group) {
        if (!isReadPermitted()) {
            return false;
        }
        if (group.getSource() == ReportSource.ANNOTATED_CLASS) {
            for (Report report : annotatedReportHolder.getAllReports()) {
                if (report.getGroup() != null && report.getGroup().getCode().equals(group.getCode())) {
                    return true;
                }
            }
        }

        Optional<Report> report = dataManager.load(Report.class)
                .query("select r from report_Report r where r.group.id = :groupId")
                .parameter("groupId", group.getId())
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .optional();

        return report.isPresent();
    }

    @Override
    public Report save(Report report) {
        return reportsPersistence.save(report);
    }

    @Override
    public Report reloadForRunning(Report report) {
        if (!isReadPermitted()) {
            throw new AccessDeniedException("entity", metadata.getClass(Report.class).getName(), EntityOp.READ.getId());
        }

        if (report.getSource() == ReportSource.ANNOTATED_CLASS) {
            return report;
        }

        if (report.getIsTmp()) {
            return report;
        }

        if (entityStates.isLoadedWithFetchPlan(report, RUN_FETCH_PLAN)) {
            return report;
        }

        return dataManager.load(Id.of(report))
                .fetchPlan(RUN_FETCH_PLAN)
                .one();
    }

    @Override
    public ReportTemplate reloadTemplateForRunning(ReportTemplate template) {
        if (!entityStates.isLoadedWithFetchPlan(template, "template.edit")) {
            template = dataManager.load(Id.of(template))
                    .fetchPlan("template.edit")
                    .one();
        }
        return template;
    }

    protected boolean isReadPermitted() {
        CrudEntityContext showScreenContext = new CrudEntityContext(metadata.getClass(Report.class));
        accessManager.applyRegisteredConstraints(showScreenContext);

        return showScreenContext.isReadPermitted();
    }

    @EventListener
    public void handleApplicationStartedEvent(ApplicationStartedEvent event) {
        reportScanner.importGroupDefinitions();
        reportScanner.importReportDefinitions();
    }

    // serves as cache for some calculations
    public class FilteringContext {
        Set<String> resourceRoleCodes;
        Set<String> inputValueMetaClassNames;
    }
}
