/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.devserver.theme;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import io.jmix.flowui.devserver.startup.CopyFilesStartupTask;
import io.jmix.flowui.devserver.startup.StartupContext;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for the file or sub-directory changes in the given directory.
 */
public class ThemeFilesSynchronizer implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ThemeFilesSynchronizer.class);
    private static final long DEFAULT_TIMEOUT = 1000;
    private final FileAlterationMonitor monitor;
    private long timeout = DEFAULT_TIMEOUT;

    private static volatile ThemeFilesSynchronizer INSTANCE = null;

    /**
     * Synchronize project themes folder and studio themes folder.
     */
    public static void start(StartupContext context) {
        try {
            startThemesFolderWatcher(context);
        } catch (Exception e) {
            String message = "Failed to start synchronizing themes files";
            log.error(message, e);
        }
    }

    public static void stop() {
        if (INSTANCE != null) {
            try {
                INSTANCE.doStop();
                INSTANCE = null;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Creates an instance of the file watcher for the given directory.
     * <p>
     * Reports the changed file or directory as a {@link File} instance to the
     * provided consumer.
     * <p>
     * Watches the files create/delete and directory create/delete events.
     *
     * @param onChangeConsumer to be called when any change detected
     * @param watchDirectory   the directory to watch for the changes, cannot be empty
     */
    private ThemeFilesSynchronizer(SerializableConsumer<File> onChangeConsumer,
                                   File... watchDirectory) {
        this(onChangeConsumer, file -> true, watchDirectory);
    }

    private ThemeFilesSynchronizer(File watchDirectory, SerializableConsumer<File> onChangeConsumer) {
        this(onChangeConsumer, watchDirectory);
    }

    /**
     * Creates an instance of the file watcher for the given directory taking
     * into account the given file filter.
     * <p>
     * Reports the changed file or directory as a {@link File} instance to the
     * provided consumer.
     * <p>
     * Watches the files create/delete and directory create/delete events.
     *
     * @param fileFilter       defined if the given file or directory should be watched
     * @param onChangeConsumer to be called when any change detected
     * @param watchDirectory   the directory to watch for the changes, cannot be empty
     */
    private ThemeFilesSynchronizer(SerializableConsumer<File> onChangeConsumer,
                                   SerializablePredicate<File> fileFilter, File... watchDirectory) {
        this(new DefaultFileListener(onChangeConsumer), fileFilter,
                watchDirectory);
    }

    /**
     * Creates an instance of the file watcher for the given directory taking
     * into account the given file filter.
     * <p>
     * Reports the changed file or directory as a {@link File} instance to the
     * provided consumer.
     * <p>
     * Reports file and directory changes to the given listener.
     *
     * @param fileFilter     defined if the given file or directory should be watched
     * @param listener       to be invoked once any changes detected
     * @param watchDirectory the directory to watch for the changes, cannot be empty
     */
    private ThemeFilesSynchronizer(FileAlterationListener listener,
                                   SerializablePredicate<File> fileFilter, File... watchDirectory) {
        Objects.requireNonNull(watchDirectory, "Watch directory cannot be empty");
        if (watchDirectory.length < 1) {
            throw new IllegalArgumentException("Watch directory cannot be empty");
        }

        Objects.requireNonNull(fileFilter, "File filter cannot be empty");
        Objects.requireNonNull(listener, "File alteration listener cannot be empty");

        monitor = new FileAlterationMonitor(timeout);

        Arrays.stream(watchDirectory).forEach(dir -> {
            FileAlterationObserver observer = new FileAlterationObserver(dir, fileFilter::test);
            observer.addListener(listener);
            monitor.addObserver(observer);
        });
    }

    /**
     * Starts the file watching.
     *
     * @throws Exception if an error occurs during startup
     */
    private void doStart() throws Exception {
        log.info("Starting project theme folder synchronization");
        monitor.start();
    }

    /**
     * Stops the file watching.
     *
     * @throws Exception if an error occurs during stop
     */
    private void doStop() throws Exception {
        log.info("Stopping project theme folder synchronization");
        monitor.stop();
    }

    /**
     * Stops the file watching and waits for a given stop interval for watching
     * thread to finish.
     *
     * @param stopInterval time interval to wait for the watching thread
     * @throws Exception if an error occurs during stop
     */
    private void doStop(long stopInterval) throws Exception {
        monitor.stop(stopInterval);
    }

    /**
     * Sets the time interval between file/directory checks.
     *
     * @param timeout time interval between file/directory checks
     */
    private void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private static void startThemesFolderWatcher(StartupContext context) throws Exception {
        String themeName = context.themeName();
        if (StringUtils.isBlank(themeName)) {
            log.info("Project theme name not set. Skipping watching the theme files");
            return;
        }

        File projectThemeFolder = context.getProjectThemeFolder();
        if (!projectThemeFolder.exists()) {
            log.info("Project theme folder not set. Skipping watching the theme files");
            return;
        }

        File designerThemeFolder = context.getDesignerThemeFolder();

        if (INSTANCE != null) {
            INSTANCE.doStop();
        }

        INSTANCE = new ThemeFilesSynchronizer(projectThemeFolder,
                projectThemeFile -> synchronizeThemesFolders(context));

        log.info("Scheduling synchronization between project theme folder {} and designer theme folder {}",
                projectThemeFolder, designerThemeFolder);

        INSTANCE.doStart();
    }

    private static void synchronizeThemesFolders(StartupContext context) {
        log.info("Synchronizing themes folders...");
        CopyFilesStartupTask.copyThemes(context);
    }

    /**
     * Default file change listener which triggers the callback only when file
     * or directory is changed/deleted.
     */
    private static final class DefaultFileListener
            extends FileAlterationListenerAdaptor implements Serializable {

        private final SerializableConsumer<File> onChangeConsumer;

        public DefaultFileListener(
                SerializableConsumer<File> onChangeConsumer) {
            this.onChangeConsumer = onChangeConsumer;
        }

        @Override
        public void onDirectoryChange(File directory) {
            onChangeConsumer.accept(directory);
        }

        @Override
        public void onDirectoryDelete(File directory) {
            onChangeConsumer.accept(directory);
        }

        @Override
        public void onFileChange(File file) {
            onChangeConsumer.accept(file);
        }

        @Override
        public void onFileDelete(File file) {
            onChangeConsumer.accept(file);
        }
    }
}