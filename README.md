# Jmix Email

This repository contains Email project of the [Jmix](https://jmix.io) framework.

For more information see:

* Jmix Core project source [repository](https://github.com/Haulmont/jmix-core).
* Jmix [documentation](https://docs.jmix.io).

### Email sending configuration
Two groups of application properties are used to configure email sending:
1. Spring Boot mail properties (e.g. host, port, protocol and etc)
2. Jmix specific properties (e.g. defaultFromAddress)

Spring Boot mail properties must be set using the following format "_spring.mail.XXX_".
<br/>
**Note:** Property ```spring.mail.host``` is required to send email.

Also there is an ability to specify JavaMail properties in the format "_spring.mail.properties.XXX_".
<br/>E.g. property ```mail.smtp.connectiontimeout``` must be specified in the following way:

```spring.mail.properties.mail.smtp.connectiontimeout=20000```

**Example of configuration (Gmail SMTP server)**:
```
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.protocol=smtp
spring.mail.username=username
spring.mail.password=password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```
### File storage usage

There is an ability to store email body text and attachments in file storage instead of BLOB columns in database.
     
Steps to configure file storage usage:     
1. Configure file storage implementation.<br/>
To use JMIX file storage implementation it is needed to add the following dependency:<br/>
    ```
    dependencies {
            //...
            implementation 'io.jmix.localfs:jmix-localfs-starter'
    }
    ``` 
2. Enable file storage usage: <br/>
Set ```jmix.email.isFileStorageUsed=true``` in application.properties
3. Configure default file storage: set property ```jmix.core.defaultFileStorage``` in application.properties
