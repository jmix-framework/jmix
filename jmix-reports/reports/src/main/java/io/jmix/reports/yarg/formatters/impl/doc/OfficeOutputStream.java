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
package io.jmix.reports.yarg.formatters.impl.doc;

import com.sun.star.io.XOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Proxy stream
 */
public class OfficeOutputStream extends OutputStream implements XOutputStream {

    private OutputStream outputStream;

    public OfficeOutputStream(OutputStream outputStream) {
        if (outputStream == null)
            throw new NullPointerException();

        this.outputStream = outputStream;
    }

    @Override
    public void write(int b) throws IOException {
        this.outputStream.write(b);
    }

    public void writeBytes(byte[] values) throws com.sun.star.io.IOException {
        try {
            this.outputStream.write(values);
        } catch (IOException e) {
            throw (new com.sun.star.io.IOException(e.getMessage()));
        }
    }

    public void closeOutput() throws com.sun.star.io.IOException {
        try {
            this.outputStream.flush();
            this.outputStream.close();
        } catch (IOException e) {
            throw (new com.sun.star.io.IOException(e.getMessage()));
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.outputStream.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.outputStream.write(b);
    }

    @Override
    public void flush() {
        try {
            this.outputStream.flush();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void close() throws IOException {
        this.outputStream.close();
    }
}