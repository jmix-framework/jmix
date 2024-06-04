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

package io.jmix.jmxconsole.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import io.jmix.core.CoreProperties;
import io.jmix.core.TimeSource;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundTaskHandler;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.jmxconsole.AttributeHelper;
import io.jmix.jmxconsole.JmxConsoleProperties;
import io.jmix.jmxconsole.JmxControl;
import io.jmix.jmxconsole.JmxControlException;
import io.jmix.jmxconsole.model.ManagedBeanOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.MBeanException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

@Route(value = "jmxconsole/mbeanresult", layout = DefaultMainViewParent.class)
@ViewController("sys_MBeanOperationResultView")
@ViewDescriptor("mbean-operation-result-view.xml")
@DialogMode(width = "60em", resizable = true)
public class MBeanOperationResultView extends StandardView {
    @ViewComponent
    protected ProgressBar taskProgressBar;
    @ViewComponent
    protected JmixButton exportBtn;
    @ViewComponent
    protected VerticalLayout resultVBox;

    @Autowired
    protected JmxControl jmxControl;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected BackgroundWorker backgroundWorker;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected JmxConsoleProperties jmxConsoleProperties;

    protected BackgroundTaskHandler<Object> taskHandler;
    protected Object result;
    protected Throwable exception;
    protected ManagedBeanOperation operation;
    protected Object[] paramValues;

    public void setParameters(ManagedBeanOperation operation, Object[] paramValues) {
        this.operation = operation;
        this.paramValues = paramValues;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        BackgroundTask<Long, Object> task =
                new OperationBackgroundTask(jmxConsoleProperties.getJmxConsoleMBeanOperationTimeoutSec(), this);
        taskHandler = backgroundWorker.handle(task);
        taskHandler.execute();
    }

    @Subscribe("exportAction")
    public void onExportAction(final ActionPerformedEvent event) {
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
            ByteArrayDownloadDataProvider dataProvider =
                    new ByteArrayDownloadDataProvider(bytes, uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                            coreProperties.getTempDir());
            downloader.download(dataProvider,
                    String.format("jmx.%s_%s_%s.log",
                            operation.getMbean().getClassName(),
                            operation.getName(),
                            new SimpleDateFormat("HH-mm-ss").format(
                                    timeSource.currentTimestamp())));
        } else {
            notifications.create(messageBundle.getMessage("operationResult.resultIsEmpty"))
                    .withType(Notifications.Type.DEFAULT)
                    .show();
        }
    }

    protected JmixTextArea createTextArea(String message) {
        JmixTextArea textArea = uiComponents.create(JmixTextArea.class);
        textArea.setWidthFull();
        textArea.setMaxHeight("35em");
        textArea.setValue(message);
        textArea.setReadOnly(true);
        return textArea;
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

        protected OperationBackgroundTask(long timeoutSeconds, View<?> view) {
            super(timeoutSeconds, view);
        }

        @Override
        public Object run(TaskLifeCycle<Long> taskLifeCycle) {
            return jmxControl.invokeOperation(operation, paramValues);
        }

        @Override
        public void done(Object res) {
            result = res;
            String resultString = AttributeHelper.convertToString(result);
            taskProgressBar.setVisible(false);
            if (StringUtils.isNotEmpty(resultString)) {
                showResult(resultString);
            } else {
                closeWithDefaultAction();
                notifications.create(messageBundle.getMessage("operationResult.void"))
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            }
        }

        @Override
        public boolean handleException(Exception ex) {
            exception = ex;
            taskProgressBar.setVisible(false);
            showResult(getExceptionMessage(ex));
            return true;
        }

        @Override
        public boolean handleTimeoutException() {
            notifications.create(messageBundle.getMessage("backgroundWorkProgress.timeout"),
                            messageBundle.getMessage("backgroundWorkProgress.timeoutMessage"))
                    .show();

            return true;
        }

        protected void showResult(String resultMessage) {
            resultVBox.add(createTextArea(resultMessage));
            exportBtn.setEnabled(true);
        }
    }
}
