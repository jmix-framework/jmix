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

package test_support.entity.sales;

import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.TestBaseEntity;

import jakarta.persistence.*;

/**
 * Fourth level of the {@code Order > OrderLine > OrderLineParam > OrderLineParamNote} composition chain.
 */
@Table(name = "TEST_ORDER_LINE_PARAM_NOTE")
@Entity(name = "test_OrderLineParamNote")
@JmixEntity
public class OrderLineParamNote extends TestBaseEntity {
    private static final long serialVersionUID = -3902139517462913820L;

    @Column(name = "TEXT_")
    protected String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARAM_ID")
    protected OrderLineParam param;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OrderLineParam getParam() {
        return param;
    }

    public void setParam(OrderLineParam param) {
        this.param = param;
    }
}
