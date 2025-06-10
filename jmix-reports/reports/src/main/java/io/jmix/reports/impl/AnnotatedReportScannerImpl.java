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

import io.jmix.core.ClassManager;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.ReportGroupDef;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.impl.builder.AnnotatedGroupBuilder;
import io.jmix.reports.impl.builder.AnnotatedReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("reports_AnnotatedReportScanner")
public class AnnotatedReportScannerImpl implements AnnotatedReportScanner, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(AnnotatedReportScannerImpl.class);

    protected ApplicationContext applicationContext;
    protected final AnnotatedReportBuilder reportBuilder;
    protected final AnnotatedReportHolder reportHolder;
    protected final AnnotatedGroupBuilder groupBuilder;
    protected final AnnotatedReportGroupHolder reportGroupHolder;
    protected final ClassManager classManager;

    public AnnotatedReportScannerImpl(AnnotatedReportBuilder reportBuilder, AnnotatedReportHolder reportHolder,
                                      AnnotatedGroupBuilder groupBuilder, AnnotatedReportGroupHolder reportGroupHolder,
                                      ClassManager classManager) {
        this.reportBuilder = reportBuilder;
        this.reportHolder = reportHolder;
        this.groupBuilder = groupBuilder;
        this.reportGroupHolder = reportGroupHolder;
        this.classManager = classManager;
    }

    @Override
    public void importGroupDefinitions() {
        Map<String, Object> groupDefinitions = applicationContext.getBeansWithAnnotation(ReportGroupDef.class);
        for (Map.Entry<String, Object> entry : groupDefinitions.entrySet()) {
            importGroupDefinition(entry.getValue(), entry.getKey());
        }
        log.info("Imported {} group definitions", groupDefinitions.size());
    }

    protected void importGroupDefinition(Object bean, String beanName) {
        ReportGroup group = groupBuilder.createGroupFromDefinition(bean);
        if (reportGroupHolder.getGroupByCode(group.getCode()) != null) {
            throw new IllegalStateException(
                    String.format("Duplicate group code: %s, bean name: %s", group.getCode(), beanName)
            );
        }
        reportGroupHolder.put(group);
        log.debug("Imported group definition: name {}, {}", beanName, bean.getClass());
    }

    @Override
    public void importReportDefinitions() {
        Map<String, Object> reportDefinitions = applicationContext.getBeansWithAnnotation(ReportDef.class);
        for (Map.Entry<String, Object> entry : reportDefinitions.entrySet()) {
            importReportDefinition(entry.getValue(), entry.getKey());
        }
        log.info("Imported {} report definitions", reportDefinitions.size());
    }

    protected void importReportDefinition(Object bean, String beanName) {
        Report report = reportBuilder.createReportFromDefinition(bean);

        if (reportHolder.getByCode(report.getCode()) != null) {
            throw new IllegalStateException(
                    String.format("Duplicate report code: %s, bean name: %s", report.getCode(), beanName)
            );
        }
        reportHolder.put(report);
        log.debug("Imported report definition: name {}, {}", beanName, bean.getClass());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void loadReportGroupClass(String className) {
        Class<?> reportGroupClass = classManager.loadClass(className);
        if (!reportGroupClass.isAnnotationPresent(ReportGroupDef.class)) {
            log.error("Group class not annotated with @{}: {}", ReportGroupDef.class.getSimpleName(), className);
            return;
        }

        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        Object definitionInstance = beanFactory.createBean(reportGroupClass);

        // test that definition is correct,
        //   to fail-fast without loosing cache if it's invalid
        groupBuilder.createGroupFromDefinition(definitionInstance);

        String beanName = getBeanName(beanFactory, reportGroupClass);
        List<String> beanNamesToDelete = getBeanNamesToDelete(beanName, className, ReportGroupDef.class);
        replaceBeanInContext(beanFactory, definitionInstance, beanName, beanNamesToDelete);

        log.info("Rescanning annotated groups and reports");
        reportHolder.clear();
        reportGroupHolder.clear();

        importGroupDefinitions();
        importReportDefinitions();
    }

    @Override
    public void loadReportClass(String className) {
        Class<?> reportClass = classManager.loadClass(className);
        if (!reportClass.isAnnotationPresent(ReportDef.class)) {
            log.error("Report class not annotated with @{}: {}", ReportDef.class.getSimpleName(), className);
            return;
        }

        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        Object definitionInstance = beanFactory.createBean(reportClass);

        // test that definition is correct,
        //   to fail-fast without loosing cache if it's invalid
        reportBuilder.createReportFromDefinition(definitionInstance);

        String beanName = getBeanName(beanFactory, reportClass);
        List<String> beanNamesToDelete = getBeanNamesToDelete(beanName, className, ReportDef.class);
        replaceBeanInContext(beanFactory, definitionInstance, beanName, beanNamesToDelete);

        log.info("Rescanning annotated reports");
        reportHolder.clear();
        importReportDefinitions();
    }

    protected void replaceBeanInContext(AutowireCapableBeanFactory beanFactory, Object definitionInstance,
                                        String beanName, List<String> beanNamesToDelete) {
        DefaultSingletonBeanRegistry registry = (DefaultSingletonBeanRegistry) beanFactory;

        for (String beanToDelete : beanNamesToDelete) {
            registry.destroySingleton(beanToDelete);

            if (!beanName.equals(beanToDelete)) {
                ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(beanToDelete);
            }
        }
        registry.registerSingleton(beanName, definitionInstance);

        log.info("Bean {} replaced in context with: {}, {}", beanNamesToDelete, beanName, definitionInstance.getClass());
    }

    /*
     * Handle also case when bean name is changed with hot deploy (but class name remains the same).
     */
    protected List<String> getBeanNamesToDelete(String beanName, String className, Class<? extends Annotation> annotationMarkerClass) {
        List<String> beanNamesToDelete = new ArrayList<>();
        if (applicationContext.containsBean(beanName)) {
            beanNamesToDelete.add(beanName);
        }
        String anotherBeanName = findExistingBeanNameByClassName(className, annotationMarkerClass);
        if (anotherBeanName != null && !anotherBeanName.equals(beanName)) {
            beanNamesToDelete.add(anotherBeanName);
        }
        return beanNamesToDelete;
    }

    @Nullable
    protected String findExistingBeanNameByClassName(String className, Class<? extends Annotation> annotationMarkerClass) {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(annotationMarkerClass);

        for (String beanName : beanNames) {
            Class<?> beanClass = applicationContext.getType(beanName);
            if (beanClass != null && className.equals(beanClass.getName())) {
                return beanName;
            }
        }
        return null;
    }

    protected String getBeanName(AutowireCapableBeanFactory beanFactory, Class<?> reportGroupClass) {
        AnnotationBeanNameGenerator beanNameGenerator = getBeanNameGenerator();

        BeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(reportGroupClass);

        String beanName = beanNameGenerator.generateBeanName(beanDefinition, (BeanDefinitionRegistry) beanFactory);
        return beanName;
    }

    protected AnnotationBeanNameGenerator getBeanNameGenerator() {
        return AnnotationBeanNameGenerator.INSTANCE;
    }
}
