# Jmix Translations

This folder contains translations for the [Jmix](https://jmix.io) framework.

For more information see Jmix [documentation](https://docs.jmix.io/jmix/localization/framework-translations.html).

## Editing

Be sure that the file encoding for the Properties Files is set to UTF-8 in
 *Preferences -> File Encodings -> Properties Files*.

## Building

Publish all translations to `.m2`:
```
./gradlew publishToMavenLocal
```

## Usage

```groovy
implementation 'io.jmix.translations:jmix-translations-ar'
implementation 'io.jmix.translations:jmix-translations-ckb'
implementation 'io.jmix.translations:jmix-translations-de'
implementation 'io.jmix.translations:jmix-translations-el'
implementation 'io.jmix.translations:jmix-translations-fr'
implementation 'io.jmix.translations:jmix-translations-it'
implementation 'io.jmix.translations:jmix-translations-nl'
implementation 'io.jmix.translations:jmix-translations-ro'
implementation 'io.jmix.translations:jmix-translations-ru'
implementation 'io.jmix.translations:jmix-translations-tr'
implementation 'io.jmix.translations:jmix-translations-zh-cn'
```
