/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.formatters.impl.docx;

import io.jmix.reports.yarg.formatters.impl.AbstractFormatter;
import io.jmix.reports.yarg.formatters.impl.DocxFormatterDelegate;
import io.jmix.reports.yarg.formatters.impl.docx.TextMerger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;

import java.util.List;
import java.util.Set;

public abstract class AliasVisitor extends TraversalUtil.CallbackImpl {
    protected DocxFormatterDelegate docxFormatter;

    public AliasVisitor(DocxFormatterDelegate docxFormatter) {
        this.docxFormatter = docxFormatter;
    }

    @Override
    public List<Object> apply(Object o) {
        if (o instanceof P || o instanceof P.Hyperlink) {
            String paragraphText = docxFormatter.getElementText(o);

            if (AbstractFormatter.UNIVERSAL_ALIAS_PATTERN.matcher(paragraphText).find()) {
                Set<Text> mergedTexts = new TextMerger((ContentAccessor) o, AbstractFormatter.UNIVERSAL_ALIAS_REGEXP).mergeMatchedTexts();
                for (Text text : mergedTexts) {
                    handle(text);
                }
            }
        }

        return null;
    }

    protected abstract void handle(Text text);

    public void walkJAXBElements(Object parent) {
        List children = getChildren(parent);
        if (children != null) {

            for (Object object : children) {
                object = XmlUtils.unwrap(object);

                if (object instanceof Child && !(parent instanceof SdtBlock)) {
                    ((Child) object).setParent(parent);
                }

                this.apply(object);

                if (this.shouldTraverse(object)) {
                    walkJAXBElements(object);
                }
            }
        }
    }
}