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

package io.jmix.core.impl;

import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * The serialization implementation using standard Java serialization and allowing beans to be restored on deserialization
 */
@Component("core_StandardSerialization")
public class StandardSerialization {

    @Autowired
    protected BeanFactory beanFactory;

    public void serialize(Object object, OutputStream os) {
        ObjectOutputStream out = null;
        boolean isObjectStream = os instanceof ObjectOutputStream;
        try {
            out = isObjectStream ? (ObjectOutputStream) os : new ObjectOutputStream(os);
            out.writeObject(object);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to serialize object", ex);
        } finally {
            //Prevent close stream.
            //Only flush buffer to output stream
            if (!isObjectStream && out != null) {
                try {
                    out.flush();
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to serialize object", ex);
                }
            }
        }
    }

    //To work properly must itself be loaded by the application classloader (i.e. by classloader capable of loading
    //all the other application classes). For web application it means placing this class inside webapp folder.
    public Object deserialize(InputStream is) {
        //Put BeanFactory to let deserialized objects restore beans
        SerializationContext.setThreadLocalBeanFactory(beanFactory);
        try {
            ObjectInputStream ois;
            boolean isObjectStream = is instanceof ObjectInputStream;
            if (isObjectStream) {
                ois = (ObjectInputStream) is;
            } else {
                ois = new ObjectInputStream(is) {
                    @Override
                    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                        return ClassUtils.getClass(StandardSerialization.class.getClassLoader(), desc.getName());
                    }
                };
            }
            return ois.readObject();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to deserialize object", ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Failed to deserialize object type", ex);
        } finally {
            //Clean variable after deserialization
            SerializationContext.removeThreadLocalBeanFactory();
        }
    }

    public byte[] serialize(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serialize(object, bos);
        return bos.toByteArray();
    }

    public Object deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return deserialize(new ByteArrayInputStream(bytes));
    }
}
