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

package com.haulmont.cuba.gui.xml;

import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import io.jmix.ui.component.Table;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.xml.DeclarativeColumnGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * @see Table.Column#setColumnGenerator(Function)
 * @deprecated Use installing instance from the controller instead.
 */
@Deprecated
@Primary
@Component("cuba_CubaDeclarativeColumnGenerator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CubaDeclarativeColumnGenerator extends DeclarativeColumnGenerator {

    public CubaDeclarativeColumnGenerator(Table table, String methodName) {
        super(table, methodName);
    }

    @Override
    protected FrameOwner getFrameOwner() {
        FrameOwner controller = super.getFrameOwner();
        return controller instanceof LegacyFragmentAdapter
                ? ((LegacyFragmentAdapter) controller).getRealScreen()
                : controller;
    }
}
