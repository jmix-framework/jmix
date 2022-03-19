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

package io.jmix.charts.model.export;

import io.jmix.charts.model.JsonEnum;

public enum PageSize implements JsonEnum {

    /**
     * Dimensions in pixel [4767.87, 6740.79]
     */
    X4A0("4A0"),

    /**
     * Dimensions in pixel [3370.39, 4767.87]
     */
    X2A0("2A0"),

    /**
     * Dimensions in pixel [2383.94, 3370.39]
     */
    A0("A0"),

    /**
     * Dimensions in pixel [1683.78, 2383.94]
     */
    A1("A1"),

    /**
     * Dimensions in pixel [1190.55, 1683.78]
     */
    A2("A2"),

    /**
     * Dimensions in pixel [841.89, 1190.55]
     */
    A3("A3"),

    /**
     * Dimensions in pixel [595.28, 841.89]
     */
    A4("A4"),

    /**
     * Dimensions in pixel [419.53, 595.28]
     */
    A5("A5"),

    /**
     * Dimensions in pixel [297.64, 419.53]
     */
    A6("A6"),

    /**
     * Dimensions in pixel [209.76, 297.64]
     */
    A7("A7"),

    /**
     * Dimensions in pixel [147.40, 209.76]
     */
    A8("A8"),

    /**
     * Dimensions in pixel [104.88, 147.40]
     */
    A9("A9"),

    /**
     * Dimensions in pixel [73.70, 104.88]
     */
    A10("A10"),

    /**
     * Dimensions in pixel [2834.65, 4008.19]
     */
    B0("B0"),

    /**
     * Dimensions in pixel [2004.09, 2834.65]
     */
    B1("B1"),

    /**
     * Dimensions in pixel [1417.32, 2004.09]
     */
    B2("B2"),

    /**
     * Dimensions in pixel [1000.63, 1417.32]
     */
    B3("B3"),

    /**
     * Dimensions in pixel [708.66, 1000.63]
     */
    B4("B4"),

    /**
     * Dimensions in pixel [498.90, 708.66]
     */
    B5("B5"),

    /**
     * Dimensions in pixel [354.33, 498.90]
     */
    B6("B6"),

    /**
     * Dimensions in pixel [249.45, 354.33]
     */
    B7("B7"),

    /**
     * Dimensions in pixel [175.75, 249.45]
     */
    B8("B8"),

    /**
     * Dimensions in pixel [175.75, 249.45]
     */
    B9("B9"),

    /**
     * Dimensions in pixel [124.72, 175.75]
     */
    B10("B10"),

    /**
     * Dimensions in pixel [2599.37, 3676.54]
     */
    C0("C0"),

    /**
     * Dimensions in pixel [1836.85, 2599.37]
     */
    C1("C1"),

    /**
     * Dimensions in pixel [1298.27, 1836.85]
     */
    C2("C2"),

    /**
     * Dimensions in pixel [918.43, 1298.27]
     */
    C3("C3"),

    /**
     * Dimensions in pixel [649.13, 918.43]
     */
    C4("C4"),

    /**
     * Dimensions in pixel [459.21, 649.13]
     */
    C5("C5"),

    /**
     * Dimensions in pixel [323.15, 459.21]
     */
    C6("C6"),

    /**
     * Dimensions in pixel [229.61, 323.15]
     */
    C7("C7"),

    /**
     * Dimensions in pixel [161.57, 229.61]
     */
    C8("C8"),

    /**
     * Dimensions in pixel [113.39, 161.57]
     */
    C9("C9"),

    /**
     * Dimensions in pixel [79.37, 113.39]
     */
    C10("C10"),

    /**
     * Dimensions in pixel [2437.80, 3458.27]
     */
    RA0("RA0"),

    /**
     * Dimensions in pixel [1729.13, 2437.80]
     */
    RA1("RA1"),

    /**
     * Dimensions in pixel [1218.90, 1729.13]
     */
    RA2("RA2"),

    /**
     * Dimensions in pixel [864.57, 1218.90]
     */
    RA3("RA3"),

    /**
     * Dimensions in pixel [609.45, 864.57]
     */
    RA4("RA4"),

    /**
     * Dimensions in pixel [2383.94, 3370.39]
     */
    SRA0("SRA0"),

    /**
     * Dimensions in pixel [1814.17, 2551.18]
     */
    SRA1("SRA1"),

    /**
     * Dimensions in pixel [1275.59, 1814.17]
     */
    SRA2("SRA2"),

    /**
     * Dimensions in pixel [907.09, 1275.59]
     */
    SRA3("SRA3"),

    /**
     * Dimensions in pixel [637.80, 907.09]
     */
    SRA4("SRA4"),

    /**
     * Dimensions in pixel [521.86, 756.00]
     */
    EXECUTIVE("EXECUTIVE"),

    /**
     * Dimensions in pixel [612.00, 936.00]
     */
    FOLIO("FOLIO"),

    /**
     * Dimensions in pixel [612.00, 1008.00]
     */
    LEGAL("LEGAL"),

    /**
     * Dimensions in pixel [612.00, 792.00]
     */
    LETTER("LETTER"),

    /**
     * Dimensions in pixel [792.00, 1224.00]
     */
    TABLOID("TABLOID");

    private String id;

    PageSize(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    public static PageSize valueFromId(String id) {
        for (PageSize pageSize : values()) {
            if (pageSize.getId().equals(id)) {
                return pageSize;
            }
        }
        throw new IllegalArgumentException(String.format("No enum constant's id '%s'", id));
    }
}