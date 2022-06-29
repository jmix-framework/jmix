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

package test_support.bean;

import io.jmix.ui.component.impl.AppWorkAreaImpl;
import io.jmix.ui.event.screen.ScreenClosedEvent;
import io.jmix.ui.event.screen.ScreenOpenedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component(TestWebBean.NAME)
public class TestWebBean {

    public static final String NAME = "test_WebBean";

    public static final ThreadLocal<Boolean> testMethodInvoked = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> paramsExist = new ThreadLocal<>();

    public static final ThreadLocal<Boolean> screenOpenedEventHandled = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> screenClosedEventHandled = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> workAreaTabChangedEventHandled = new ThreadLocal<>();

    public void testMethod() {
        testMethodInvoked.set(true);
    }

    public void testMethodWithParams(Map<String, Object> params) {
        paramsExist.set(!params.isEmpty());
    }

    @EventListener
    public void onScreenOpened(ScreenOpenedEvent evt) {
        screenOpenedEventHandled.set(true);
    }

    @EventListener
    public void onScreenClosed(ScreenClosedEvent evt) {
        screenClosedEventHandled.set(true);
    }

    @EventListener
    public void onWorkAreaTabChangedEvent(AppWorkAreaImpl.WorkAreaTabChangedEvent evt) {
        workAreaTabChangedEventHandled.set(true);
    }
}
