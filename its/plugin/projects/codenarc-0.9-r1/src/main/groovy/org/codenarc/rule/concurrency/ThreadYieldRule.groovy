/*
 * Copyright 2010 the original author or authors.
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

package org.codenarc.rule.concurrency

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.AbstractAstVisitor
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression

/**
  * Method calls to Thread.yield() should not be allowed.
  * This method has no useful guaranteed semantics, and is often used by inexperienced
  * programmers to mask race conditions.
  *
  * @author Hamlet D'Arcy
  * @version $Revision: 335 $ - $Date: 2010-05-01 04:09:20 +0400 (Сб, 01 май 2010) $
  */
class ThreadYieldRule extends AbstractAstVisitorRule {

     String name = 'ThreadYield'
     int priority = 2
     Class astVisitorClass = ThreadYieldAstVisitor
 }

class ThreadYieldAstVisitor extends AbstractAstVisitor {

     def void visitMethodCallExpression(MethodCallExpression call) {
         if (call.objectExpression instanceof VariableExpression) {
             def target = call.objectExpression.variable
             if (target == "Thread" && call.method instanceof ConstantExpression) {
                 if (call.method.value == "yield") {
                     addViolation(call)
                 }
             }
         }
         super.visitMethodCallExpression(call);
     }
 }
