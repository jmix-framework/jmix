/*
 * Copyright 2018 Haulmont
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
package io.jmix.reports.yarg.formatters.factory;

import io.jmix.reports.yarg.formatters.impl.pdf.HtmlToPdfConverter;
import io.jmix.reports.yarg.formatters.impl.pdf.ITextPdfConverter;
import io.jmix.reports.yarg.formatters.impl.pdf.OpenHtmlToPdfConverter;

public class HtmlToPdfConverterFactory {

    protected boolean openHtmlForPdfConversion;

    public boolean isOpenHtmlForPdfConversion() {
        return openHtmlForPdfConversion;
    }

    public void setOpenHtmlForPdfConversion(boolean openHtmlForPdfConversion) {
        this.openHtmlForPdfConversion = openHtmlForPdfConversion;
    }

    public HtmlToPdfConverter createHtmlToPdfConverter() {
        return openHtmlForPdfConversion ? new OpenHtmlToPdfConverter() : new ITextPdfConverter();
    }
}
