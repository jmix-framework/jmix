/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.fragment;

import io.jmix.flowui.view.View;

/**
 * Marker interface for classes that can be parents of fragments,
 * i.e. can provide fragments with additional data that is needed
 * for their correct initialization. For example, provided data
 * components.
 *
 * @see View
 * @see Fragment
 */
public interface FragmentOwner {
}
