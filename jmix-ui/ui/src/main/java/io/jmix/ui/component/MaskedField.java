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
import java.util.UUID;

/**
 * Masked field component generic interface.
 * FieldConfig supports following format symbols:
 * <ul>
 * <li># - Digit</li>
 * <li>U - Uppercase letter</li>
 * <li>L - Lowercase letter</li>
 * <li>A - Letter or digit</li>
 * <li>* - Any symbol</li>
 * <li>H - Hex symbol</li>
 * <li>~ - + or -</li>
 * </ul>
 * Any other symbols in format will be treated as mask literals.
 */
@StudioComponent(
        caption = "MaskedField",
        category = "Components",
        xmlElement = "maskedField",
        icon = "io/jmix/ui/icon/component/maskedField.svg",
        canvasBehaviour = CanvasBehaviour.INPUT_FIELD,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/masked-field.html"
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
public interface MaskedField<V>
        extends
            TextInputField<V>,
            HasDatatype<V>,
            TextInputField.TextSelectionSupported,
            TextInputField.CursorPositionSupported,
            TextInputField.EnterPressNotifier {

    String NAME = "maskedField";

    ParameterizedTypeReference<MaskedField<String>> TYPE_DEFAULT =
            new ParameterizedTypeReference<MaskedField<String>>() {};
    ParameterizedTypeReference<MaskedField<String>> TYPE_STRING =
            new ParameterizedTypeReference<MaskedField<String>>() {};

    ParameterizedTypeReference<MaskedField<Integer>> TYPE_INTEGER =
            new ParameterizedTypeReference<MaskedField<Integer>>() {};
    ParameterizedTypeReference<MaskedField<Long>> TYPE_LONG =
            new ParameterizedTypeReference<MaskedField<Long>>() {};
    ParameterizedTypeReference<MaskedField<Double>> TYPE_DOUBLE =
            new ParameterizedTypeReference<MaskedField<Double>>() {};
    ParameterizedTypeReference<MaskedField<BigDecimal>> TYPE_BIGDECIMAL =
            new ParameterizedTypeReference<MaskedField<BigDecimal>>() {};

    ParameterizedTypeReference<MaskedField<UUID>> TYPE_UUID =
            new ParameterizedTypeReference<MaskedField<UUID>>() {};

    @StudioProperty(required = true)
    void setMask(String mask);
    String getMask();

    /**
     * Sets ValueMode for component
     * <p>
     * MASKED - value contain mask literals
     * CLEAR - value contain only user input.
     * </p>
     *
     * @param mode value mode
     */
    @StudioProperty(name = "valueMode", type = PropertyType.ENUMERATION, defaultValue = "clear",
            options = {"clear", "masked"})
    void setValueMode(ValueMode mode);

    ValueMode getValueMode();

    boolean isSendNullRepresentation();
    void setSendNullRepresentation(boolean sendNullRepresentation);

    /**
     * Returns a string representation of the value.
     */
    String getRawValue();

    enum ValueMode {
        MASKED,
        CLEAR
    }
}