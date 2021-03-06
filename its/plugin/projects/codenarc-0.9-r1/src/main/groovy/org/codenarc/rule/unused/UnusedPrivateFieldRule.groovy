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
package org.codenarc.rule.unused

import org.codehaus.groovy.ast.FieldNode
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codenarc.util.AstUtil

/**
 * Rule that checks for private fields that are not referenced within the same class.
 *
 * @author Chris Mair
 * @version $Revision: 303 $ - $Date: 2010-02-03 04:36:02 +0300 (Ср, 03 фев 2010) $
 */
class UnusedPrivateFieldRule extends AbstractAstVisitorRule {
    String name = 'UnusedPrivateField'
    int priority = 2
    Class astVisitorClass = UnusedPrivateFieldAstVisitor
}

class UnusedPrivateFieldAstVisitor extends AbstractAstVisitor  {
    private unusedPrivateFields

    void visitClass(ClassNode classNode) {
        this.unusedPrivateFields = classNode.fields.findAll { fieldNode ->
            def isPrivate = fieldNode.modifiers & FieldNode.ACC_PRIVATE
            def isNotGenerated = fieldNode.lineNumber != -1
            isPrivate && isNotGenerated
        }
        super.visitClass(classNode)

        unusedPrivateFields.each { unusedPrivateField ->
            addViolation(unusedPrivateField)
        }
    }

    void visitVariableExpression(VariableExpression expression) {
        removeUnusedPrivateField(expression.name)

        // This causes problems (StackOverflow) in Groovy 1.7.0
        //super.visitVariableExpression(expression)
    }

    void visitProperty(PropertyNode node) {
        removeUnusedPrivateField(node.name)
        super.visitProperty(node)
    }

    void visitPropertyExpression(PropertyExpression expression) {
        if (    expression.objectExpression instanceof VariableExpression &&
                expression.objectExpression.name == 'this' &&
                expression.property instanceof ConstantExpression) {

            removeUnusedPrivateField(expression.property.value)
        }
        super.visitPropertyExpression(expression)
    }

    void visitFieldExpression(FieldExpression expression) {
        super.visitFieldExpression(expression)
    }

    void visitMethod(MethodNode node) {
        if (node.parameters) {
            node.parameters.each { parameter ->
                def initialExpression = parameter.initialExpression
                if (initialExpression && AstUtil.respondsTo(initialExpression, 'getName')) {
                    removeUnusedPrivateField(initialExpression.name)
                }
            }
        }
        super.visitMethod(node)
    }
    private void removeUnusedPrivateField(String name) {
        def referencedField = unusedPrivateFields.find { it.name == name }
        if (referencedField) {
            unusedPrivateFields.remove(referencedField)
        }
    }
}