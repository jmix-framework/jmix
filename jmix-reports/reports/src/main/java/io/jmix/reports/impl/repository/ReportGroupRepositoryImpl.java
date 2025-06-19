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
import io.jmix.reports.entity.ReportGroup;
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
    protected final RepositoryUtil repositoryUtil;
    protected final AccessManager accessManager;

    public ReportGroupRepositoryImpl(AnnotatedReportGroupHolder annotatedReportGroupHolder, DataManager dataManager,
                                     Metadata metadata, MsgBundleTools msgBundleTools,
                                     RepositoryUtil repositoryUtil, AccessManager accessManager) {
        this.annotatedReportGroupHolder = annotatedReportGroupHolder;
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.msgBundleTools = msgBundleTools;
        this.repositoryUtil = repositoryUtil;
        this.accessManager = accessManager;
    }

    @Override
    public List<ReportGroup> loadAll() {
        ReportGroupLoadContext context = new ReportGroupLoadContext(
                new ReportGroupFilter(),
                Sort.by(ReportGroupLoadContext.LOCALIZED_TITLE_SORT_KEY),
                0,
                0
        );
        return loadList(context);
    }

    @Override
    public List<ReportGroup> loadList(ReportGroupLoadContext loadContext) {
        if (!isReadPermitted()) {
            return Collections.emptyList();
        }
        Collection<ReportGroup> annotatedGroups = annotatedReportGroupHolder.getAllGroups();
        List<ReportGroup> dbGroups = loadGroupsFromDatabase();

        Stream<ReportGroup> stream = Stream.concat(
                annotatedGroups.stream(),
                dbGroups.stream()
        );

        List<ReportGroup> result = applyFilterSortPagination(stream, loadContext);
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
                .filter(rg -> satisfies(rg, filter))
                .count();

        return (int) count;
    }

    private List<ReportGroup> applyFilterSortPagination(Stream<ReportGroup> stream, ReportGroupLoadContext loadContext) {
        stream = stream.filter(rg -> satisfies(rg, loadContext.getFilter()));

        if (loadContext.getSort() != null && !loadContext.getSort().getOrders().isEmpty()) {
            Comparator<ReportGroup> comparator = repositoryUtil.comparatorBuilder(ReportGroup.class)
                    .customProperty(ReportGroupLoadContext.LOCALIZED_TITLE_SORT_KEY, rg -> {
                        return rg.getInstanceName(msgBundleTools);
                    })
                    .build(loadContext.getSort());
            stream = stream.sorted(comparator);
        }

        if (loadContext.getFirstResult() != 0) {
            stream = stream.skip(loadContext.getFirstResult());
        }

        if (loadContext.getMaxResults() != 0) {
            stream = stream.limit(loadContext.getMaxResults());
        }

        return stream.toList();
    }

    private boolean satisfies(ReportGroup group, ReportGroupFilter filter) {
        return repositoryUtil.containsIgnoreCase(group.getCode(), filter.getCodeContains())
               && repositoryUtil.containsIgnoreCase(group.getInstanceName(msgBundleTools), filter.getTitleContains());
    }

    protected List<ReportGroup> loadGroupsFromDatabase() {
        // here optimisations could be done if necessary: partially apply filter from load context
        return dataManager.load(ReportGroup.class)
                .all()
                .fetchPlan(FetchPlan.BASE).list();
    }

    protected boolean isReadPermitted() {
        CrudEntityContext showScreenContext = new CrudEntityContext(metadata.getClass(ReportGroup.class));
        accessManager.applyRegisteredConstraints(showScreenContext);

        return showScreenContext.isReadPermitted();
    }
}
