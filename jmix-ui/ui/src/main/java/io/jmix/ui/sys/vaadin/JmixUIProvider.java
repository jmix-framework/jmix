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

package io.jmix.ui.sys.vaadin;

import com.vaadin.annotations.Widgetset;
import com.vaadin.server.*;
import com.vaadin.spring.server.SpringUIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Enables property placeholders in {@link Widgetset} annotation.
 */
public class JmixUIProvider extends SpringUIProvider {

    private static final String APP_WIDGETSET_NAME = "AppWidgetset";

    public JmixUIProvider(VaadinSession vaadinSession) {
        super(vaadinSession);
    }

    @Nullable
    @Override
    public WidgetsetInfo getWidgetsetInfo(UICreateEvent event) {
        Widgetset uiWidgetset = getAnnotationFor(event.getUIClass(), Widgetset.class);

        // First case: We have an @Widgetset annotation, use that
        if (uiWidgetset != null) {
            String value = resolvePropertyPlaceholders(uiWidgetset.value());

            return new WidgetsetInfoImpl(value);
        }

        // Second case: We might have an init parameter, use that
        String initParameterWidgetSet = event.getService()
                .getDeploymentConfiguration().getWidgetset(null);
        if (initParameterWidgetSet != null) {
            return new WidgetsetInfoImpl(initParameterWidgetSet);
        }

        // Find the class AppWidgetset in the default package if one exists
        WidgetsetInfo info = getWidgetsetClassInfo();

        // Third case: we have a generated class called APP_WIDGETSET_NAME
        if (info != null) {
            return info;
        } else {
            // Fourth case: we have an AppWidgetset.gwt.xml file
            try (InputStream resource = event.getUIClass().getResourceAsStream("/" + APP_WIDGETSET_NAME + ".gwt.xml")){
                if (resource != null) {
                    return new WidgetsetInfoImpl(false, null, APP_WIDGETSET_NAME);
                }
            } catch (IOException e) {
                throw new RuntimeException("AppWidgetset.gwt.xml file I/O exception", e);
            }
        }

        // fifth case: we are using the default widgetset
        return null;
    }

    private String resolvePropertyPlaceholders(String value) {
        if (StringUtils.hasText(value)) {
            return getWebApplicationContext().getEnvironment()
                    .resolvePlaceholders(value);
        }
        return value;
    }

    protected static class WidgetsetInfoImpl implements WidgetsetInfo {
        private final boolean cdn;
        private final String widgetsetUrl;
        private final String widgetsetName;

        public WidgetsetInfoImpl(boolean cdn, @Nullable String widgetsetUrl,
                                 String widgetsetName) {

            this.cdn = cdn;
            this.widgetsetUrl = widgetsetUrl;
            this.widgetsetName = widgetsetName;
        }

        public WidgetsetInfoImpl(String widgetsetName) {
            this(false, null, widgetsetName);
        }

        @Override
        public boolean isCdn() {
            return cdn;
        }

        @Nullable
        @Override
        public String getWidgetsetUrl() {
            return widgetsetUrl;
        }

        @Override
        public String getWidgetsetName() {
            return widgetsetName;
        }
    }

    @Nullable
    private WidgetsetInfo getWidgetsetClassInfo() {
        Class<WidgetsetInfo> cls = findWidgetsetClass();
        if (cls != null) {
            try {
                return cls.newInstance();
            } catch (InstantiationException e) {
                getLogger().info("Unexpected trying to instantiate class {}",
                        cls.getName(), e);
            } catch (IllegalAccessException e) {
                getLogger().info("Unexpected trying to access class {}",
                        cls.getName(), e);
            }
        }
        return null;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(UIProvider.class);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Class<WidgetsetInfo> findWidgetsetClass() {
        try {
            // We cannot naively use Class.forName without getting the correct
            // classloader
            ClassLoader tccl = VaadinService.getCurrent().getClassLoader();
            Class<?> c = Class.forName(APP_WIDGETSET_NAME, true, tccl);

            // if not implementing the interface, possibly a @WebListener class
            // from an earlier version - ignore it
            if (WidgetsetInfo.class.isAssignableFrom(c)) {
                return (Class<WidgetsetInfo>) c;
            }
        } catch (ClassNotFoundException e) {
            // ClassNotFound is a normal case
        }
        return null;
    }
}