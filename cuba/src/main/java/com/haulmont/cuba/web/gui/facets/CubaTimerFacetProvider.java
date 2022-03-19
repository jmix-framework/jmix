/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.web.gui.facets;

import com.haulmont.cuba.gui.components.Timer;
import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import com.haulmont.cuba.web.gui.components.WebTimer;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.facet.TimerFacetProvider;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.xml.layout.ComponentLoader.ComponentContext;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component("cuba_TimerFacetProvider")
public class CubaTimerFacetProvider extends TimerFacetProvider {

    @Override
    public Class getFacetClass() {
        return Timer.class;
    }

    @Override
    public Timer create() {
        return new WebTimer();
    }

    @Override
    protected void loadTimer(io.jmix.ui.component.Timer timer, Element element, ComponentContext context) {
        super.loadTimer(timer, element, context);

        // use @Subscribe event handlers instead
        String onTimer = element.attributeValue("onTimer");
        if (isNotEmpty(onTimer)) {
            String timerMethodName = onTimer;
            if (StringUtils.startsWith(onTimer, "invoke:")) {
                timerMethodName = StringUtils.substring(onTimer, "invoke:".length());
            }
            timerMethodName = StringUtils.trim(timerMethodName);

            addInitTimerMethodTask((Timer) timer, timerMethodName, context);
        }
    }

    @Deprecated
    protected void addInitTimerMethodTask(Timer timer, String timerMethodName, ComponentContext context) {
        FrameOwner controller = context.getFrame().getFrameOwner();
        if (controller instanceof LegacyFragmentAdapter) {
            controller = ((LegacyFragmentAdapter) controller).getRealScreen();
        }

        Class<? extends FrameOwner> windowClass = controller.getClass();

        Method timerMethod;
        try {
            timerMethod = windowClass.getMethod(timerMethodName, Timer.class);
        } catch (NoSuchMethodException e) {
            throw new GuiDevelopmentException("Unable to find invoke method for timer", context,
                    ParamsMap.of(
                            "Timer Id", timer.getId(),
                            "Method name", timerMethodName));
        }

        timer.addTimerActionListener(new DeclarativeTimerActionHandler(timerMethod, controller));
    }

    @Deprecated
    protected static class DeclarativeTimerActionHandler implements Consumer<Timer.TimerActionEvent> {
        protected final Method timerMethod;
        protected final FrameOwner controller;

        public DeclarativeTimerActionHandler(Method timerMethod, FrameOwner controller) {
            this.timerMethod = timerMethod;
            this.controller = controller;
        }

        @Override
        public void accept(Timer.TimerActionEvent e) {
            try {
                timerMethod.invoke(controller, e.getSource());
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException("Unable to invoke onTimer", ex);
            }
        }
    }
}
