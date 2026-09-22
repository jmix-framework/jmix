/*
 * Copyright 2026 Haulmont.
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

package localized_string

import io.jmix.core.CoreConfiguration
import io.jmix.core.LocalizedStringSupport
import io.jmix.core.LocalizedStringValue
import io.jmix.core.security.ClientDetails
import io.jmix.core.security.SecurityContextHelper
import io.jmix.core.security.SystemAuthenticationToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration
import test_support.localized_string.TestLocalizedStringConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestLocalizedStringConfiguration])
class LocalizedStringSupportTest extends Specification {

    static final Locale DE = Locale.GERMAN
    static final Locale RU = Locale.forLanguageTag('ru')
    static final Locale PT_BR = Locale.forLanguageTag('pt-BR')

    @Autowired
    LocalizedStringSupport support

    void cleanup() {
        SecurityContextHelper.setAuthentication(null)
    }

    def "a literal parses as the default value and is not localized"() {
        expect:
        support.parse('Meine Rolle') == new LocalizedStringValue('Meine Rolle', [:])
        support.parse('line 1\nline 2') == new LocalizedStringValue('line 1\nline 2', [:])
        !support.isLocalized('Meine Rolle')
        support.parse(null) == LocalizedStringValue.EMPTY
        support.parse('') == LocalizedStringValue.EMPTY
    }

    def "entries are read after the default, with tolerant whitespace and canonical keys"() {
        when:
        def value = support.parse('Meine Rolle\nen=My role\n pt_BR = Minha função')

        then:
        value.defaultValue() == 'Meine Rolle'
        value.values() == [en: 'My role', pt_BR: 'Minha função']
        value.isLocalized()
        support.isLocalized('Meine Rolle\nen=My role')
    }

    def "a value that starts with an entry has an empty default"() {
        expect:
        support.parse('en=My role\npt_BR=Minha função') == new LocalizedStringValue('', [en: 'My role', pt_BR: 'Minha função'])
    }

    def "a non-entry line continues the preceding value, so multi-line values are allowed everywhere"() {
        when:
        def value = support.parse('first\nsecond\nen=one\ntwo\nde=drei')

        then:
        value.defaultValue() == 'first\nsecond'
        value.values() == [en: 'one\ntwo', de: 'drei']
    }

    def "an empty entry counts as absent and a duplicate key resolves to the first entry, as the database does"() {
        when:
        def value = support.parse('D\nen=\nde=a\nde=b')

        then:
        value.getValue('en') == null
        value.getValue('de') == 'a'
        value.getValue('fr') == null

        and: "the continuation lines of the dropped duplicate do not reach the kept entry"
        support.parse('D\nde=a\nde=b\ncontinued').getValue('de') == 'a'
    }

    def "a line that only looks like an entry is not one"() {
        expect: "'note=' has more than three letters before '=', 'x=' fewer than two"
        support.parse('D\nnote=1\nx=2').defaultValue() == 'D\nnote=1\nx=2'
        !support.containsEntryLine('a note=1 b')
        support.containsEntryLine('text\nen=x')
    }

    def "format writes the canonical form and keeps a plain string plain"() {
        expect:
        support.format(new LocalizedStringValue('Meine Rolle', [:])) == 'Meine Rolle'
        support.format(new LocalizedStringValue('Meine Rolle', [en: 'My role', de: ''])) == 'Meine Rolle\nen=My role'
        support.format(new LocalizedStringValue('', [en: 'My role'])) == '\nen=My role'
    }

    def "blank lines of a multi-line default value survive the round trip"() {
        expect:
        support.parse('\n\nHello').defaultValue() == '\n\nHello'
        support.parse('\n\nHello\nde=X').defaultValue() == '\n\nHello'
        support.parse('Hello\n\n\nde=X').defaultValue() == 'Hello\n\n'
        support.format(support.parse('\n\nHello\nde=X')) == '\n\nHello\nde=X'
    }

    def "parse and format round-trip the canonical form"() {
        given:
        def canonical = 'Meine Rolle\nen=My role\npt_BR=Minha função'

        expect:
        support.format(support.parse(canonical)) == canonical
        support.format(support.parse('\nen=My role')) == '\nen=My role'
    }

    def "resolve walks exact key, language, application default locale, default value, empty string"() {
        given: "the application default locale is 'en' (jmix.core.available-locales is not set in this context)"
        def raw = 'Default\nen=English\npt_BR=Brazil\npt=Portuguese\nfr='

        expect:
        support.resolve(raw, PT_BR) == 'Brazil'
        support.resolve(raw, Locale.forLanguageTag('pt-PT')) == 'Portuguese'
        support.resolve(raw, RU) == 'English'
        support.resolve('Default\nde=Deutsch', RU) == 'Default'
        support.resolve('\nde=Deutsch', RU) == ''
        support.resolve('Default\nfr=', Locale.FRENCH) == 'Default'
        support.resolve('Meine Rolle', DE) == 'Meine Rolle'
        support.resolve(null, DE) == ''
    }

    def "a msg:// reference resolves through the message bundle of the given locale"() {
        given: "a flat key is global, a group/key pair addresses the bundle of a package"
        def flat = 'msg://roles.manager.name'
        def grouped = 'msg://test_support.localized_string/roles.admin.name'

        expect:
        support.isMessageReference(flat)
        !support.isLocalized(flat)
        support.resolve(flat, Locale.ENGLISH) == 'Manager'
        support.resolve(flat, DE) == 'Leiter'
        support.resolve(grouped, Locale.ENGLISH) == 'Administrator'
        support.resolve(grouped, DE) == 'Verwalter'
    }

    def "resolve without a locale uses the current user's locale, else the application default locale"() {
        given:
        def raw = 'Default\nen=English\nde=Deutsch'

        expect: "unauthenticated"
        support.resolve(raw) == 'English'

        when:
        def token = new SystemAuthenticationToken(null)
        token.details = ClientDetails.builder().locale(DE).build()
        SecurityContextHelper.setAuthentication(token)

        then:
        support.resolve(raw) == 'Deutsch'
        support.getCurrentLocale() == DE
    }

    def "resolutionKeys and localeKey follow LocaleResolver.localeToString"() {
        expect:
        support.localeKey(PT_BR) == 'pt_BR'
        support.resolutionKeys(PT_BR) == ['pt_BR', 'pt', 'en']
        support.resolutionKeys(Locale.ENGLISH) == ['en']
    }
}
