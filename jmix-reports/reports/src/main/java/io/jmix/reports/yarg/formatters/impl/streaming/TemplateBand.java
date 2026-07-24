/*
 * Copyright 2026 Haulmont.
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
package io.jmix.reports.yarg.formatters.impl.streaming;

/**
 * A band blueprint parsed from a workbook named range (the range name equals the band name).
 * Coordinates are 0-based and inclusive.
 */
public class TemplateBand {

    public final String name;
    public final int firstRow;
    public final int lastRow;
    public final int firstCol;
    public final int lastCol;

    public TemplateBand(String name, int firstRow, int lastRow, int firstCol, int lastCol) {
        this.name = name;
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.firstCol = firstCol;
        this.lastCol = lastCol;
    }
}
