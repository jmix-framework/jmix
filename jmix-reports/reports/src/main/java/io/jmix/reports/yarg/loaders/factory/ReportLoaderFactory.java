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

package io.jmix.reports.yarg.loaders.factory;


import io.jmix.reports.yarg.loaders.ReportDataLoader;

/**
 * This interface describes a factory which spawns data loaders. The default implementation is
 * io.jmix.reports.yarg.loaders.factory.DefaultLoaderFactory
 */
public interface ReportLoaderFactory {
    ReportDataLoader createDataLoader(String loaderType);
}