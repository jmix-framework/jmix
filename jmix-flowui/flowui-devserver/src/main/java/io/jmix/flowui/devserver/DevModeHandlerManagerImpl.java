/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.flowui.devserver;

import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.internal.BrowserLiveReloadAccessor;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.Mode;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.flow.server.startup.VaadinInitializerException;
import io.jmix.flowui.devserver.frontend.FrontendUtils;
import io.jmix.flowui.devserver.startup.DevModeInitializer;
import io.jmix.flowui.devserver.startup.DevModeStartupListener;
import jakarta.servlet.annotation.HandlesTypes;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_STUDIO_DIR;

@SuppressWarnings("unused")
public class DevModeHandlerManagerImpl implements DevModeHandlerManager {

    /*
     * Attribute key for storing Dev Mode Handler startup flag.
     *
     * If presented in Servlet Context, shows the Dev Mode Handler already
     * started / become starting. This attribute helps to avoid Dev Mode running
     * twice.
     *
     * Addresses the issue https://github.com/vaadin/spring/issues/502
     */
    private static final class DevModeHandlerAlreadyStartedAttribute implements Serializable {

    }

    private static final Logger log = LoggerFactory.getLogger(DevModeHandlerManagerImpl.class);
    private DevModeHandler devModeHandler;
    private FileWatcher projectThemeFilesWatcher;

    @Override
    public Class<?>[] getHandlesTypes() {
        return DevModeStartupListener.class.getAnnotation(HandlesTypes.class)
                .value();
    }

    @Override
    public void setDevModeHandler(DevModeHandler devModeHandler) {
        if (this.devModeHandler != null) {
            throw new IllegalStateException(
                    "Unable to initialize dev mode handler. A handler is already present: "
                            + this.devModeHandler);
        }
        this.devModeHandler = devModeHandler;
    }

    @Override
    public DevModeHandler getDevModeHandler() {
        return devModeHandler;
    }

    @Override
    public void launchBrowserInDevelopmentMode(String url) {
        // browserLauncher.launchBrowserInDevelopmentMode(url);
    }

    @Override
    public void initDevModeHandler(Set<Class<?>> classes, VaadinContext context)
            throws VaadinInitializerException {
        setDevModeHandler(
                DevModeInitializer.initDevModeHandler(classes, context));
        // startWatchingThemeFolder(context);
        startWatchingAndSyncThemesFolder(context);
        setDevModeStarted(context);

    }

    @Override
    public void stopDevModeHandler() {
        if (devModeHandler != null) {
            devModeHandler.stop();
            devModeHandler = null;
        }
        if (projectThemeFilesWatcher != null) {
            try {
                projectThemeFilesWatcher.stop();
            } catch (Exception e) {
                String message = "Failed to stop theme files watcher";
                FrontendUtils.console(FrontendUtils.RED, message + Arrays.toString(e.getStackTrace()));
                log.error(message, e);
            }
        }
    }

    private void startWatchingThemeFolder(VaadinContext context) {
        ApplicationConfiguration config = ApplicationConfiguration.get(context);

        if (config.getMode() != Mode.DEVELOPMENT_BUNDLE) {
            // Theme files are watched by Vite or app runs in prod mode
            return;
        }

        try {
            File projectFolder = config.getProjectFolder();
            Optional<String> themeName = FrontendUtils
                    .getThemeName(projectFolder);

            if (themeName.isEmpty()) {
                log.debug("Found no custom theme in the project. "
                        + "Skipping watching the theme files");
                return;
            }

            // TODO: frontend folder to be taken from config
            // see https://github.com/vaadin/flow/pull/15552
            File watchDirectory = new File(projectFolder,
                    Path.of(FrontendUtils.FRONTEND,
                                    Constants.APPLICATION_THEME_ROOT, themeName.get())
                            .toString());

            Optional<BrowserLiveReload> liveReload = BrowserLiveReloadAccessor
                    .getLiveReloadFromContext(context);
            if (liveReload.isPresent()) {
                projectThemeFilesWatcher = new FileWatcher(
                        file -> liveReload.get().reload(), watchDirectory);
                projectThemeFilesWatcher.start();
            } else {
                String message = "Browser live reload is not available. Failed to start live-reload for theme files";
                FrontendUtils.logInFile(message);
                log.error(message);
            }
        } catch (Exception e) {
            String message = "Failed to start live-reload for theme files";
            FrontendUtils.logInFile(message + "\n" + Arrays.toString(e.getStackTrace()));
            log.error(message, e);
        }
    }

    private void synchronizeThemesFolders(File projectThemesFolder, File stuidThemesFolder) {
        try {
            FrontendUtils.logInFile("Synchronizing themes folder...");
            FileUtils.copyDirectory(projectThemesFolder, stuidThemesFolder);
        } catch (IOException e) {
            FrontendUtils.logInFile(
                    "Error when trying to synchronize themes folders..." +
                            "\nProject themes dir:" + projectThemesFolder +
                            "\nStudio themes dir:" + stuidThemesFolder +
                            "\nTheme changes cannot by applied by live reload..."
            );
        }
    }

    private void setDevModeStarted(VaadinContext context) {
        context.setAttribute(DevModeHandlerAlreadyStartedAttribute.class,
                new DevModeHandlerAlreadyStartedAttribute());
    }

    /**
     * Shows whether {@link DevModeHandler} has been already started or not.
     *
     * @param context The {@link VaadinContext}, not <code>null</code>
     * @return <code>true</code> if {@link DevModeHandler} has already been
     * started, <code>false</code> - otherwise
     */
    public static boolean isDevModeAlreadyStarted(VaadinContext context) {
        assert context != null;
        return context.getAttribute(
                DevModeHandlerAlreadyStartedAttribute.class) != null;
    }

    /**
     * Synchronize project themes folder and studio themes folder.
     */
    private void startWatchingAndSyncThemesFolder(VaadinContext context) {
        try {
            ApplicationConfiguration config = ApplicationConfiguration.get(context);
            File projectFolder = config.getProjectFolder();
            File studioFolder = new File(System.getProperty(PARAM_STUDIO_DIR));
            Optional<String> themeName = FrontendUtils.getThemeName(studioFolder);

            // TODO: frontend folder to be taken from config
            // see https://github.com/vaadin/flow/pull/15552
            File projectThemesFolder = new File(projectFolder,
                    Path.of(FrontendUtils.FRONTEND,
                                    Constants.APPLICATION_THEME_ROOT, themeName.orElse(""))
                            .toString());

            File studioThemesFolder = new File(studioFolder,
                    Path.of(FrontendUtils.FRONTEND,
                                    Constants.APPLICATION_THEME_ROOT, themeName.orElse(""))
                            .toString());

            projectThemeFilesWatcher = new FileWatcher(
                    projectThemeFile -> synchronizeThemesFolders(projectThemesFolder, studioThemesFolder),
                    projectThemesFolder
            );
            projectThemeFilesWatcher.start();

        } catch (Exception e) {
            String message = "Failed to start synchronizing themes files";
            FrontendUtils.logInFile(message + "\n" + Arrays.toString(e.getStackTrace()));
            log.error(message, e);
        }
    }
}
