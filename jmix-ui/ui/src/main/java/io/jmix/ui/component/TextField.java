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
package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;

/**
 * A component for editing textual data that fits on a single line. For a multi-line textarea, see the
 * {@link TextArea} component.
 *
 * @param <V> type of value
 */
@StudioComponent(
        caption = "TextField",
        category = "Components",
        xmlElement = "textField",
        icon = "io/jmix/ui/icon/component/textField.svg",
        canvasBehaviour = CanvasBehaviour.INPUT_FIELD,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/text-field.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "V"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface TextField<V>
        extends
            TextInputField<V>,
            HasDatatype<V>,
            HasFormatter<V>,
            TextInputField.MaxLengthLimited,
            TextInputField.TrimSupported,
            TextInputField.TextSelectionSupported,
            TextInputField.TextChangeNotifier,
            TextInputField.EnterPressNotifier,
            TextInputField.CursorPositionSupported,
            TextInputField.CaseConversionSupported,
            HasInputPrompt,
            HasConversionErrorMessage,
            TextInputField.HtmlNameSupported {

    String NAME = "textField";

    ParameterizedTypeReference<TextField<String>> TYPE_DEFAULT =
            new ParameterizedTypeReference<TextField<String>>() {};
    ParameterizedTypeReference<TextField<String>> TYPE_STRING =
            new ParameterizedTypeReference<TextField<String>>() {};

    ParameterizedTypeReference<TextField<Integer>> TYPE_INTEGER =
            new ParameterizedTypeReference<TextField<Integer>>() {};
    ParameterizedTypeReference<TextField<Long>> TYPE_LONG =
            new ParameterizedTypeReference<TextField<Long>>() {};
    ParameterizedTypeReference<TextField<Double>> TYPE_DOUBLE =
            new ParameterizedTypeReference<TextField<Double>>() {};
    ParameterizedTypeReference<TextField<BigDecimal>> TYPE_BIGDECIMAL =
            new ParameterizedTypeReference<TextField<BigDecimal>>() {};

    ParameterizedTypeReference<TextField<Date>> TYPE_DATE =
            new ParameterizedTypeReference<TextField<Date>>() {};
    ParameterizedTypeReference<TextField<java.util.Date>> TYPE_DATETIME =
            new ParameterizedTypeReference<TextField<java.util.Date>>() {};
    ParameterizedTypeReference<TextField<LocalDate>> TYPE_LOCALDATE =
            new ParameterizedTypeReference<TextField<LocalDate>>() {};
    ParameterizedTypeReference<TextField<LocalDateTime>> TYPE_LOCALDATETIME =
            new ParameterizedTypeReference<TextField<LocalDateTime>>() {};
    ParameterizedTypeReference<TextField<Time>> TYPE_TIME =
            new ParameterizedTypeReference<TextField<Time>>() {};
    ParameterizedTypeReference<TextField<OffsetTime>> TYPE_OFFSETTIME =
            new ParameterizedTypeReference<TextField<OffsetTime>>() {};

    /**
     * Returns a string representation of the value.
     */
    String getRawValue();
}