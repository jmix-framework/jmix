/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.exception;

import com.vaadin.spring.annotation.UIScope;
import io.jmix.core.FileStorageException;
import io.jmix.core.Messages;
import io.jmix.ui.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@UIScope
@Component("ui_FileStorageExceptionHandler")
public class FileStorageExceptionHandler extends AbstractUiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(FileStorageExceptionHandler.class);

    @Autowired
    protected Messages messages;

    public FileStorageExceptionHandler() {
        super(FileStorageException.class.getName());
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable, UiContext context) {
        String msg = null;
        if (throwable != null) {
            FileStorageException storageException = (FileStorageException) throwable;
            String fileName = storageException.getFileName();
            if (storageException.getType().equals(FileStorageException.Type.FILE_NOT_FOUND)) {
                msg = messages.formatMessage(FileStorageExceptionHandler.class,
                        "fileStorageException.fileNotFound", fileName);
            } else if (storageException.getType().equals(FileStorageException.Type.STORAGE_INACCESSIBLE)) {
                msg = messages.getMessage(FileStorageExceptionHandler.class,
                        "fileStorageException.fileStorageInaccessible");
            }
        }

        if (msg == null) {
            msg = messages.getMessage(FileStorageExceptionHandler.class,
                    "fileStorageException.message");

            if (throwable != null) {
                log.error("Unable to handle FileStorageException", throwable);
            }
        }

        context.getNotifications().create(Notifications.NotificationType.ERROR)
                .withCaption(msg)
                .show();
    }
}
