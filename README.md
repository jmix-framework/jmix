# Jmix Translations

This repository contains translations for the [Jmix](https://jmix.io) framework.

For more information see:

* Jmix Core project source [repository](https://github.com/Haulmont/jmix-core).
* Jmix [documentation](https://docs.jmix.io).

## Building

Publish all translations to `.m2`:
```
./gradlew publishToMavenLocal
```

Publish only Russian translation to `.m2`:
```
./gradlew publishRuPublicationToMavenLocal
```

## Usage

```
dependencies {
    // ...
    implementation 'io.jmix.translations:jmix-translations-ru:0.1.0-SNAPSHOT'
```