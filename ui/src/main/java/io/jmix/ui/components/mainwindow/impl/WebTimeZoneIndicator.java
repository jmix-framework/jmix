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

package io.jmix.ui.components.mainwindow.impl;

import com.vaadin.ui.Label;
import io.jmix.core.BeanLocator;
import io.jmix.core.security.UserSessionSource;
import io.jmix.ui.components.impl.WebAbstractComponent;
import io.jmix.ui.components.mainwindow.TimeZoneIndicator;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.TimeZone;

public class WebTimeZoneIndicator extends WebAbstractComponent<Label> implements TimeZoneIndicator {

    protected static final String USER_TIMEZONE_LABEL_STYLENAME = "c-user-timezone-label";

    public WebTimeZoneIndicator() {
        component = new Label();
        component.setSizeUndefined();
        component.setStyleName(USER_TIMEZONE_LABEL_STYLENAME);
    }

    @Inject
    public void setBeanLocator(BeanLocator beanLocator) {
        super.setBeanLocator(beanLocator);

        UserSessionSource uss = beanLocator.get(UserSessionSource.NAME);
        TimeZone timeZone = uss.getUserSession().getTimeZone();
        // todo TimeZones
//        TimeZones timeZones = beanLocator.get(TimeZones.NAME);
//        component.setValue(timeZones.getDisplayNameShort(timeZone));
        if (timeZone == null) {
            // hidden by default if timeZone is null
            setVisible(false);
        }
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(USER_TIMEZONE_LABEL_STYLENAME, ""));
    }

    @Override
    public void setStyleName(String styleName) {
        super.setStyleName(styleName);

        component.addStyleName(USER_TIMEZONE_LABEL_STYLENAME);
    }
}
