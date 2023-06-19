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

package io.jmix.reports.yarg.formatters.impl.xlsx;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.jmix.reports.yarg.structure.BandData;

public class BandsForRanges {
    protected BiMap<BandData, Range> bandsToTemplateRanges = HashBiMap.create();
    protected BiMap<BandData, Range> bandsToResultRanges = HashBiMap.create();

    public void add(BandData bandData, Range template, Range result) {
        bandsToTemplateRanges.forcePut(bandData, template);
        bandsToResultRanges.forcePut(bandData, result);
    }

    public BandData bandForResultRange(Range result) {
        return bandsToResultRanges.inverse().get(result);
    }

    public Range resultForBand(BandData bandData) {
        return bandsToResultRanges.get(bandData);
    }

    public Range templateForBand(BandData bandData) {
        return bandsToTemplateRanges.get(bandData);
    }
}