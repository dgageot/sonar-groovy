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
package org.codenarc.rule.junit

import org.codenarc.rule.AbstractRuleTestCase
import org.codenarc.rule.Rule

/**
 * Tests for JUnitSetUpCallsSuperRule
 *
 * @author Chris Mair
 * @version $Revision: 257 $ - $Date: 2009-12-26 01:07:22 +0300 (Сб, 26 дек 2009) $
 */
class JUnitSetUpCallsSuperRuleTest extends AbstractRuleTestCase {

    void testRuleProperties() {
        assert rule.priority == 2
        assert rule.name == 'JUnitSetUpCallsSuper'
    }

    void testApplyTo_SetUpCallsSuperSetUp() {
        final SOURCE = '''
          class MyTest extends TestCase {
            void setUp() {
                super.setUp()
                println 'bad'
            }
          }
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_SetUpDoesNotCallSuperSetUp() {
        final SOURCE = '''
          class MyTest extends TestCase {
            void setUp() {
                println 'bad'
            }
          }
        '''
        assertSingleViolation(SOURCE, 3, 'void setUp() {')
    }

    void testApplyTo_SetUpDoesNotCallSuperSetUp_CallsSuperSetUpWithParameters() {
        final SOURCE = '''
          class MyTest extends TestCase {
            void setUp() {
                println 'bad'
                super.setUp('But', 'has', 'parameters')
            }
          }
        '''
        sourceCodePath = 'src/MyTests.groovy'
        assertSingleViolation(SOURCE, 3, 'void setUp() {')
    }

    void testApplyTo_SetUpDoesNotCallSuperSetUp_CallsSuper() {
        final SOURCE = '''
          class MyTest extends TestCase {
            void setUp() {
                println 'bad'
                super.someOtherMethod()
            }
          }
        '''
        assertSingleViolation(SOURCE, 3, 'void setUp() {')
    }

    void testApplyTo_SetUpDoesNotCallSuperSetUp_CallsSetUp() {
        final SOURCE = '''
          class MyTest extends TestCase {
            void setUp() {
                println 'bad'
                other.setUp()
            }
          }
        '''
        assertSingleViolation(SOURCE, 3, 'void setUp() {')
    }

    void testApplyTo_SetUpIncludesCallWithNamedParameterList() {
        final SOURCE = '''
          class MyTest extends TestCase {
            void setUp() {
                super.setUp()
        		ant.delete(dir:appBase, failonerror:false)
            }
          }
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_SetUpMethodHasBeforeAnnotation() {
        final SOURCE = '''
          class MyTest extends TestCase {
            @Before void setUp() {
                println 'bad'
            }
          }
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_NonSetUpMethod() {
        final SOURCE = '''
            class MyTest extends TestCase {
                def otherMethod() {
                }
            }
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_SetUpMethodHasParameters() {
        final SOURCE = '''
            class MyTest extends TestCase {
                void setUp(int count, String name) {
                }
            }
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_NonTestClass() {
        final SOURCE = '''
            class MyClass {
              int count
            }
        '''
        assertNoViolations(SOURCE)
    }

    protected Rule createRule() {
        return new JUnitSetUpCallsSuperRule()
    }
}