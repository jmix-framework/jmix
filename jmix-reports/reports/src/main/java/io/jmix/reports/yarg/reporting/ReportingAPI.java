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

package io.jmix.reports.yarg.reporting;


import java.io.OutputStream;

/**
 * This class is entry point for reporting functionality
 * Use it to print reports
 */
public interface ReportingAPI {

    /**
     * This method generates report and put result to output stream.
     * ! Attention : ReportOutputDocument.content field is null in this case !
     *
     * @param runParams - parameters for report printing
     * @param outputStream - the stream which accept binary file generated by reporting
     * @return ReportOutputDocument - object which describes generated report - its name, type etc
     */
    ReportOutputDocument runReport(RunParams runParams, OutputStream outputStream);

    /**
     * This method generates report and put result to ReportOutputDocument.content field
     * @param runParams - parameters for report printing
     * @return ReportOutputDocument - object which describes generated report - its name, type etc
     */
    ReportOutputDocument runReport(RunParams runParams);
}
