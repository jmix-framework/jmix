/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.exception;

import io.jmix.core.FileStorageException;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class FileNotExistsExceptionHandler extends AbstractUiExceptionHandler {

    private final Notifications notifications;
    private final Messages messages;

    public FileNotExistsExceptionHandler(Notifications notifications, Messages messages) {
        super(FileStorageException.class.getName());

        this.notifications = notifications;
        this.messages = messages;
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable) {
        if (!(throwable instanceof FileStorageException)) {
            return;
        }
        if (FileStorageException.Type.FILE_NOT_FOUND != ((FileStorageException) throwable).getType()) {
            return;
        }

        String fileNotFoundMessage = messages.getMessage("fileNotFound.message");
        String formattedMessage = String.format(fileNotFoundMessage, ((FileStorageException) throwable).getFileName());

        notifications.create(formattedMessage)
                .withType(Notifications.Type.ERROR)
                .show();
    }
}
