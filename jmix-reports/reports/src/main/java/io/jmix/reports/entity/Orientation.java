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
package io.jmix.reports.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.reports.yarg.structure.BandOrientation;

/**
 * Band orientation. Relevant only for spreadsheet-like output types (XLS, XLSX).
 * Determines direction where data entries are copied on the sheet.
 */
public enum Orientation implements EnumClass<Integer> {
    /**
     * Band entries are copied downwards, may contain sub-bands.
     */
    HORIZONTAL(0, BandOrientation.HORIZONTAL),
    /**
     * Band entries are copied to the right.
     */
    VERTICAL(1, BandOrientation.VERTICAL),
    /**
     * Band entries are copied to the right and downwards as a matrix.
     * The band must contain three datasets:
     * <li><code>${band_name}_dynamic_header</code> - the data from this dataset is copied to the right like a vertical band with table columns header</li>
     * <li><code>${band_name}_master_data</code> - the data from this dataset is copied downwards like a horizontal band with table rows header</li>
     * <li><code>${band_name}</code> - the dataset named the same as the band, it belongs to; it is the main content band that will fulfill the matrix cells.</li>
     */
    CROSS(2, BandOrientation.CROSS);

    private Integer id;
    private BandOrientation bandOrientation;

    Orientation(Integer id, BandOrientation bandOrientation) {
        this.id = id;
        this.bandOrientation = bandOrientation;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public BandOrientation getBandOrientation() {
        return bandOrientation;
    }

    public static Orientation fromId(Integer id) {
        for (Orientation type : Orientation.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    public static Orientation fromBandOrientation(BandOrientation orientation) {
        for (Orientation type : Orientation.values()) {
            if (type.getBandOrientation() == orientation) {
                return type;
            }
        }
        return null;
    }
}
