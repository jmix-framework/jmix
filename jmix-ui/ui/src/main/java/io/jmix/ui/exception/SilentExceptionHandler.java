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

package io.jmix.ui.exception;

import io.jmix.core.JmixOrder;
import io.jmix.core.common.util.SilentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("ui_SilentExceptionHandler")
@Order(JmixOrder.LOWEST_PRECEDENCE - 100)
public class SilentExceptionHandler extends AbstractUiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SilentExceptionHandler.class);

    public SilentExceptionHandler() {
        super(SilentException.class.getName());
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable, UiContext context) {
        log.trace("Suppressed SilentException");
    }
}
