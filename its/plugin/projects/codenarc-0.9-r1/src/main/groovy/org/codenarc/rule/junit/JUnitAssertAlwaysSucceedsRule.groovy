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

import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codehaus.groovy.ast.expr.MethodCallExpression

/**
 * Rule that checks for JUnit <code>assert()</code> method calls with constant arguments
 * such that the assertion always succeeds. This includes:
 * <ul>
 *   <li><code>assertTrue(true)</code>.</li>
 *   <li><code>assertFalse(false)</code>.</li>
 *   <li><code>assertNull(null)</code>.</li>
 * </ul>
 * This rule sets the default value of <code>applyToFilesMatching</code> to only match source code file
 * paths ending in 'Test.groovy' or 'Tests.groovy'.
 *
 * @author Chris Mair
 * @version $Revision: 163 $ - $Date: 2009-05-24 03:41:24 +0400 (Вс, 24 май 2009) $
 */
class JUnitAssertAlwaysSucceedsRule extends AbstractAstVisitorRule {
    String name = 'JUnitAssertAlwaysSucceeds'
    int priority = 2
    Class astVisitorClass = JUnitAssertAlwaysSucceedsAstVisitor
    String applyToClassNames = DEFAULT_TEST_CLASS_NAMES
}

class JUnitAssertAlwaysSucceedsAstVisitor extends AbstractAstVisitor  {

    void visitMethodCallExpression(MethodCallExpression methodCall) {
        def isMatch =
            JUnitUtil.isAssertConstantValueCall(methodCall, 'assertTrue', Boolean.TRUE) ||
            JUnitUtil.isAssertConstantValueCall(methodCall, 'assertFalse', Boolean.FALSE) ||
            JUnitUtil.isAssertConstantValueCall(methodCall, 'assertNull', null)
        if (isMatch) {
            addViolation(methodCall)
        }
        super.visitMethodCallExpression(methodCall)
    }

}