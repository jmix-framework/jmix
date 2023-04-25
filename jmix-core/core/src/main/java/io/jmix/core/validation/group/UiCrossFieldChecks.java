/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.validation.group;

/**
 * Bean validation constraint group used by UI for cross-field validation. <br>
 * You can assign this group for constraints that must be checked only when instance is validated in UI editor. <br>
 * Cross field validation passes this group to {@link jakarta.validation.Validator#validate(Object, Class[])}
 * without {@link jakarta.validation.groups.Default} group.
 */
public interface UiCrossFieldChecks {
}