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

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.util.AstUtil
import org.codehaus.groovy.ast.expr.ConstantExpression

/**
 * Utility methods for JUnit rule classes. This class is not intended for general use.
 *
 * @author Chris Mair
 * @version $Revision: 109 $ - $Date: 2009-03-31 05:33:32 +0400 (Вт, 31 мар 2009) $
 */
class JUnitUtil {

    /**
     * Return true if the MethodCallExpression represents a JUnit assert method call with the specified
     * method name and constant argument value. This handles either single-argument assert calls or
     * 2-argument assert methods where the first parameter is the assertion message.
     * @param methodCall - the MethodCallExpression of the method call
     * @param methodName - the name of the method
     * @param value - the argument value
     */
    protected static boolean isAssertConstantValueCall(MethodCallExpression methodCall, String methodName, Object value) {
        def isMatch = false
        if (AstUtil.isMethodCall(methodCall, 'this', methodName)) {
            def args = methodCall.arguments.expressions
            isMatch = args.size() in 1..2 &&
                args[args.size()-1] instanceof ConstantExpression &&
                args[args.size()-1].properties['value'] == value
        }
        return isMatch
    }

    /**
     * Private constructor. All members are static.
     */
    private JUnitUtil() { }
}