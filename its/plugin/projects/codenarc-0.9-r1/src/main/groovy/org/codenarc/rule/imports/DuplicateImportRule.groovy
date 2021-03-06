/*
 * Copyright 2008 the original author or authors.
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
package org.codenarc.rule.imports

import org.codenarc.rule.AbstractRule
import org.codenarc.source.SourceCode

/**
 * Rule that checks for a duplicate import
 *
 * NOTE: Does not work under Groovy 1.7 (i.e., will not produce violations for duplicate
 * import statements).
 *
 * NOTE: Does not distinguish between multiple duplicate imports of the same class.
 * Thus, it may produce multiple violations with the same line number in that case.
 *
 * @author Chris Mair
 * @version $Revision: 302 $ - $Date: 2010-02-01 03:06:00 +0300 (Пн, 01 фев 2010) $
 */
class DuplicateImportRule extends AbstractRule {
    String name = 'DuplicateImport'
    int priority = 3

    void applyTo(SourceCode sourceCode, List violations) {
        def importedClassNames = new HashSet()

        sourceCode.ast?.imports.each { importNode ->
            if (importedClassNames.contains(importNode.className)) {
                violations.add(createViolationForImport(sourceCode, importNode))
            }
            else {
                importedClassNames.add(importNode.className)
            }
        }
    }

}