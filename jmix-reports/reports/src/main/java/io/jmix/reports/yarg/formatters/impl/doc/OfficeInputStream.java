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

import com.sun.star.io.XInputStream;
import com.sun.star.io.XSeekable;

import java.io.ByteArrayInputStream;

/**
 * Implementation for XInputStream
 */
public class OfficeInputStream extends ByteArrayInputStream implements XInputStream, XSeekable {

    public OfficeInputStream(byte[] buf) {
        super(buf);
    }

    public int readBytes(byte[][] buffer, int bufferSize) throws com.sun.star.io.IOException {
        int numberOfReadBytes;
        try {
            byte[] bytes = new byte[bufferSize];
            numberOfReadBytes = super.read(bytes);
            if(numberOfReadBytes > 0) {
                if(numberOfReadBytes < bufferSize) {
                    byte[] smallerBuffer = new byte[numberOfReadBytes];
                    System.arraycopy(bytes, 0, smallerBuffer, 0, numberOfReadBytes);
                    bytes = smallerBuffer;
                }
            }
            else {
                bytes = new byte[0];
                numberOfReadBytes = 0;
            }
            buffer[0]=bytes;
            return numberOfReadBytes;
        }
        catch (java.io.IOException e) {
            throw new com.sun.star.io.IOException(e.getMessage(),this);
        }
    }

    public int readSomeBytes(byte[][] buffer, int bufferSize) throws com.sun.star.io.IOException {
        return readBytes(buffer, bufferSize);
    }

    public void skipBytes(int skipLength) {
        skip(skipLength);
    }

    public void closeInput() throws com.sun.star.io.IOException {
        try {
            close();
        }
        catch (java.io.IOException e) {
            throw new com.sun.star.io.IOException(e.getMessage(), this);
        }
    }

    public long getLength() {
        return count;
    }

    public long getPosition() {
        return pos;
    }

    public void seek(long position) throws IllegalArgumentException {
        pos = (int) position;
    }
}