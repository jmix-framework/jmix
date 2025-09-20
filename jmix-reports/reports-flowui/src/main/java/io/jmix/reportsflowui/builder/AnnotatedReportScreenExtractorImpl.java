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

package io.jmix.reportsflowui.builder;

import io.jmix.core.Metadata;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.reports.annotation.AvailableInViews;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportScreen;
import io.jmix.reports.impl.builder.AnnotatedReportScreenExtractor;
import io.jmix.reports.impl.builder.InvalidReportDefinitionException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("report_AnnotatedReportScreenExtractor")
public class AnnotatedReportScreenExtractorImpl implements AnnotatedReportScreenExtractor {

    protected final Metadata metadata;
    protected final ViewRegistry viewRegistry;

    public AnnotatedReportScreenExtractorImpl(Metadata metadata, ViewRegistry viewRegistry) {
        this.metadata = metadata;
        this.viewRegistry = viewRegistry;
    }

    @Override
    public List<ReportScreen> extractScreens(Object definitionInstance, Report report) {
        AvailableInViews annotation = definitionInstance.getClass().getAnnotation(AvailableInViews.class);
        if (annotation == null) {
            return Collections.emptyList();
        }

        List<ReportScreen> screens = new ArrayList<>();

        for (String viewId : annotation.viewIds()) {
            validateViewIsRegistered(viewId, annotation);

            ReportScreen reportScreen = convertToReportScreen(report, viewId);
            screens.add(reportScreen);
        }

        for (Class<?> viewClass : annotation.viewClasses()) {
            String viewId;
            try {
                viewId = ViewDescriptorUtils.getInferredViewId(viewClass);
            } catch (IllegalArgumentException ex) {
                throw new InvalidReportDefinitionException(
                        String.format("Invalid value for viewClasses: '%s'. Annotation: %s", viewClass.getName(), annotation),
                        ex
                );
            }

            validateViewIsRegistered(viewId, annotation);

            ReportScreen reportScreen = convertToReportScreen(report, viewId);
            screens.add(reportScreen);
        }

        return Collections.unmodifiableList(screens);
    }

    protected void validateViewIsRegistered(String viewId, AvailableInViews annotation) {
        if (!viewRegistry.hasView(viewId)) {
            throw new InvalidReportDefinitionException(
                    String.format("View with id='%s' is not registered: %s", viewId, annotation)
            );
        }
    }

    protected ReportScreen convertToReportScreen(Report report, String viewId) {
        ReportScreen reportScreen = metadata.create(ReportScreen.class);
        reportScreen.setReport(report);
        reportScreen.setScreenId(viewId);
        return reportScreen;
    }
}
