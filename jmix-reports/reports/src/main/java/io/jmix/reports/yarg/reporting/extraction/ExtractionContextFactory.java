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

package io.jmix.reports.yarg.reporting.extraction;

import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportBand;

import java.util.Map;

/**
 * This interface implementation should create immutable extraction context object
 *
 * <p>The default implementation is <b>io.jmix.reports.yarg.reporting.extraction.DefaultExtractionContextFactory</b></p>
 */
public interface ExtractionContextFactory {
    /**
     * Method should always return new <b>immutable</b> context object
     */
    ExtractionContext context(ReportBand band, BandData parentBand, Map<String, Object> params);
}
