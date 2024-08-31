/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ComponentEvent;
import elemental.json.JsonObject;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;

public class AbstractPivotTableEvent extends ComponentEvent<JmixPivotTable> {

    protected JsonObject paramsJson;

    public AbstractPivotTableEvent(JmixPivotTable source, boolean fromClient, JsonObject paramsJson) {
        super(source, fromClient);

        this.paramsJson = paramsJson;
    }

    /**
     * Deserializes the incoming event JSON object into a POJO.
     *
     * @param paramsClass the class of the resulting POJO object
     * @param <P>         resulting POJO class type
     * @return POJO describing the event if the parse was successful
     * @throws IllegalArgumentException if parsing fails
     */
    protected <P> P convertDetail(Class<P> paramsClass) {
        P converted;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            converted = objectMapper.readValue(paramsJson.toJson(), paramsClass);
        } catch (JsonProcessingException e) {
            String message = String.format("Unparsable JsonObject for the '%s'", paramsClass.getSimpleName());
            throw new IllegalArgumentException(message, e);
        }

        return converted;
    }
}
