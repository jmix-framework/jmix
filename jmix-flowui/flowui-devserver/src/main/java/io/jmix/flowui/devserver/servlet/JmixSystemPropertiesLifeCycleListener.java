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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import io.jmix.flowui.devserver.shutdown.ShutdownTask;
import io.jmix.flowui.devserver.shutdown.SynchronizeThemesShutdownTask;
import io.jmix.flowui.devserver.startup.CopyFilesStartupTask;
import io.jmix.flowui.devserver.startup.StartupContext;
import io.jmix.flowui.devserver.startup.StartupTask;
import io.jmix.flowui.devserver.startup.SynchronizeThemesStartupTask;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.server.Constants.VAADIN_PREFIX;
import static com.vaadin.flow.server.InitParameters.FRONTEND_HOTDEPLOY;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_ENABLE_PNPM;
import static com.vaadin.flow.server.frontend.FrontendUtils.PROJECT_BASEDIR;
import static java.lang.Boolean.parseBoolean;

public class JmixSystemPropertiesLifeCycleListener implements LifeCycle.Listener {

    public static final String THEME_VALUE_PROPERTY = "jmix.devserver.themeValue";

    public static final String JMIX_VERSION_PROPERTY = "jmix.devserver.jmixVersion";
    public static final String JMIX_STUDIO_VERSION_PROPERTY = "jmix.devserver.jmixStudioVersion";

    public static final String USE_PROJECT_FOLDER_PROPERTY = "jmix.devserver.useProjectFolder";
    public static final String USE_PROJECT_PROPERTIES_PROPERTY = "jmix.devserver.useProjectProperties";

    public static final String STUDIO_VIEW_DESIGNER_DIR_PROPERTY = "STUDIO_VIEW_DESIGNER_DIR";
    public static final String VIEW_DESIGNER_FOLDER = "/.jmix/screen-designer";

    private static final String JMIX_PREVIEW_PROPERTIES_FILE_NAME = "jmix-preview.properties";

    private static final Logger log = LoggerFactory.getLogger(JmixSystemPropertiesLifeCycleListener.class);

    private static final List<Class<? extends StartupTask>> startupTasks = List.of(
            CopyFilesStartupTask.class,
            SynchronizeThemesStartupTask.class
    );

    private static final List<Class<? extends ShutdownTask>> shutdownTasks = List.of(
            SynchronizeThemesShutdownTask.class
    );

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
        onStartup();
    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
        onShutdown();
    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        onShutdown();
    }

    private void onStartup() {
        StartupContext context = new StartupContext(getProjectThemeName(),
                new File(projectBaseDir), new File(getDesignerDir()));

        for (Class<? extends StartupTask> taskClass : startupTasks) {
            try {
                log.info("Executing startup task {}", taskClass.getCanonicalName());
                StartupTask task = taskClass.getConstructor().newInstance();
                task.execute(context);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                log.warn("Can not create instance of {}", taskClass.getCanonicalName());
            }
        }
    }

    private static void onShutdown() {
        for (Class<? extends ShutdownTask> taskClass : shutdownTasks) {
            try {
                log.info("Executing shutdown task {}", taskClass.getCanonicalName());
                ShutdownTask task = taskClass.getConstructor().newInstance();
                task.execute();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                log.warn("Can not create instance of {}", taskClass.getCanonicalName());
            }
        }
    }

    private void initializeProperties() {
        readJmixPreviewPropertiesFile();

        properties.setProperty(VAADIN_PREFIX + SERVLET_PARAMETER_ENABLE_PNPM, isPnpmEnabled);
        properties.setProperty(VAADIN_PREFIX + FRONTEND_HOTDEPLOY, getFrontedHotDeploy());

        String designerDir = getDesignerDir();
        String projectDir = getUseProjectFolder() ? projectBaseDir : designerDir;

        properties.setProperty(VAADIN_PREFIX + PROJECT_BASEDIR, projectDir);
        properties.setProperty(STUDIO_VIEW_DESIGNER_DIR_PROPERTY, designerDir);

        System.getProperties().putAll(properties);

        log.info("Properties has been initialized.\n" +
                        "Jmix version: {}; " +
                        "Jmix Studio version: {}; " +
                        "Use project folder: {}; " +
                        "Use project properties: {}; " +
                        "Project theme: {}; " +
                        "Using properties: {}; ",
                getJmixVersion(),
                getJmixStudioVersion(),
                getUseProjectFolder(),
                getUseProjectProperties(),
                getProjectThemeName(),
                properties);
    }

    private void readJmixPreviewPropertiesFile() {
        log.info("Reading properties from {}", JMIX_PREVIEW_PROPERTIES_FILE_NAME);
        File propertiesFile = new File(projectBaseDir, JMIX_PREVIEW_PROPERTIES_FILE_NAME);

        if (propertiesFile.exists() && propertiesFile.isFile()) {
            try {
                try (var is = FileUtils.openInputStream(propertiesFile)) {
                    try {
                        properties.load(is);
                    } catch (Throwable e) {
                        log.warn("Exception when loading properties from file", e);
                    }
                }
            } catch (IOException e) {
                log.warn("Exception when reading {} file", JMIX_PREVIEW_PROPERTIES_FILE_NAME);
            }
        } else {
            log.info("{} file not found, skipping properties reading", propertiesFile);
        }
    }

    private String getDesignerDir() {
        return projectBaseDir + VIEW_DESIGNER_FOLDER;
    }

    private String getProjectThemeName() {
        return properties.getOrDefault(THEME_VALUE_PROPERTY, "").toString();
    }

    private String getJmixVersion() {
        return properties.getProperty(JMIX_VERSION_PROPERTY);
    }

    private String getJmixStudioVersion() {
        return properties.getProperty(JMIX_STUDIO_VERSION_PROPERTY);
    }

    private String getFrontedHotDeploy() {
        return properties.getProperty(VAADIN_PREFIX + FRONTEND_HOTDEPLOY, "false");
    }

    private boolean getUseProjectFolder() {
        boolean useProjectFolder = false;
        try {
            useProjectFolder = parseBoolean(properties.getProperty(USE_PROJECT_FOLDER_PROPERTY, "false"));
        } catch (Exception ignored) {
        }
        return useProjectFolder;
    }

    private boolean getUseProjectProperties() {
        boolean useProjectProperties = true;
        try {
            useProjectProperties = parseBoolean(properties.getProperty(USE_PROJECT_PROPERTIES_PROPERTY, "true"));
        } catch (Exception ignored) {
        }
        return useProjectProperties;
    }
}
