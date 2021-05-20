/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.runner;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;

import java.util.Map;

/**
 * Provides methods for configuring a single "run report" operation and running the report.
 *
 * Instances of this interface must be obtained by the {@link io.jmix.reports.runner.ReportRunners}.
 */
public interface ReportRunner {

    /**
     * Sets report parameters
     */
    ReportRunner withParams(Map<String, Object> params);

    /**
     * Adds a single report parameter
     */
    ReportRunner addParam(String name, Object value);

    /**
     * Sets the report template code that will be used for report generation
     */
    ReportRunner withTemplateCode(String templateCode);

    /**
     * Sets the report template instance that will be used for report generation
     */
    ReportRunner withTemplate(ReportTemplate template);

    /**
     * Sets the report output type
     */
    ReportRunner withOutputType(ReportOutputType outputType);

    /**
     * Sets the output name pattern
     */
    ReportRunner withOutputNamePattern(String outputNamePattern);

    /**
     * Runs the report and returns the generated {@link ReportOutputDocument}
     */
    ReportOutputDocument run();
}
