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
package org.codenarc.rule.braces

import org.codenarc.rule.AbstractRuleTestCase
import org.codenarc.rule.Rule

/**
 * Tests for ElseBlockBracesRule
 *
 * @author Chris Mair
 * @version $Revision: 257 $ - $Date: 2009-12-26 01:07:22 +0300 (Сб, 26 дек 2009) $
 */
class ElseBlockBracesRuleTest extends AbstractRuleTestCase {

    void testRuleProperties() {
        assert rule.priority == 2
        assert rule.name == 'ElseBlockBraces'
    }

    void testApplyTo_Violation() {
        final SOURCE = '''
            class MyClass {
                def myClosure = {
                    if (x==23) { } else println '23'
                    println 'ok'
                    if (alreadyInitialized())
                        println 'initialized'
                    else
                        println 'not initialized'
                }
            }
        '''
        assertTwoViolations(SOURCE, 4, "else println '23'", 6, 'if (alreadyInitialized())')
    }

    void testApplyTo_IfWithoutElse() {
        final SOURCE = '''
            if (isReady) {
                println 'ready'
            }
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_ElseIf() {
        final SOURCE = '''
            if (isReady) {
                println 'ready'
            } else if (able) {
                println 'able'
            }
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_BracesRequiredForElseIf() {
        final SOURCE = '''
            if (isReady) {
                println 'ready'
            } else if (able) {
                println 'able'
            }
        '''
        rule.bracesRequiredForElseIf = true
        assertSingleViolation(SOURCE, 2, "if (isReady)")
    }

    void testApplyTo_Violation_ElseBlockWithCommentOnly() {
        final SOURCE = '''
            if (isReady) {
                // TODO Should do something here
            } else {
                // TODO And should do something here
            }
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_NoViolations() {
        final SOURCE = '''class MyClass {
                def myMethod() {
                    if (isReady) {
                        println "ready"
                    } else { println 'not ready' }
                    if (x==23) { println '23' }
                    else { println 'ok' }
                }
            }'''
        assertNoViolations(SOURCE)
    }

    protected Rule createRule() {
        return new ElseBlockBracesRule()
    }

}