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
package io.jmix.reports.yarg.formatters.impl.inline;

import io.jmix.reports.yarg.exception.ReportFormattingException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Handle images with format string: ${image:100x100}
 */
public class ImageContentInliner extends AbstractInliner {
    private final static String REGULAR_EXPRESSION = "\\$\\{image:([0-9]+?)x([0-9]+?)\\}";

    public ImageContentInliner() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    public Pattern getTagPattern() {
        return tagPattern;
    }

    protected byte[] getContent(Object paramValue) {
        try {
            return IOUtils.toByteArray(new URL(paramValue.toString()).openStream());
        } catch (IOException e) {
            throw new ReportFormattingException("Unable to get image from " + paramValue, e);
        }
    }
}