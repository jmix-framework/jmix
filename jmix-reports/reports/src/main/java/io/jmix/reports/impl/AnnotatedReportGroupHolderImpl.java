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

package io.jmix.reports.impl;

import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.impl.builder.AnnotatedGroupBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("reports_AnnotatedReportGroupProvider")
public class AnnotatedReportGroupHolderImpl implements AnnotatedReportGroupHolder {

    protected final AnnotatedGroupBuilder annotatedGroupBuilder;
    protected ApplicationContext applicationContext;

    /**
     * Map: group code -> group model object.
     */
    protected Map<String, ReportGroup> groupsByCode;

    public AnnotatedReportGroupHolderImpl(AnnotatedGroupBuilder annotatedGroupBuilder) {
        this.annotatedGroupBuilder = annotatedGroupBuilder;
        this.groupsByCode = new ConcurrentHashMap<>();
    }

    @Override
    public Collection<ReportGroup> getAllGroups() {
        return groupsByCode.values();
    }

    @Override
    public ReportGroup getGroupByCode(String code) {
        return groupsByCode.get(code);
    }

    @Override
    public void put(ReportGroup group) {
        if (groupsByCode.containsKey(group.getCode())) {
            throw new IllegalStateException(
                    String.format("Duplicate group code: %s", group.getCode())
            );
        }
        groupsByCode.put(group.getCode(), group);
    }

    @Override
    public void clear() {
        groupsByCode.clear();
    }
}
