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

package io.jmix.reports.yarg.util.properties;

import java.io.IOException;
import java.util.Properties;

/**
 * This interface describes logic which load properties for application
 */
public interface PropertiesLoader {
    String CUBA_REPORTING_SQL_DRIVER = "cuba.reporting.sql.driver";
    String CUBA_REPORTING_SQL_DB_URL = "cuba.reporting.sql.dbUrl";
    String CUBA_REPORTING_SQL_USER = "cuba.reporting.sql.user";
    String CUBA_REPORTING_SQL_PASSWORD = "cuba.reporting.sql.password";
    String CUBA_REPORTING_OPENOFFICE_PATH = "cuba.reporting.openoffice.path";
    String CUBA_REPORTING_OPENOFFICE_PORTS = "cuba.reporting.openoffice.ports";
    String CUBA_REPORTING_OPENOFFICE_DISPLAY_DEVICE_AVAILABLE = "cuba.reporting.openoffice.displayDeviceAvailable";
    String CUBA_REPORTING_OPENOFFICE_TIMEOUT = "cuba.reporting.openoffice.timeout";
    String CUBA_REPORTING_PUT_EMPTY_ROW_IF_NO_DATA_SELECTED = "cuba.reporting.dataextractor.putEmptyRowIfNoDataSelected";
    String CUBA_REPORTING_FONTS_DIRECTORY = "cuba.reporting.fontsDirectory";
    String CUBA_REPORTING_OPEN_HTML_FOR_PDF_CONVERSION = "cuba.reporting.openHtmlForPdfConversion";
    String CUBA_REPORTING_FORMULAS_POST_PROCESSING_EVALUATION_ENABLED = "cuba.reporting.formulasPostProcessingEvaluationEnabled";

    Properties load() throws IOException;
}
