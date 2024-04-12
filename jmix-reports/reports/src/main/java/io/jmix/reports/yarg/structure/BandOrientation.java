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
package io.jmix.reports.yarg.structure;

public enum BandOrientation {
    HORIZONTAL("H"),
    VERTICAL("V"),
    CROSS("C"),
    UNDEFINED("U"),
    ;

    BandOrientation(String id) {
        this.id = id;
    }

    public final String id;

    public static BandOrientation fromId(String id) {
        for (BandOrientation orientation : values()) {
            if (orientation.id.equals(id)) {
                return orientation;
            }
        }

        return UNDEFINED;
    }

    public static BandOrientation defaultIfNull(BandOrientation orientation) {
        return orientation == null ? UNDEFINED : orientation;
    }
}
