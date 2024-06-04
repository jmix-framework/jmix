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

package io.jmix.quartz.exception;

public class QuartzJobSaveException extends RuntimeException  {
    public QuartzJobSaveException() {
    }

    public QuartzJobSaveException(String msg) {
        super(msg);
    }

    public QuartzJobSaveException(Throwable cause) {
        super(cause);
    }

    public QuartzJobSaveException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public Throwable getUnderlyingException() {
        return super.getCause();
    }

    public String toString() {
        Throwable cause = this.getUnderlyingException();
        return cause != null && cause != this ? super.toString() + " [See nested exception: " + cause + "]" : super.toString();
    }
}
