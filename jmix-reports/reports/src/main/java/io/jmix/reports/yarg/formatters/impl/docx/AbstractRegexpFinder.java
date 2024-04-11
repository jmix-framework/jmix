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

import io.jmix.reports.yarg.formatters.impl.DocxFormatterDelegate;
import org.docx4j.TraversalUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class AbstractRegexpFinder<T> extends TraversalUtil.CallbackImpl {
    protected DocxFormatterDelegate docxFormatter;
    protected Class<T> classToHandle;
    protected Pattern regularExpression;

    public AbstractRegexpFinder(DocxFormatterDelegate docxFormatter, Pattern regularExpression, Class<T> classToHandle) {
        this.docxFormatter = docxFormatter;
        this.regularExpression = regularExpression;
        this.classToHandle = classToHandle;
    }

    @Override
    public List<Object> apply(Object o) {
        if (skipFind()) {
            return null;
        }
        if (classToHandle.isAssignableFrom(o.getClass())) {
            @SuppressWarnings("unchecked")
            T currentElement = (T) o;
            String currentElementText = docxFormatter.getElementText(currentElement);
            if (isNotBlank(currentElementText)) {
                Matcher matcher = regularExpression.matcher(currentElementText);
                if (matcher.find()) {
                    onFind(currentElement, matcher);
                }
            }
        }

        return null;
    }

    protected abstract void onFind(T o, Matcher matcher);

    protected boolean skipFind() {
        return false;
    }
}
