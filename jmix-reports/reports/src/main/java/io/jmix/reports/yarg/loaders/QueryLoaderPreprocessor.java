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

package io.jmix.reports.yarg.loaders;

import io.jmix.reports.yarg.structure.ReportQuery;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * The interface implementation should provides custom query preprocessing before data loading
 */
@FunctionalInterface
public interface QueryLoaderPreprocessor {

     List<Map<String, Object>> preprocess(ReportQuery query, Map<String, Object> params,
                                          BiFunction<ReportQuery, Map<String, Object>, List<Map<String, Object>>> consumer);
}
