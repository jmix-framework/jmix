/*
 * Copyright 2026 Haulmont.
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

package sample.app

// Top-level Kotlin declaration (an extension function). Such declarations live in a synthetic
// file facade class and are discoverable by the Kotlin compiler only via the module metadata
// (META-INF/*.kotlin_module). Used from a test source to prove that metadata survives enhancement.
fun AppEntity.describe(): String = "AppEntity[$id]"
