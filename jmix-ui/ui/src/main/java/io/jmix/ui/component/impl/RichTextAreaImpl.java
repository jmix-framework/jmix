/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component.impl;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.ui.component.RichTextArea;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.widget.JmixRichTextArea;
import io.jmix.ui.widget.client.richtextarea.JmixRichTextAreaState;
import com.vaadin.shared.ui.ValueChangeMode;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

public class RichTextAreaImpl extends AbstractField<JmixRichTextArea, String, String>
        implements RichTextArea, InitializingBean {

    protected static final String LINE_BREAK_TAG = "<br>";
    protected static final String CLOSED_LINE_BREAK_TAG = "<br />";

    public RichTextAreaImpl() {
        component = createComponent();

        attachValueChangeListener(this.component);
        setHtmlSanitizerEnabled(false);
    }

    protected JmixRichTextArea createComponent() {
        return new JmixRichTextArea();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent(component);
    }

    protected void initComponent(JmixRichTextArea component) {
        component.setValueChangeMode(ValueChangeMode.BLUR);
    }

    @Autowired
    public void setMessages(Messages messages) {
        component.setLocaleMap(loadLabels(messages));
    }

    protected Map<String, String> loadLabels(Messages messages) {
        Map<String, String> labels = new HashMap<>();

        Collection<String> locales = Arrays.asList(
                JmixRichTextAreaState.RICH_TEXT_AREA_FOREGROUND_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_BACKGROUND_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_BLACK_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_WHITE_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_RED_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_GREEN_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_YELLOW_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_BLUE_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_FONT_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_NORMAL_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_SIZE_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_BOLD_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_ITALIC_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_UNDERLINE_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_SUBSCRIPT_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_SUPERSCRIPT_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_JUSTIFYCENTER_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_JUSTIFYRIGHT_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_JUSTIFYLEFT_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_STRIKETHROUGH_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_INDENT_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_OUTDENT_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_HR_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_OL_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_UL_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_INSERTIMAGE_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_CREATELINK_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_REMOVELINK_LABEL,
                JmixRichTextAreaState.RICH_TEXT_AREA_REMOVEFORMAT_LABEL
        );

        for (String locale : locales) {
            labels.put(locale, messages.getMessage(locale));
        }

        return labels;
    }

    @Override
    protected String convertToPresentation(@Nullable String modelValue) throws ConversionException {
        String presentationValue = nullToEmpty(super.convertToPresentation(modelValue));
        presentationValue = sanitize(presentationValue);
        return presentationValue;
    }

    @Nullable
    @Override
    protected String convertToModel(@Nullable String componentRawValue) throws ConversionException {
        String modelValue = sanitize(componentRawValue);
        modelValue = emptyToNull(modelValue);
        return super.convertToModel(modelValue);
    }

    @Override
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(getValue());
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public void commit() {
        super.commit();
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isBuffered() {
        return super.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        super.setBuffered(buffered);
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }

    @Nullable
    @Override
    protected String sanitize(@Nullable String html) {
        if (!isHtmlSanitizerEnabled() || component.isLastUserActionSanitized()) {
            return html;
        }

        String sanitizedValue = getHtmlSanitizer().sanitize(html);
        return postSanitize(sanitizedValue);
    }

    /**
     * Hook to replace the {@code <br />} with {@code <br>} in the sanitized string of HTML, since the RichTextArea
     * component does not support {@code <br />}.
     *
     * @param html the sanitized string of HTML
     * @return a post-processed sanitized string of HTML
     */
    @Nullable
    protected String postSanitize(@Nullable String html) {
        return html != null
                ? html.replaceAll(CLOSED_LINE_BREAK_TAG, LINE_BREAK_TAG)
                : null;
    }
}
