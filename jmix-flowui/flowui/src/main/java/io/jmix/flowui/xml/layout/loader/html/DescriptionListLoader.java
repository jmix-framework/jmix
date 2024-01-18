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

package io.jmix.flowui.xml.layout.loader.html;

import com.vaadin.flow.component.html.DescriptionList;

public class DescriptionListLoader extends AbstractHtmlContainerLoader<DescriptionList> {

    @Override
    protected DescriptionList createComponent() {
        return factory.create(DescriptionList.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        componentLoader().loadClickNotifierAttributes(resultComponent, element);
    }

    public static class TermLoader extends AbstractHtmlContainerLoader<DescriptionList.Term> {

        @Override
        protected DescriptionList.Term createComponent() {
            return factory.create(DescriptionList.Term.class);
        }

        @Override
        public void loadComponent() {
            super.loadComponent();

            componentLoader().loadClickNotifierAttributes(resultComponent, element);
        }
    }

    public static class DescriptionLoader extends AbstractHtmlContainerLoader<DescriptionList.Description> {

        @Override
        protected DescriptionList.Description createComponent() {
            return factory.create(DescriptionList.Description.class);
        }

        @Override
        public void loadComponent() {
            super.loadComponent();

            componentLoader().loadClickNotifierAttributes(resultComponent, element);
        }
    }
}
