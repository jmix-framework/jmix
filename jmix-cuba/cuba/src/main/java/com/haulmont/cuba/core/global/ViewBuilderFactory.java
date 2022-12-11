/*
 * Copyright 2022 Haulmont.
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

package com.haulmont.cuba.core.global;

import io.jmix.core.Entity;
import io.jmix.core.FetchPlans;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @deprecated use only in legacy CUBA code. In new code, use {@link FetchPlans}.
 */
@Deprecated
@Component(ViewBuilderFactory.NAME)
@Primary
public class ViewBuilderFactory extends FetchPlans {

    public static final String NAME = "cuba_ViewBuilderFactory";

    @Override
    public ViewBuilder builder(Class<?> entityClass) {
        return new ViewBuilder(this, (Class<? extends Entity>) entityClass);
    }

}
