/*
 * Copyright 2020 Haulmont.
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

package io.jmix.email;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains email details: list of recipients, from address, subject, body and attachments.
 * See {@link EmailInfoBuilder} for more information.
 *
 * @see Emailer
 * @see EmailInfoBuilder
 */
public class EmailInfo implements Serializable {

    private static final long serialVersionUID = -382773435130109083L;

    public static final String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";
    public static final String TEXT_CONTENT_TYPE = "text/plain; charset=UTF-8";

    /**
     * Recipient email addresses separated with "," or ";" symbol.
     * <p>
     * One message will be sent for all recipients and it will include CC and BCC.
     */
    private String addresses;
    private String cc;
    private String bcc;
    private String subject;
    private String from;
    private String body;
    private String bodyContentType;
    private boolean important;
    private List<EmailAttachment> attachments;
    private List<EmailHeader> headers;


    /**
     * INTERNAL
     */
    EmailInfo(String addresses,
              String cc,
              String bcc,
              String subject,
              String from,
              String body,
              String bodyContentType,
              boolean important,
              List<EmailHeader> headers,
              List<EmailAttachment> attachments) {
        this.addresses = addresses;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.from = from;
        this.body = body;
        this.important = important;
        this.bodyContentType = bodyContentType;
        this.headers = headers;
        this.attachments = attachments;

    }

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<EmailHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<EmailHeader> headers) {
        this.headers = headers;
    }

    public void addHeader(String name, String value) {
        if (this.headers == null)
            this.headers = new ArrayList<>();
        this.headers.add(new EmailHeader(name, value));
    }

    public String getBodyContentType() {
        return bodyContentType;
    }

    public void setBodyContentType(String bodyContentType) {
        this.bodyContentType = bodyContentType;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public Boolean getImportant() {
        return important;
    }

    public void setImportant(Boolean important) {
        this.important = important;
    }
}