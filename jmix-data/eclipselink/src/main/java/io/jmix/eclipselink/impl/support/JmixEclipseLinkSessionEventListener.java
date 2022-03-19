/*
 * Copyright 2019 Haulmont.
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

package io.jmix.eclipselink.impl.support;

import com.google.common.base.Strings;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.eclipselink.impl.DescriptorEventManagerWrapper;
import io.jmix.eclipselink.persistence.*;
import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.persistence.annotations.CacheCoordinationType;
import org.eclipse.persistence.config.CacheIsolationType;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.InheritancePolicy;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component("eclipselink_JmixEclipseLinkSessionEventListener")
public class JmixEclipseLinkSessionEventListener extends SessionEventAdapter {

    @Autowired
    private Environment environment;
    @Autowired
    private Metadata metadata;
    @Autowired
    private ListableBeanFactory beanFactory;

    private static final Logger log = LoggerFactory.getLogger(JmixEclipseLinkSessionEventListener.class);

    @Override
    public void preLogin(SessionEvent event) {
        Session session = event.getSession();

        setPrintInnerJoinOnClause(session);
        boolean hasMultipleTableConstraintDependency = hasMultipleTableConstraintDependency();

        //noinspection rawtypes
        for (Map.Entry<Class, ClassDescriptor> entry : session.getDescriptors().entrySet()) {
            MetaClass metaClass = metadata.getSession().getClass(entry.getKey());
            ClassDescriptor descriptor = entry.getValue();

            setCacheable(metaClass, descriptor, session);

            if (hasMultipleTableConstraintDependency) {
                setMultipleTableConstraintDependency(descriptor);
            }

            if (Entity.class.isAssignableFrom(descriptor.getJavaClass())) {
                // set DescriptorEventManager that doesn't invoke listeners for base classes
                descriptor.setEventManager(new DescriptorEventManagerWrapper(descriptor.getDescriptorEventManager()));
                descriptor.getEventManager().addListener(beanFactory.getBean(JmixEclipseLinkDescriptorEventListener.class));
            }

            setAdditionalCriteria(descriptor);

            executeDescriptorProcessors(descriptor, session);

            executeMappingProcessors(descriptor, session);
        }
    }

    protected void setCacheable(MetaClass metaClass, ClassDescriptor desc, Session session) {
        String property = (String) session.getProperty("eclipselink.cache.shared.default");
        boolean defaultCache = property == null || Boolean.parseBoolean(property);

        if ((defaultCache && !desc.isIsolated())
                || desc.getCacheIsolation() == CacheIsolationType.SHARED
                || desc.getCacheIsolation() == CacheIsolationType.PROTECTED) {
            metaClass.getAnnotations().put("cacheable", true);
            desc.getCachePolicy().setCacheCoordinationType(CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS);
        }
    }

    protected void setMultipleTableConstraintDependency(ClassDescriptor desc) {
        InheritancePolicy policy = desc.getInheritancePolicyOrNull();
        if (policy != null && policy.isJoinedStrategy() && policy.getParentClass() != null) {
            desc.setHasMultipleTableConstraintDependecy(true);
        }
    }

    protected boolean hasMultipleTableConstraintDependency() {
        String value = environment.getProperty("jmix.data.has-multiple-table-constraint-dependency");
        return value == null || BooleanUtils.toBoolean(value);
    }

    protected void setPrintInnerJoinOnClause(Session session) {
        boolean useInnerJoinOnClause = BooleanUtils.toBoolean(
                environment.getProperty("jmix.data.use-inner-join-on-clause"));
        session.getPlatform().setPrintInnerJoinInWhereClause(!useInnerJoinOnClause);
    }

    protected void setAdditionalCriteria(ClassDescriptor descriptor) {
        String criteria = beanFactory.getBeansOfType(AdditionalCriteriaProvider.class)
                .values().stream()
                .filter(provider -> provider.requiresAdditionalCriteria(descriptor.getJavaClass()))
                .map(provider -> provider.getAdditionalCriteria(descriptor.getJavaClass()))
                .collect(Collectors.joining(" and "));

        if (!Strings.isNullOrEmpty(criteria)) {
            descriptor.getQueryManager().setAdditionalCriteria(criteria);
        }
    }

    protected void executeDescriptorProcessors(ClassDescriptor descriptor, Session session) {
        Map<String, DescriptorProcessor> descriptorProcessors = beanFactory.getBeansOfType(DescriptorProcessor.class);
        DescriptorProcessorContext descriptorContext = new DescriptorProcessorContext(descriptor, session);
        for (DescriptorProcessor dp : descriptorProcessors.values()) {
            log.trace("{} descriptor processor is started", dp.getClass());
            dp.process(descriptorContext);
            log.trace("{} descriptor processor is finished", dp.getClass());
        }
    }

    protected void executeMappingProcessors(ClassDescriptor descriptor, Session session) {
        Map<String, MappingProcessor> mappingProcessors = beanFactory.getBeansOfType(MappingProcessor.class);
        for (DatabaseMapping mapping : descriptor.getMappings()) {
            MappingProcessorContext mappingContext = new MappingProcessorContext(mapping, session);
            for (MappingProcessor mp : mappingProcessors.values()) {
                log.trace("{} mapping processor is started", mp.getClass());
                mp.process(mappingContext);
                log.trace("{} mapping processor is finished", mp.getClass());
            }
        }
    }
}
