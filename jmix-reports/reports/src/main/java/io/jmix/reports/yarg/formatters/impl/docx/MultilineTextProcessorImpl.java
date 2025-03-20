/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.yarg.formatters.impl.docx;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Br;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

import java.util.ArrayList;
import java.util.List;

public class MultilineTextProcessorImpl implements MultilineTextProcessor {

    @Override
    public void process(Text text) {
        List<String> textLines = getTextLines(text);
        if (textLines.size() < 2) {
            return;
        }

        R parent = (R) text.getParent();
        List<Object> multiLineContent = new ArrayList<>();
        for (int i = 0; i < textLines.size(); i++) {
            String lineTextValue = textLines.get(i);
            Text lineText = createLineText(text, lineTextValue);

            multiLineContent.add(lineText);

            if (i < textLines.size() - 1) {
                multiLineContent.add(new Br());
            }
        }

        parent.getContent().remove(text);
        parent.getContent().addAll(multiLineContent);
    }

    @Override
    public void process(TextWrapper wrapper) {
        process(wrapper.text);
    }

    protected Text createLineText(Text sourceText, String lineTextValue) {
        Text text = XmlUtils.deepCopy(sourceText);
        text.setValue(lineTextValue);

        return text;
    }

    protected List<String> getTextLines(Text text) {
        String value = text.getValue();

        return value != null ? value.lines().toList() : List.of();
    }
}