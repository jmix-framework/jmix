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

package io.jmix.reports.util;

import com.haulmont.yarg.reporting.ReportOutputDocument;

import java.util.List;

/**
 * Interface contains utility methods for working with report zip archives
 */
public interface ReportZipUtils {

    /**
     * Method creates a zip archive containing files from {@code reportOutputDocuments}. If multiple documents declare
     * the same file name then file names inside the archive will be modified - an order number will be added to the
     * file name.
     *
     * @param reportOutputDocuments a collection of {@link ReportOutputDocument}
     * @return a byte array of zip archive containing files with report execution result
     */
    byte[] createZipArchive(List<ReportOutputDocument> reportOutputDocuments);
}
