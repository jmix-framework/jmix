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

import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.Mode;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.frontend.ThemeUtils;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.flow.server.startup.VaadinInitializerException;
import io.jmix.flowui.devserver.frontend.FrontendUtils;
import io.jmix.flowui.devserver.startup.DevModeInitializer;
import io.jmix.flowui.devserver.startup.DevModeStartupListener;
import jakarta.servlet.annotation.HandlesTypes;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_STUDIO_DIR;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.PARAM_THEME_VALUE;

/**
 * Provides API to access to the {@link DevModeHandler} instance.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @author Vaadin Ltd
 * @since
 */
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
    private ThemeFilesSynchronizer projectThemeFilesWatcher;
    final private Set<Command> shutdownCommands = new HashSet<>();

    private String applicationUrl;
    private boolean fullyStarted = false;

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
    public void initDevModeHandler(Set<Class<?>> classes, VaadinContext context)
            throws VaadinInitializerException {
        setDevModeHandler(
                DevModeInitializer.initDevModeHandler(classes, context));
        CompletableFuture.runAsync(() -> {
            DevModeHandler devModeHandler = getDevModeHandler();
            if (devModeHandler instanceof AbstractDevServerRunner) {
                ((AbstractDevServerRunner) devModeHandler).waitForDevServer();
//            } else if (devModeHandler instanceof DevBundleBuildingHandler devBundleBuilder) {
//                devBundleBuilder.waitForDevBundle();
            }

            ApplicationConfiguration config = ApplicationConfiguration
                    .get(context);

            startWatchingAndSyncThemesFolder(context);
            watchExternalDependencies(context, config);
            setFullyStarted(true);
        });
        setDevModeStarted(context);
        // this.browserLauncher = new BrowserLauncher(context);
    }

    private void watchExternalDependencies(VaadinContext context,
                                           ApplicationConfiguration config) {
        File frontendFolder = FrontendUtils.getProjectFrontendDir(config);
        File jarFrontendResourcesFolder = FrontendUtils
                .getJarResourcesFolder(frontendFolder);
        registerWatcherShutdownCommand(new ExternalDependencyWatcher(context,
                jarFrontendResourcesFolder));

    }

    private void startWatchingThemeFolder(VaadinContext context,
                                          ApplicationConfiguration config) {

        if (config.getMode() != Mode.DEVELOPMENT_BUNDLE) {
            // Theme files are watched by Vite or app runs in prod mode
            return;
        }

        try {
            Optional<String> maybeThemeName = ThemeUtils.getThemeName(context);

            if (maybeThemeName.isEmpty()) {
                log.debug("Found no custom theme in the project. "
                        + "Skipping watching the theme files");
                return;
            }
            List<String> activeThemes = ThemeUtils.getActiveThemes(context);
            for (String themeName : activeThemes) {
                File themeFolder = ThemeUtils.getThemeFolder(
                        FrontendUtils.getProjectFrontendDir(config), themeName);
                registerWatcherShutdownCommand(
                        new ThemeLiveUpdater(themeFolder, context));
            }
        } catch (Exception e) {
            log.error("Failed to start live-reload for theme files", e);
        }
    }

    @Override
    public void stopDevModeHandler() {
        if (devModeHandler != null) {
            devModeHandler.stop();
            devModeHandler = null;
        }

        try {
            projectThemeFilesWatcher.stop();
        } catch (Exception e) {
            log.warn("Exception when stopping projectThemeFilesWatcher", e);
        }

        for (Command shutdownCommand : shutdownCommands) {
            try {
                shutdownCommand.execute();
            } catch (Exception e) {
                String msg = "Failed to execute shut down command %s"
                        .formatted(shutdownCommand.getClass().getName());
                FrontendUtils.console(FrontendUtils.RED, msg);
                log.error(msg, e);
            }
        }
        shutdownCommands.clear();
    }

    @Override
    public void launchBrowserInDevelopmentMode(String url) {
        // browserLauncher.launchBrowserInDevelopmentMode(url);
    }

    @Override
    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
        reportApplicationUrl();
    }

    private void setFullyStarted(boolean fullyStarted) {
        this.fullyStarted = fullyStarted;
        reportApplicationUrl();
    }

    private void reportApplicationUrl() {
        if (fullyStarted && applicationUrl != null) {
            log.info("Application running at {}", applicationUrl);
        }
    }

    private void setDevModeStarted(VaadinContext context) {
        context.setAttribute(DevModeHandlerAlreadyStartedAttribute.class,
                new DevModeHandlerAlreadyStartedAttribute());
    }

    private void registerWatcherShutdownCommand(Closeable watcher) {
        registerShutdownCommand(() -> {
            try {
                watcher.close();
            } catch (Exception e) {
                log.error("Failed to stop watcher {}",
                        watcher.getClass().getName(), e);
            }
        });
    }

    @Override
    public void registerShutdownCommand(Command command) {
        shutdownCommands.add(command);
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
            File projectFolder = FrontendUtils.getProjectBaseDir(config);
            Optional<String> themeName = Optional.ofNullable(System.getProperty(PARAM_THEME_VALUE));

            if (themeName.isEmpty()) {
                String msg = "Found no custom theme in the project. "
                        + "Skipping watching the theme files";
                log.info(msg);
                return;
            }

            startThemesFolderWatcher(themeName.get(), projectFolder, config);

        } catch (Exception e) {
            String message = "Failed to start synchronizing themes files";
            log.error(message, e);
        }
    }

    private void startThemesFolderWatcher(String themeName, File projectFolder, ApplicationConfiguration config) throws Exception {
        Path projectFrontendDir = Path.of("src", "main", FrontendUtils.FRONTEND, Constants.APPLICATION_THEME_ROOT, themeName);
        Path legacyProjectFrontendDir = Path.of(FrontendUtils.FRONTEND, Constants.APPLICATION_THEME_ROOT, themeName);

        File projectThemesFolder = new File(projectFolder, projectFrontendDir.toString());
        File legacyProjectThemesFolder = new File(projectFolder, legacyProjectFrontendDir.toString());
        File actualProjectThemesFolder = projectThemesFolder.exists()
                ? projectThemesFolder : legacyProjectThemesFolder.exists()
                ? legacyProjectThemesFolder : projectThemesFolder;

        File studioThemesFolder = new File(
                FrontendUtils.getProjectFrontendDir(config),
                Path.of(Constants.APPLICATION_THEME_ROOT, themeName).toString());

        projectThemeFilesWatcher = new ThemeFilesSynchronizer(
                projectThemeFile -> synchronizeThemesFolders(actualProjectThemesFolder, studioThemesFolder),
                actualProjectThemesFolder
        );

        projectThemeFilesWatcher.start();
    }

    private void synchronizeThemesFolders(File projectThemesFolder, File studioThemesFolder) {
        try {
            log.info("Synchronizing themes folder...");
            FileUtils.copyDirectory(projectThemesFolder, studioThemesFolder);
        } catch (IOException e) {
            log.info("Error when trying to synchronize themes folders...\n" +
                    "Project themes dir:{}\n" +
                    "Studio themes dir:{}\n" +
                    "Theme changes cannot by applied by live reload...",
                    projectThemesFolder, studioThemesFolder);
        }
    }
}
