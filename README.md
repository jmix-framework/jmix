# Jmix Email

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
