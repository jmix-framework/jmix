/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.ui.facet;

import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Timer;
import io.jmix.ui.component.impl.TimerImpl;
import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component("ui_TimerFacetProvider")
public class TimerFacetProvider implements FacetProvider<Timer> {

    @Override
    public Class<Timer> getFacetClass() {
        return Timer.class;
    }

    @Override
    public Timer create() {
        return new TimerImpl();
    }

    @Override
    public String getFacetTag() {
        return "timer";
    }

    @Override
    public void loadFromXml(Timer facet, Element element, ComponentLoader.ComponentContext context) {
        loadTimer(facet, element, context);
    }

    protected void loadTimer(Timer timer, Element element, ComponentLoader.ComponentContext context) {
        String id = element.attributeValue("id");
        if (isNotEmpty(id)) {
            timer.setId(id);
        }

        String delay = element.attributeValue("delay");
        if (StringUtils.isEmpty(delay)) {
            throw new GuiDevelopmentException("Timer 'delay' can't be empty", context,
                    "Timer ID", timer.getId());
        }

        int value = parseInt(delay);
        if (value <= 0) {
            throw new GuiDevelopmentException("Timer 'delay' must be greater than 0",
                    context, "Timer ID", timer.getId());
        }

        timer.setDelay(value);
        timer.setRepeating(parseBoolean(element.attributeValue("repeating")));

        String autostart = element.attributeValue("autostart");
        if (isNotEmpty(autostart)
                && parseBoolean(autostart)) {
            timer.start();
        }
    }
}