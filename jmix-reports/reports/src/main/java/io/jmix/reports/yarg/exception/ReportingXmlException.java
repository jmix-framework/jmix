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
package io.jmix.reports.yarg.exception;

/**
 * Thrown when report unmarshalling from xml or report validation by xsd fails
 */
public class ReportingXmlException extends ReportingException {
    public ReportingXmlException() {
    }

    public ReportingXmlException(String message) {
        super(message);
    }

    public ReportingXmlException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportingXmlException(Throwable cause) {
        super(cause);
    }
}
