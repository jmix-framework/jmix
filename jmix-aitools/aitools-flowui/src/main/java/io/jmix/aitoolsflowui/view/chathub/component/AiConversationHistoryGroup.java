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

package io.jmix.aitoolsflowui.view.chathub.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitools.entity.AiConversation;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * One date-bucket section of the conversation history: a header (label + count)
 * followed by the conversation cards built by the supplied factory.
 */
@NullMarked
public class AiConversationHistoryGroup extends Composite<VerticalLayout> {

    protected static final String BASE_CN = "chat-hub-history-group";
    protected static final String HEADER_CN = BASE_CN + "-header";
    protected static final String COUNT_CN = BASE_CN + "-count";
    protected static final String LABEL_CN = "chat-hub-history-bucket";

    @Override
    protected VerticalLayout initContent() {
        return createContent();
    }

    public void setGroup(String bucketLabel,
                         List<AiConversation> conversations,
                         Function<AiConversation, AiConversationCard> cardCreator) {
        getContent().removeAll();

        getContent().add(createHeader(bucketLabel, conversations.size()));

        conversations.stream()
                .map(cardCreator)
                .forEach(getContent()::add);
    }

    /**
     * Header row: bucket label (e.g. "TODAY") followed by the count badge.
     *
     * @param bucketLabel localized bucket label
     * @param count       number of conversations in the bucket
     * @return the assembled header row
     */
    protected HorizontalLayout createHeader(String bucketLabel, int count) {
        HorizontalLayout header = new HorizontalLayout(
                createBucketLabel(bucketLabel),
                createBucketCount(count)
        );
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(false);
        header.addClassName(HEADER_CN);
        return header;
    }

    protected Span createBucketLabel(String bucketLabel) {
        Span label = new Span(bucketLabel.toUpperCase(Locale.ROOT));
        label.addClassName(LABEL_CN);
        return label;
    }

    protected Span createBucketCount(int count) {
        Span countSpan = new Span(String.valueOf(count));
        countSpan.addClassName(COUNT_CN);
        return countSpan;
    }

    protected VerticalLayout createContent() {
        VerticalLayout root = new VerticalLayout();
        root.setPadding(false);
        root.setSpacing(false);
        root.setWidthFull();
        root.addClassName(BASE_CN);
        return root;
    }
}
