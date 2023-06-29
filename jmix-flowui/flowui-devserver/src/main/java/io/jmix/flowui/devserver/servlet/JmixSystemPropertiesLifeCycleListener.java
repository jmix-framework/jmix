/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.devserver.servlet;

import com.vaadin.flow.server.InitParameters;
import io.jmix.flowui.devserver.frontend.FrontendUtils;
import org.eclipse.jetty.util.component.LifeCycle;

import java.util.Properties;

import static com.vaadin.flow.server.Constants.PROJECT_FRONTEND_GENERATED_DIR_TOKEN;
import static com.vaadin.flow.server.Constants.VAADIN_PREFIX;
import static com.vaadin.flow.server.InitParameters.FRONTEND_HOTDEPLOY;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_ENABLE_PNPM;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.*;

public class JmixSystemPropertiesLifeCycleListener implements LifeCycle.Listener {

    private final String projectBaseDir;
    private final String isPnpmEnabled;
    private final Properties properties;

    public JmixSystemPropertiesLifeCycleListener(String projectBaseDir, String isPnpmEnabled, Properties properties) {
        this.projectBaseDir = projectBaseDir;
        this.isPnpmEnabled = isPnpmEnabled;
        this.properties = properties;
        initializeProperties();
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
        System.getProperties().putAll(properties);
    }

    private void initializeProperties() {
        this.properties.setProperty(VAADIN_PREFIX + PROJECT_BASEDIR, projectBaseDir);
        this.properties.setProperty(VAADIN_PREFIX + InitParameters.BUILD_FOLDER, FrontendUtils.BUILD_FOLDER);
        this.properties.setProperty(PARAM_STUDIO_DIR, projectBaseDir + VIEW_DESIGNER_FOLDER);
        this.properties.setProperty(PARAM_FRONTEND_DIR, projectBaseDir + FRONTEND_FOLDER);
        this.properties.setProperty(PARAM_FLOW_FRONTEND_DIR, projectBaseDir + FLOW_FRONTEND_FOLDER);
        this.properties.setProperty(VAADIN_PREFIX + SERVLET_PARAMETER_ENABLE_PNPM, isPnpmEnabled);
        this.properties.setProperty(VAADIN_PREFIX + PROJECT_FRONTEND_GENERATED_DIR_TOKEN, projectBaseDir + GENERATED_FRONTEND_FOLDER);
        this.properties.setProperty(VAADIN_PREFIX + FRONTEND_HOTDEPLOY, "true");
    }
}
