/*
 * Copyright (c) 2023 Haulmont.
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

package io.jmix.flowui.xml.facet;

import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.facet.impl.TimerImpl;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

@Component("flowui_TimerFacetProvider")
public class TimerFacetProvider implements FacetProvider<Timer> {

    protected LoaderSupport loaderSupport;

    public TimerFacetProvider(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

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
        return Timer.NAME;
    }

    @Override
    public void loadFromXml(Timer timer, Element element, ComponentLoader.ComponentContext context) {
        loadTimer(timer, element, context);
    }

    protected void loadTimer(Timer timer, Element element, ComponentLoader.ComponentContext context) {
        String id = loaderSupport.loadString(element, "id")
                .orElseThrow(() -> new IllegalStateException("Timer id must be defined"));
        timer.setId(id);

        int delay = loaderSupport.loadInteger(element, "delay").orElse(-1);
        if (delay <= 0) {
            throw new GuiDevelopmentException("Timer 'delay' must be greater than 0", context, "Timer ID", id);
        }
        timer.setDelay(delay);

        loaderSupport.loadBoolean(element, "repeating", timer::setRepeating);

        boolean autostart = loaderSupport.loadBoolean(element, "autostart").orElse(false);
        if (autostart) {
            timer.start();
        }
    }
}