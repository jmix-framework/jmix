/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsflowui.runner;

/**
 * Options that allow to show a dialog to input the report parameters or not.
 */
public enum ParametersDialogShowMode {

    /**
     * Show dialog to input the report parameters
     */
    YES,

    /**
     * Not to show dialog to input the report parameters
     */
    NO,

    /**
     * Show dialog to input the report parameters in the following cases:
     * <ul>
     *     <li>Report has input parameters</li>
     *     <li>Report has several templates</li>
     *     <li>Report has one template with alterable output type</li>
     * </ul>
     */
    IF_REQUIRED
}
