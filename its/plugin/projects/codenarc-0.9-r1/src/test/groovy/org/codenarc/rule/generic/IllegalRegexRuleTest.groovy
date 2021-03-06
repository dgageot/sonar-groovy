/*
 * Copyright 2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codenarc.rule.generic

import org.codenarc.rule.AbstractRuleTestCase
import org.codenarc.rule.Rule

/**
 * Tests for IllegalRegexRule
 *
 * @author Chris Mair
 * @version $Revision: 257 $ - $Date: 2009-12-26 01:07:22 +0300 (Сб, 26 дек 2009) $
 */
class IllegalRegexRuleTest extends AbstractRuleTestCase {
    static final REGEX = /\@author Joe/

    void testRuleProperties() {
        assert rule.priority == 3
        assert rule.name == 'IllegalRegex'
    }

    void testRegexIsNull() {
        final SOURCE = ' class MyClass { }'
        rule.regex = null
        assert !rule.ready
        assertNoViolations(SOURCE)
    }

    void testRegexIsPresent() {
        final SOURCE = '''
            /** @author Joe */
            class MyClass {
            }
        '''
        assertSingleViolation(SOURCE, 1, '@author Joe', ['regular expression', REGEX])
    }

    void testTwoOccurrences() {
        final SOURCE = '''
            /** @author Joe */
            class MyClass {
            }
            // Other @author Joe
        '''
        assertTwoViolations(SOURCE,
                1, '@author Joe', ['regular expression', REGEX],
                4, '@author Joe', ['regular expression', REGEX])
    }

    void testRegexIsNotPresent() {
        final SOURCE = '''
            /** @author Mike */
            class MyClass {
            }
        '''
        assertNoViolations(SOURCE)
    }

    void testOverrideViolationMessage() {
        final SOURCE = '''
            /** @author Joe */
            class MyClass {
            }
        '''
        rule.violationMessage = 'abc123'
        assertSingleViolation(SOURCE, 1, '@author Joe', 'abc123')
    }

    protected Rule createRule() {
        return new IllegalRegexRule(regex:REGEX)
    }

}