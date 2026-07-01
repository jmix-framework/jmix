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

package test_support.entity.cascade_operations;

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;

@JmixEntity(name = "test$JpaCascadeEmbeddable")
@Embeddable
public class JpaCascadeEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "BAR_INSIDE_ID")
    private JpaCascadeBar barInside;

    public JpaCascadeBar getBarInside() {
        return barInside;
    }

    public void setBarInside(JpaCascadeBar barInside) {
        this.barInside = barInside;
    }
}
