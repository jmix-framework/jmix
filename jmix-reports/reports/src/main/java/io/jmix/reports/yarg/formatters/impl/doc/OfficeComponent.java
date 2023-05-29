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
package io.jmix.reports.yarg.formatters.impl.doc;

import com.sun.star.lang.XComponent;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeResourceProvider;

public class OfficeComponent {

    private XComponent officeComponent;
    private OfficeResourceProvider officeResourceProvider;

    public OfficeComponent(OfficeResourceProvider officeResourceProvider, XComponent xComponent) {
        this.officeResourceProvider = officeResourceProvider;
        this.officeComponent = xComponent;
    }

    public OfficeResourceProvider getOfficeResourceProvider() {
        return officeResourceProvider;
    }

    public XComponent getOfficeComponent() {
        return officeComponent;
    }
}