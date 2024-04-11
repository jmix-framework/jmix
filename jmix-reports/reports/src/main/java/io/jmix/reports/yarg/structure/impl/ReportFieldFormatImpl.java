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

package io.jmix.reports.yarg.structure.impl;

import com.google.common.base.Preconditions;
import io.jmix.reports.yarg.structure.ReportFieldFormat;

public class ReportFieldFormatImpl implements ReportFieldFormat {
    protected String name;
    protected String format;
    protected Boolean groovyScript;

    public ReportFieldFormatImpl(String name, String format) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(format, "\"format\" parameter can not be null");

        this.name = name;
        this.format = format;
        this.groovyScript = false;
    }

    public ReportFieldFormatImpl(String name, String format, Boolean groovyScript) {
        this(name, format);
        Preconditions.checkNotNull(groovyScript, "\"groovyScript\" parameter can not be null");
        this.groovyScript = groovyScript;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public Boolean isGroovyScript() {
        return groovyScript;
    }
}
