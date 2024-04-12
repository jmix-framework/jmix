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

package io.jmix.reports.yarg.structure;

/**
 * This interface used for proxy wrapping
 *
 * <p>ex: io.jmix.reports.yarg.reporting.extraction.preprocessor.SqlCrosstabPreprocessor#preprocess</p>
 */
public interface ProxyWrapper {
    /**
     * Internal.
     * Use {@link ProxyWrapper#unwrap(Object)} to unwrap proxied instance
     */
    Object unwrap();

    /**
     * Method checks instance inheritance of ProxyWrapper and unwrapping their real instance
     *
     * @param obj - instance of any object
     * @return unwrapped instance (if proxied) or same object
     */
    static<T> T unwrap(T obj) {
        return obj != null && obj instanceof ProxyWrapper ? (T)((ProxyWrapper)obj).unwrap() : obj;
    }
}
