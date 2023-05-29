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
import io.jmix.reports.yarg.formatters.impl.docx.AbstractRegexpFinder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpFinder<T> extends AbstractRegexpFinder<T> {
    protected String value;

    public RegexpFinder(DocxFormatterDelegate docxFormatter, Pattern regularExpression, Class<T> classToHandle) {
        super(docxFormatter, regularExpression, classToHandle);
    }

    protected void onFind(T o, Matcher matcher) {
        if (value == null) {
            value = matcher.group(0);
        }
    }

    @Override
    protected boolean skipFind() {
        return value != null;
    }

    public String getValue() {
        return value;
    }
}
