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

package io.jmix.reportsflowui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.Messages;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.reports.entity.ReportOutputType;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@org.springframework.stereotype.Component("report_OutputTypeHelper")
public class OutputTypeHelper {

    @Autowired
    protected Messages messages;
    @Autowired
    protected Icons icons;

    /**
     * @return list of currently supported output types that user should see in UI
     */
    public List<ReportOutputType> getSupportedOutputTypes() {
        ArrayList<ReportOutputType> outputTypes = new ArrayList<>(Arrays.asList(ReportOutputType.values()));

        // Unsupported types for now
        outputTypes.remove(ReportOutputType.CHART);
        outputTypes.remove(ReportOutputType.PIVOT_TABLE);

        return outputTypes;
    }

    /**
     * Creates a small visual badge combining the output-type icon with the localised type name.
     * Intended for use in grid column renderers.
     * Returns an empty layout when {@code outputType} is {@code null}.
     */
    public HorizontalLayout createOutputTypeBadge(@Nullable ReportOutputType outputType) {
        if (outputType == null) {
            return new HorizontalLayout();
        }

        Component icon = icons.get(getIcon(outputType));
        Span text = new Span(messages.getMessage(outputType));

        HorizontalLayout layout = new HorizontalLayout(icon, text);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(false);
        layout.getThemeList().add("spacing-xs");

        return layout;
    }

    /**
     * @return the {@link JmixFontIcon} that best represents the given output type;
     * falls back to {@link JmixFontIcon#FILE_O} for {@code null} or unrecognised types
     */
    public JmixFontIcon getIcon(@Nullable ReportOutputType outputType) {
        if (outputType == null) {
            return JmixFontIcon.FILE_O;
        }

        return switch (outputType) {
            case CSV, XLS, XLSX -> JmixFontIcon.FILE_TABLE;
            case PDF, DOC, DOCX -> JmixFontIcon.FILE_TEXT_O;
            case HTML -> JmixFontIcon.FILE_CODE;
            case TABLE -> JmixFontIcon.TABLE;
            default -> JmixFontIcon.FILE_O;
        };
    }
}
