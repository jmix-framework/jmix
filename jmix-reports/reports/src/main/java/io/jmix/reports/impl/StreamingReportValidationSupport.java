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
package io.jmix.reports.impl;

import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.reporting.StreamingReportValidator;
import io.jmix.reports.yarg.structure.ReportBand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring facade over {@link StreamingReportValidator} bound to the application's loader factory.
 */
@Component("report_StreamingReportValidationSupport")
public class StreamingReportValidationSupport {

    protected final StreamingReportValidator validator = new StreamingReportValidator();

    @Autowired
    protected ReportLoaderFactory loaderFactory;

    public List<StreamingReportValidator.Violation> validate(ReportBand rootBand) {
        return validator.validate(rootBand, loaderFactory);
    }
}
