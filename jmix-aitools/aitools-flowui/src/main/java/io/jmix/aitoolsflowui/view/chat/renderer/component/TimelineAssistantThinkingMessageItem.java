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

package io.jmix.aitoolsflowui.view.chat.renderer.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitools.tool.AiUiStatusUpdate;
import io.jmix.aitoolsflowui.model.TimelineItem;
import io.jmix.aitoolsflowui.model.TimelineItemStatus;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * Row shown while the assistant is preparing a response. Displays a shimmer
 * indicator, the latest live status, and a history of prior steps delivered
 * by AI tools through
 * {@link io.jmix.aitools.tool.AiToolStatusPublisher}.
 * <p>
 * Each {@link AiUiStatusUpdate} carries a {@code message} and an optional
 * {@code resultSnippet}; {@link AiUiStatusUpdate#isCompleted()} is true when
 * the snippet is non-blank. Completed past steps are prefixed with a
 * {@code "✓ "} check mark and their snippet is rendered next to the base
 * text; in-flight past steps render plain (no check, no snippet).
 */
public class TimelineAssistantThinkingMessageItem extends AbstractTimelineItem implements InitializingBean,
        ApplicationContextAware {

    public static final String BASE_CN = "timeline-message-row-assistant";

    public static final String THINKING_CN = "timeline-message-row-thinking";
    public static final String THINKING_STATUS_CN = "timeline-thinking-status-base";
    public static final String THINKING_STATUS_RESULT_CN = "timeline-thinking-status-result";

    public static final String THINKING_TEXT_CN = "timeline-thinking-text";
    public static final String THINKING_SHIMMER_CN = "timeline-thinking-shimmer";
    public static final String THINKING_STATUS_LIST_CN = "timeline-thinking-status-list";
    public static final String THINKING_STATUS_LIST_ITEM_CN = "timeline-thinking-status-item";

    protected ApplicationContext applicationContext;
    protected Metadata metadata;
    protected Messages messages;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        metadata = applicationContext.getBean(Metadata.class);
        messages = applicationContext.getBean(Messages.class);

        initComponent();
    }

    protected void initComponent() {
        addClassNames(BASE_CN, THINKING_CN);
    }

    public void setThinking(TimelineItem item,
                            String assistantName,
                            String defaultThinkingIndicatorText) {
        List<TimelineItemStatus> statusUpdates = item.getStatusUpdates() != null
                ? item.getStatusUpdates()
                : List.of();

        initRow(assistantName);

        Span thinkingText = buildStatusSpan(resolveActiveStatus(statusUpdates, defaultThinkingIndicatorText));

        Div shimmer = new Div();
        shimmer.addClassName(THINKING_SHIMMER_CN);

        body.add(thinkingText, shimmer);
        if (statusUpdates.size() > 1) {
            body.add(createThinkingStatusList(statusUpdates));
        }
    }

    private Component createThinkingStatusList(List<TimelineItemStatus> statusUpdates) {
        VerticalLayout statusList = new VerticalLayout();
        statusList.setPadding(false);
        statusList.setSpacing(false);
        statusList.addClassName(THINKING_STATUS_LIST_CN);

        // Walk older updates from newest-but-one (size - 2) down to oldest (0);
        // the very last item is shown separately as the active status, so we
        // skip it here. Each item's visual shape (check-mark + snippet) is
        // decided via isCompleted().
        for (int i = statusUpdates.size() - 2; i >= 0; i--) {
            TimelineItemStatus update = statusUpdates.get(i);
            statusList.add(buildStatusSpan(update, THINKING_STATUS_LIST_ITEM_CN, true));
        }

        return statusList;
    }

    private Span buildStatusSpan(TimelineItemStatus update) {
        return buildStatusSpan(update, THINKING_TEXT_CN, false);
    }

    private Span buildStatusSpan(TimelineItemStatus update, String mainClass, boolean completedPrefix) {
        Span container = new Span();
        container.addClassName(mainClass);

        String prefix = completedPrefix && update.isCompleted()
                ? messages.getMessage("TimelineAssistantThinkingMessageItem.completed")
                : "";
        String baseText = prefix.isEmpty()
                ? update.getMessage()
                : prefix + " " + update.getMessage();

        Span baseTextSpan = new Span(baseText);
        baseTextSpan.addClassName(THINKING_STATUS_CN);
        container.add(baseTextSpan);

        if (update.isCompleted()) {
            Span resultText = new Span(" " + update.getResultSnippet());
            resultText.addClassName(THINKING_STATUS_RESULT_CN);
            container.add(resultText);
        }
        return container;
    }

    private TimelineItemStatus resolveActiveStatus(List<TimelineItemStatus> statusUpdates, String defaultThinkingIndicatorText) {
        if (statusUpdates.isEmpty()) {
            TimelineItemStatus timelineStatus = metadata.create(TimelineItemStatus.class);
            timelineStatus.setMessage(defaultThinkingIndicatorText);
            return timelineStatus;
        }
        return statusUpdates.get(statusUpdates.size() - 1);
    }

    @Override
    protected Component createAvatar(String actorName) {
        return new AssistantAvatar();
    }
}
