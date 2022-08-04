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
import io.jmix.ui.app.jmxconsole.JmxControl;
import io.jmix.ui.app.jmxconsole.JmxControlException;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanOperation;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.ProgressBar;
import io.jmix.ui.component.ScrollBoxLayout;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.screen.*;
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

    protected Object result;

    protected Throwable exception;

    @WindowParam
    protected ManagedBeanOperation operation;

    @WindowParam
    protected Object[] paramValues;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected JmxControl jmxControl;

    @Autowired
    protected Button exportBtn;

    @Autowired
    protected ProgressBar taskProgressBar;

    @Autowired
    protected BackgroundWorker backgroundWorker;

    protected BackgroundTaskHandler<Object> taskHandler;

    @Subscribe
    public void afterShow(AfterShowEvent afterShowEvent) {
        BackgroundTask<Long, Object> task = new OperationBackgroundTask(uiProperties.getJmxConsoleMBeanOperationTimeoutSec(),
                this);
        taskHandler = backgroundWorker.handle(task);
        taskHandler.execute();
    }

    @Subscribe
    public void beforeClose(BeforeCloseEvent event) {
        if (taskHandler.isAlive()) {
            taskHandler.cancel();
        }
    }

    @Subscribe("closeBtn")
    public void close(Button.ClickEvent clickEvent) {
        close(WINDOW_CLOSE_ACTION);
    }

    @Subscribe("exportBtn")
    public void exportToFile(Button.ClickEvent clickEvent) {
        if (result != null || exception != null) {
            String exportResult = String.format("JMX Method %s : %s result\n",
                    operation.getMbean().getClassName(), operation.getName());

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
                            operation.getMbean().getClassName(),
                            operation.getName(),
                            new SimpleDateFormat("HH:mm:ss").format(
                                    timeSource.currentTimestamp())));
        } else {
            notifications.create()
                    .withCaption(messageBundle.getMessage("operationResult.resultIsEmpty"))
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

    protected class OperationBackgroundTask extends BackgroundTask<Long, Object> {
        protected Label<String> label = null;
        protected String resultMessage;

        protected OperationBackgroundTask(long timeoutSeconds, Screen screen) {
            super(timeoutSeconds, screen);
        }

        @Override
        public Object run(TaskLifeCycle<Long> taskLifeCycle) throws Exception {
            return jmxControl.invokeOperation(operation, paramValues);
        }

        @Override
        public void done(Object res) {
            result = res;
            if (result != null) {
                label = createLabel(AttributeHelper.convertToString(result));
                resultMessage = messageBundle.getMessage("operationResult.result");
            } else {
                resultMessage = messageBundle.getMessage("operationResult.void");
            }
            showResult();
        }

        @Override
        public boolean handleException(Exception ex) {
            exception = ex;
            label = createLabel(getExceptionMessage(ex));
            resultMessage = messageBundle.getMessage("operationResult.exception");
            showResult();
            return true;
        }

        @Override
        public boolean handleTimeoutException() {
            Screen screen = getOwnerScreen();
            ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);
            screenContext.getScreens().remove(screen);

            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(OperationBackgroundTask.class, "backgroundWorkProgress.timeout"))
                    .withDescription(messages.getMessage(OperationBackgroundTask.class, "backgroundWorkProgress.timeoutMessage"))
                    .show();

            return true;
        }

        protected void showResult() {
            taskProgressBar.setVisible(false);
            resultLabel.setValue(resultMessage);

            if (label != null) {
                resultContainer.add(label);
            }
            exportBtn.setEnabled(true);
        }
    }
}