## Jmix source code

**This repository is a read-only mirror.**

It is a copy of the `jmix/` folder of the Jmix team's private repository, and it is updated automatically. Changes are made only there, so this repository does not accept pull requests.

- To report a problem or ask for a feature, open an issue in this repository or post on the [forum](https://forum.jmix.io).
- To offer a fix, open an issue and attach your change as a patch (`git format-patch`). If a maintainer takes it, it is applied with you as the author and appears here under your name.

## Translations

Translations of the framework messages are kept in a separate repository, [jmix-framework/jmix-translations](https://github.com/jmix-framework/jmix-translations). It accepts pull requests.

- To add or fix a translation, send a pull request there. Edit only the `messages_<locale>.properties` file of the needed locale.
- The English `messages.properties` files in that repository are a reference copy of the framework messages. Do not change them in a pull request. They are updated automatically from the framework.
- A missing key falls back to English, so a translation does not have to cover every key.
- The files are UTF-8. Write non-Latin characters directly, not as `\u` escapes.
