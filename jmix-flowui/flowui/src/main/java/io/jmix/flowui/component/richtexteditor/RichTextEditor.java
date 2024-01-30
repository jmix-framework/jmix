/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.component.richtexteditor;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.flowui.component.delegate.RichTextEditorDelegate;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.kit.component.richtexteditor.JmixRichTextEditor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

public class RichTextEditor extends JmixRichTextEditor
        implements SupportsValueSource<String>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected RichTextEditorDelegate<RichTextEditor, String> fieldDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        fieldDelegate = createFieldDelegate();
        applyLocalization();
    }

    protected void applyLocalization() {
        Messages messages = applicationContext.getBean(Messages.class);

        RichTextEditorI18n i18n = new RichTextEditorI18n();
        i18n.setBold(messages.getMessage("richTextEditor.i18n.bold"));
        i18n.setItalic(messages.getMessage("richTextEditor.i18n.italic"));
        i18n.setUnderline(messages.getMessage("richTextEditor.i18n.underline"));
        i18n.setStrike(messages.getMessage("richTextEditor.i18n.strike"));

        i18n.setH1(messages.getMessage("richTextEditor.i18n.h1"));
        i18n.setH2(messages.getMessage("richTextEditor.i18n.h2"));
        i18n.setH3(messages.getMessage("richTextEditor.i18n.h3"));

        i18n.setSubscript(messages.getMessage("richTextEditor.i18n.subscript"));
        i18n.setSuperscript(messages.getMessage("richTextEditor.i18n.superscript"));

        i18n.setListOrdered(messages.getMessage("richTextEditor.i18n.listOrdered"));
        i18n.setListBullet(messages.getMessage("richTextEditor.i18n.listBullet"));

        i18n.setAlignStart(messages.getMessage("richTextEditor.i18n.alignStart"));
        i18n.setAlignCenter(messages.getMessage("richTextEditor.i18n.alignCenter"));
        i18n.setAlignEnd(messages.getMessage("richTextEditor.i18n.alignEnd"));
        i18n.setAlignJustify(messages.getMessage("richTextEditor.i18n.alignJustify"));

        i18n.setImage(messages.getMessage("richTextEditor.i18n.image"));
        i18n.setLink(messages.getMessage("richTextEditor.i18n.link"));

        i18n.setBlockquote(messages.getMessage("richTextEditor.i18n.blockquote"));
        i18n.setCodeBlock(messages.getMessage("richTextEditor.i18n.codeBlock"));

        i18n.setClean(messages.getMessage("richTextEditor.i18n.clean"));

        setI18n(i18n);
    }

    protected RichTextEditorDelegate<RichTextEditor, String> createFieldDelegate() {
        //noinspection unchecked
        return applicationContext.getBean(RichTextEditorDelegate.class, this);
    }

    @Nullable
    @Override
    public ValueSource<String> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<String> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public void setValue(String value) {
        super.setValue(Strings.nullToEmpty(value));
    }
}
