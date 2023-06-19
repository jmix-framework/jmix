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
 * Thrown when user choose data loader not supported in data loader factory
 */
public class UnsupportedLoaderException extends ReportingException {
    public UnsupportedLoaderException() {
    }

    public UnsupportedLoaderException(String message) {
        super(message);
    }

    public UnsupportedLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedLoaderException(Throwable cause) {
        super(cause);
    }
}
