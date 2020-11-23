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

package io.jmix.ui.app.jmxconsole.screen.inspect.operation;


import com.vaadin.shared.ui.ContentMode;
import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import io.jmix.core.TimeSource;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.WindowParam;
import io.jmix.ui.app.jmxconsole.AttributeHelper;
import io.jmix.ui.app.jmxconsole.JmxControlException;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.ScrollBoxLayout;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.MBeanException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

@UiController("ui_MBeanOperationResultScreen")
@UiDescriptor("mbean-operation-result-screen.xml")
public class MBeanOperationResultScreen extends Screen {
    @Autowired
    protected Label<String> resultLabel;

    @Autowired
    protected ScrollBoxLayout resultContainer;

    @Autowired
    protected Downloader downloader;

    @Autowired
    protected TimeSource timeSource;

    @WindowParam
    protected Object result;

    @WindowParam
    protected Throwable exception;

    @WindowParam
    protected String methodName;

    @WindowParam
    protected String beanName;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Messages messages;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected Notifications notifications;

    @Subscribe
    public void beforeShow(BeforeShowEvent beforeShowEvent) {
        String resultMessage = messages.getMessage(getClass(), "operationResult.void");
        Label<String> label = null;
        if (exception != null) {
            label = createLabel(getExceptionMessage(exception));
            resultMessage = messages.getMessage(getClass(), "operationResult.exception");
        } else if (result != null) {
            label = createLabel(AttributeHelper.convertToString(result));
            resultMessage = messages.getMessage(getClass(), "operationResult.result");
        }

        resultLabel.setValue(resultMessage);

        if (label != null) {
            resultContainer.add(label);
        }
    }

    @Subscribe("closeBtn")
    public void close(Button.ClickEvent clickEvent) {
        close(WINDOW_CLOSE_ACTION);
    }

    @Subscribe("exportBtn")
    public void exportToFile(Button.ClickEvent clickEvent) {
        if (result != null || exception != null) {
            String exportResult = String.format("JMX Method %s : %s result\n", beanName, methodName);

            if (result != null) {
                exportResult += AttributeHelper.convertToString(result);
            }
            if (exception != null) {
                exportResult += getExceptionMessage(exception);
            }

            byte[] bytes = exportResult.getBytes(StandardCharsets.UTF_8);
            downloader.download(new ByteArrayDataProvider(bytes, uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                            coreProperties.getTempDir()),
                    String.format("jmx.%s-%s-%s.log",
                            beanName,
                            methodName,
                            new SimpleDateFormat("HH:mm:ss").format(
                                    timeSource.currentTimestamp())));
        } else {
            notifications.create()
                    .withCaption(messages.getMessage(getClass(), "operationResult.resultIsEmpty"))
                    .withType(Notifications.NotificationType.HUMANIZED)
                    .show();
        }
    }

    protected Label<String> createLabel(String message) {
        Label<String> traceLabel = uiComponents.create(Label.NAME);
        traceLabel.setValue(message);

        traceLabel.withUnwrapped(com.vaadin.ui.Label.class, vLabel ->
                vLabel.setContentMode(ContentMode.PREFORMATTED));
        return traceLabel;
    }

    protected String getExceptionMessage(Throwable exception) {
        if (exception instanceof UndeclaredThrowableException)
            exception = exception.getCause();

        if (exception instanceof JmxControlException) {
            exception = exception.getCause();

            if (exception instanceof MBeanException) {
                exception = exception.getCause();
            }
        }

        String msg;
        if (exception != null) {
            msg = String.format("%s: \n%s\n%s",
                    exception.getClass().getName(),
                    exception.getMessage(),
                    ExceptionUtils.getStackTrace(exception));
        } else {
            msg = "";
        }
        return msg;
    }
}