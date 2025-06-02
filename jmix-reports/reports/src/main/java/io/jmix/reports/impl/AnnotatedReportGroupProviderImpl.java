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

import io.jmix.reports.annotation.ReportGroupDef;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.impl.builder.AnnotatedGroupBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("reports_AnnotatedReportGroupProvider")
public class AnnotatedReportGroupProviderImpl implements AnnotatedReportGroupProvider, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedReportGroupProviderImpl.class);

    protected final AnnotatedGroupBuilder annotatedGroupBuilder;
    protected ApplicationContext applicationContext;

    /**
     * Map: group code -> group model object.
     */
    protected Map<String, ReportGroup> groupsByCode;

    public AnnotatedReportGroupProviderImpl(AnnotatedGroupBuilder annotatedGroupBuilder) {
        this.annotatedGroupBuilder = annotatedGroupBuilder;
        this.groupsByCode = new HashMap<>();
    }

    @Override
    public Collection<ReportGroup> getAllGroups() {
        return groupsByCode.values();
    }

    @Override
    public ReportGroup getGroupByCode(String code) {
        return groupsByCode.get(code);
    }

    protected void importGroupDefinition(Object bean, String beanName) {
        ReportGroup group = annotatedGroupBuilder.createGroupFromDefinition(bean);
        if (groupsByCode.containsKey(group.getCode())) {
            throw new IllegalStateException(
                    String.format("Duplicate group code: %s, bean name: %s", group.getCode(), beanName)
            );
        }
        groupsByCode.put(group.getCode(), group);
        log.debug("Imported group definition: name {}, {}", beanName, bean.getClass());
    }

    @Override
    public void importGroupDefinitions() {
        Map<String, Object> groupDefinitions = applicationContext.getBeansWithAnnotation(ReportGroupDef.class);
        for (Map.Entry<String, Object> entry : groupDefinitions.entrySet()) {
            importGroupDefinition(entry.getValue(), entry.getKey());
        }
        log.info("Imported {} group definitions", groupDefinitions.size());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
