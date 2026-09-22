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

// A test source that calls a main top-level declaration. It compiles against the enhanced main
// output dir (sourceSets.main.output is redirected there), so it fails with "Unresolved reference"
// unless the enhanced dir carries the Kotlin module metadata.
object AppEntityUsage {
    fun run(): String = AppEntity().describe()
}
