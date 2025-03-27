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

package io.jmix.flowui.devserver.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.vaadin.flow.server.frontend.FrontendUtils.RED;
import static com.vaadin.flow.server.frontend.FrontendUtils.console;
import static io.jmix.flowui.devserver.servlet.JmixSystemPropertiesLifeCycleListener.STUDIO_VIEW_DESIGNER_DIR_PROPERTY;

public final class JmixLoggerUtils {

    public static void logInFile(String text) {
        logInFile(text, true);
    }

    public static void logInFile(String text, boolean createLogFileIfNotExist) {
        Optional<File> logFile = findLogFile();
        if (createLogFileIfNotExist || !logFile.map(File::exists).orElse(false)) {
            try {
                logFile = createLogFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        logFile.ifPresent(file -> logInFile(file, text));
    }

    private static void logInFile(@Nonnull File logFile, String text) {
        if (logFile.exists() && logFile.canWrite()) {
            try {
                Files.writeString(logFile.toPath(), text, StandardOpenOption.APPEND);
            } catch (IOException e) {
                String errorMsg = "Error when writing in log file...";
                console(RED, errorMsg + "\n" + Arrays.toString(e.getStackTrace()));
            }
        } else if (!logFile.exists()) {
            throw new RuntimeException("File not found: " + logFile.getAbsolutePath());
        }
    }

    private static Optional<File> findLogFile() {
        try {
            Path filePath = getLogFilePath();
            if (filePath == null) {
                String logFileIsNull = "Log file path is null";
                console(RED, logFileIsNull);
                return Optional.empty();
            }

            File logFile = filePath.toFile();
            if (!logFile.exists()) {
                int lastSeparatorIndex = filePath.toString().lastIndexOf(File.separator);
                Path logDirPath = Paths.get(filePath.toString().substring(0, lastSeparatorIndex));
                if (!logDirPath.toFile().exists()) {
                    Files.createDirectory(logDirPath);
                }
                logFile = Files.createFile(filePath).toFile();
            }

            return Optional.of(logFile);

        } catch (IOException e) {
            String stackTrace = Arrays.toString(e.getStackTrace());
            console(RED, stackTrace);
            return Optional.empty();
        }
    }

    private static Optional<File> createLogFile() throws IOException {
        Path logFilePath = getLogFilePath();
        if (logFilePath == null || !Files.exists(logFilePath)) {
            final String studioDir = getStudioDirPathFromProperty();
            if (studioDir != null) {
                String logDir = "tmp";
                Path logDirPath = Paths.get(studioDir, logDir);
                if (!Files.exists(logDirPath)) {
                    Files.createDirectories(logDirPath);
                }

                logFilePath = Paths.get(studioDir, logDir, getActualLogFileName());
                if (!Files.exists(logFilePath)) {
                    Files.createFile(logFilePath);
                }
            }
        }
        if (logFilePath == null) {
            return Optional.empty();
        } else {
            return Optional.of(logFilePath.toFile());
        }
    }

    @Nullable
    private static Path getLogFilePath() {
        final String studioDir = getStudioDirPathFromProperty();
        if (studioDir == null || studioDir.isEmpty()) {
            return null;
        } else {
            final String logDir = "tmp";
            return Paths.get(studioDir, logDir, getActualLogFileName());
        }
    }

    private static String getStudioDirPathFromProperty() {
        return System.getProperty(STUDIO_VIEW_DESIGNER_DIR_PROPERTY);
    }

    @Nonnull
    private static String getActualLogFileName() {
        final String logFileId = getCurrentDateTime("yyyy-MM-dd");
        final String logFileBaseName = "log-";
        final String logFileExtension = ".log";
        return logFileBaseName + logFileId + logFileExtension;
    }

    @Nonnull
    private static String getCurrentDateTime(String dateFormatPattern) {
        DateFormat formatter = new SimpleDateFormat(dateFormatPattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return formatter.format(calendar.getTime());
    }
}
