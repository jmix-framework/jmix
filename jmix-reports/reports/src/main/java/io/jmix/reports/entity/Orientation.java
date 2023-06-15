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
import com.haulmont.yarg.structure.BandOrientation;

public enum Orientation implements EnumClass<Integer> {
    HORIZONTAL(0, BandOrientation.HORIZONTAL),
    VERTICAL(1, BandOrientation.VERTICAL),
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
