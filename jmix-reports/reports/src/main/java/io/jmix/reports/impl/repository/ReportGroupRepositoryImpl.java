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
import io.jmix.reports.ReportGroupFilter;
import io.jmix.reports.ReportGroupLoadContext;
import io.jmix.reports.ReportGroupRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportGroupInfo;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reports.impl.AnnotatedReportGroupHolder;
import io.jmix.reports.util.MsgBundleTools;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component("report_ReportGroupRepository")
public class ReportGroupRepositoryImpl implements ReportGroupRepository {

    protected final AnnotatedReportGroupHolder annotatedReportGroupHolder;
    protected final DataManager dataManager;
    protected final Metadata metadata;
    protected final MsgBundleTools msgBundleTools;
    protected final EntityStates entityStates;
    protected final RepositoryUtil repositoryUtil;
    protected final AccessManager accessManager;

    public ReportGroupRepositoryImpl(AnnotatedReportGroupHolder annotatedReportGroupHolder, DataManager dataManager,
                                     Metadata metadata, MsgBundleTools msgBundleTools,
                                     EntityStates entityStates, RepositoryUtil repositoryUtil, AccessManager accessManager) {
        this.annotatedReportGroupHolder = annotatedReportGroupHolder;
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.msgBundleTools = msgBundleTools;
        this.entityStates = entityStates;
        this.repositoryUtil = repositoryUtil;
        this.accessManager = accessManager;
    }

    @Override
    public List<ReportGroupInfo> loadAll() {
        ReportGroupLoadContext context = new ReportGroupLoadContext(
                new ReportGroupFilter(null, null),
                Sort.by("localizedTitle"),
                0,
                0
        );
        return loadList(context);
    }

    @Override
    public List<ReportGroupInfo> loadList(ReportGroupLoadContext loadContext) {
        if (!isReadPermitted()) {
            return Collections.emptyList();
        }
        Collection<ReportGroup> annotatedGroups = annotatedReportGroupHolder.getAllGroups();
        List<ReportGroup> dbGroups = loadGroupsFromDatabase();

        Stream<ReportGroup> stream = Stream.concat(
                annotatedGroups.stream(),
                dbGroups.stream()
        );

        Stream<ReportGroupInfo> infoStream = stream.map(this::convertToInfo);

        List<ReportGroupInfo> result = applyFilterSortPagination(infoStream, loadContext);
        return result;
    }

    @Override
    public int getTotalCount(ReportGroupFilter filter) {
        if (!isReadPermitted()) {
            return 0;
        }
        Collection<ReportGroup> annotatedGroups = annotatedReportGroupHolder.getAllGroups();
        List<ReportGroup> dbGroups = loadGroupsFromDatabase();

        long count = Stream.concat(annotatedGroups.stream(), dbGroups.stream())
                .map(this::convertToInfo)
                .filter(rg -> satisfies(rg, filter))
                .count();

        return (int) count;
    }

    private List<ReportGroupInfo> applyFilterSortPagination(Stream<ReportGroupInfo> infoStream, ReportGroupLoadContext loadContext) {
        Stream<ReportGroupInfo> stream = infoStream.filter(rg -> satisfies(rg, loadContext.filter()));

        if (loadContext.sort() != null && !loadContext.sort().getOrders().isEmpty()) {
            Comparator<ReportGroupInfo> comparator = repositoryUtil.comparatorBuilder(ReportGroupInfo.class)
                    .build(loadContext.sort());
            stream = stream.sorted(comparator);
        }

        if (loadContext.firstResult() != 0) {
            stream = stream.skip(loadContext.firstResult());
        }

        if (loadContext.maxResults() != 0) {
            stream = stream.limit(loadContext.maxResults());
        }

        return stream.toList();
    }

    private boolean satisfies(ReportGroupInfo group, ReportGroupFilter filter) {
        return repositoryUtil.containsIgnoreCase(group.getCode(), filter.codeContains())
               && repositoryUtil.containsIgnoreCase(group.getLocalizedTitle(), filter.titleContains());
    }

    protected List<ReportGroup> loadGroupsFromDatabase() {
        // here additional optimisations could be done:
        // partially apply filter, sort, limit from load context,
        // remembering that final result list will always be smaller or equal to result list from db
        return dataManager.load(ReportGroup.class)
                .all()
                .fetchPlan(FetchPlan.BASE).list();
    }

    @Override
    public ReportGroupInfo convertToInfo(ReportGroup group) {
        ReportGroupInfo info = metadata.create(ReportGroupInfo.class);
        info.setId(group.getId());
        info.setCode(group.getCode());
        info.setLocalizedTitle(group.getInstanceName(msgBundleTools));
        info.setSource(group.getSource());
        info.setSystemFlag(group.getSystemFlag());
        entityStates.setNew(info, false); // some UI logic works differently for new entities
        return info;
    }

    @Override
    public ReportGroup loadModelObject(ReportGroupInfo reportGroupInfo) {
        if (reportGroupInfo.getSource() == ReportSource.DATABASE) {
            ReportGroup persistentEntity = dataManager.load(reportGroupInfo.toEntityId()).one();
            return persistentEntity;
        } else {
            ReportGroup group = annotatedReportGroupHolder.getGroupByCode(reportGroupInfo.getCode());
            if (group == null) {
                throw new NoResultException("Group with code " + reportGroupInfo.getCode() + " not found");
            }
            return group;
        }
    }

    @Override
    public void remove(ReportGroupInfo info) {
        if (info.getSource() != ReportSource.DATABASE) {
            throw new IllegalArgumentException("Cannot remove non-database report");
        }

        dataManager.remove(info.toEntityId());
    }

    protected boolean isReadPermitted() {
        CrudEntityContext showScreenContext = new CrudEntityContext(metadata.getClass(Report.class));
        accessManager.applyRegisteredConstraints(showScreenContext);

        return showScreenContext.isReadPermitted();
    }
}
