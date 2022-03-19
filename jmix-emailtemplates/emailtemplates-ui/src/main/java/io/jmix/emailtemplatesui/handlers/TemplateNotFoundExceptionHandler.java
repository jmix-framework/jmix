/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplatesui.handlers;


import io.jmix.emailtemplates.exception.TemplateNotFoundException;
import io.jmix.ui.Notifications;
import io.jmix.ui.exception.UiExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("emltmp_TemplateNotFoundExceptionHandler")
public class TemplateNotFoundExceptionHandler implements UiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TemplateNotFoundExceptionHandler.class);

    @Override
    public boolean handle(Throwable exception, UiContext context) {
        if (exception instanceof TemplateNotFoundException) {
            context.getNotifications().create(Notifications.NotificationType.ERROR)
                    .withDescription(exception.getMessage())
                    .show();
            log.warn(exception.getMessage());
            return true;
        }
        return false;
    }
}
