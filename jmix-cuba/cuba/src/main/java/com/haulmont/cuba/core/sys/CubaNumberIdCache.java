/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.entity.annotation.IdSequence;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.impl.NumberIdCache;

import java.util.Map;

public class CubaNumberIdCache extends NumberIdCache {

    @Override
    protected SequenceParams getSequenceParams(MetaClass metaClass) {
        SequenceParams sequenceParams = super.getSequenceParams(metaClass);
        if (sequenceParams.name == null) {
            Map attributes = (Map) metaClass.getAnnotations().get(IdSequence.class.getName());
            if (attributes != null) {
                return new SequenceParams((String) attributes.get("name"), Boolean.TRUE.equals(attributes.get("cached")));
            }
        }
        return sequenceParams;
    }
}
