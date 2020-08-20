# Jmix Translations

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