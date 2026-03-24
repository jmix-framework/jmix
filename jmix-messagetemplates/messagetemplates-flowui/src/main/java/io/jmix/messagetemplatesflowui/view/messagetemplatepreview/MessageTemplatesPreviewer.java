/*
 * Copyright 2026 Haulmont.
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

package io.jmix.messagetemplatesflowui.view.messagetemplatepreview;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.messagetemplates.MessageTemplatesGenerator;
import io.jmix.messagetemplates.entity.MessageTemplate;
import io.jmix.messagetemplatesflowui.view.parametersinputdialog.MessageTemplateParametersInputDialog;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Provides functionality for previewing {@link MessageTemplate}.
 * <p>
 * Allows users to enter parameters used for generating a message preview
 * and displays the result in a dialog.
 * <p>
 * This is an internal component and is not intended for direct use in application code.
 */
@Internal
@Component("msgtmp_MessageTemplatesPreviewer")
public class MessageTemplatesPreviewer {

    @Autowired
    protected MessageTemplatesGenerator messageTemplatesGenerator;
    @Autowired
    protected DialogWindows dialogWindows;

    /**
     * Opens a preview for the specified message template.
     * <p>
     * If the template contains parameters, a dialog for parameter input is shown first.
     * After confirmation, the template is generated and the result is displayed.
     *
     * @param messageTemplate the message template (can be {@code null})
     * @param origin          the originating view used to open dialogs
     */
    public void showPreview(@Nullable MessageTemplate messageTemplate, View<?> origin) {
        if (messageTemplate == null) {
            return;
        }

        if (!messageTemplate.getParameters().isEmpty()) {
            dialogWindows.view(origin, MessageTemplateParametersInputDialog.class)
                    .withViewConfigurer(view ->
                            view.setTemplateParameters(messageTemplate.getParameters()))
                    .withAfterCloseListener(event -> {
                        if (event.closedWith(StandardOutcome.SAVE)) {
                            showTemplate(messageTemplate, event.getView().getParameters(), origin);
                        }
                    })
                    .open();
        } else {
            showTemplate(messageTemplate, Collections.emptyMap(), origin);
        }
    }

    /**
     * Generates the message template using the provided parameters
     * and displays the result in the preview dialog.
     *
     * @param template   the message template
     * @param parameters parameters used for generation
     * @param origin     the originating view
     */
    protected void showTemplate(MessageTemplate template, Map<String, Object> parameters, View<?> origin) {
        String content = messageTemplatesGenerator.generateMessage(template, parameters);

        dialogWindows.view(origin, MessageTemplatePreviewView.class)
                .withViewConfigurer(view -> view.setPreviewContent(template.getType(), content))
                .open();
    }
}
