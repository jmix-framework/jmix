/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.app.propertyfilter.dateinterval.model.predefined;

import io.jmix.core.Messages;
import io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for predefined date intervals.
 */
public abstract class PredefinedDateInterval implements BaseDateInterval {

    protected Messages messages;

    protected String name;

    public PredefinedDateInterval(String name) {
        this.name = name;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public BaseDateInterval.Type getType() {
        return BaseDateInterval.Type.PREDEFINED;
    }

    /**
     * @return name of date interval
     */
    public String getName() {
        return name;
    }

    /**
     * @return localized caption of date interval that can be used in UI
     */
    public String getLocalizedCaption() {
        return messages.getMessage(this.getClass(), name);
    }
}
