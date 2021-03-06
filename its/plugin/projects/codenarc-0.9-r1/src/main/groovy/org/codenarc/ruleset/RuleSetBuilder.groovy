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
package org.codenarc.ruleset

import org.codenarc.rule.Rule

/**
 * A Builder for RuleSets. Create a RuleSet by calling the <code>ruleset</code>
 * method, passing in a <code>Closure</code> defining the contents of the RuleSet.
 * The <code>Closure</code> can contain any combination of the following (as well as
 * arbitrary Groovy code):
 * <ul>
 *   <li><code>ruleset</code> - to load a RuleSet file. The path specifies either a
 *          Groovy file or an XML file.</li>
 *   <li><code>rule</code> - to load a single Rule</li>
 *   <li><code>description</code> - description of the RuleSet (optional)</li>
 * </ul>
 *
 * @author Chris Mair
 * @version $Revision: 212 $ - $Date: 2009-08-26 05:20:16 +0400 (Ср, 26 авг 2009) $
 */
class RuleSetBuilder {

    private topLevelDelegate = new TopLevelDelegate()

    void ruleset(Closure closure) {
        closure.delegate = topLevelDelegate
        closure.call()
    }

    RuleSet getRuleSet() {
        topLevelDelegate.ruleSet
    }
}

class TopLevelDelegate {
    private allRuleSet = new CompositeRuleSet()

    void ruleset(String path) {
        def ruleSet = RuleSetUtil.loadRuleSetFile(path)
        allRuleSet.addRuleSet(ruleSet)
    }

    void ruleset(String path, Closure closure) {
        def ruleSet = RuleSetUtil.loadRuleSetFile(path)
        def ruleSetConfigurer = new RuleSetDelegate(ruleSet)
        closure.delegate = ruleSetConfigurer
        closure.call()
        allRuleSet.addRuleSet(ruleSetConfigurer.ruleSet)
    }

    void rule(Class ruleClass) {
        RuleSetUtil.assertClassImplementsRuleInterface(ruleClass)
        Rule rule = ruleClass.newInstance()
        allRuleSet.addRule(rule)
    }

    void rule(Class ruleClass, Closure closure) {
        RuleSetUtil.assertClassImplementsRuleInterface(ruleClass)
        Rule rule = ruleClass.newInstance()
        closure.delegate = rule
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
        allRuleSet.addRule(rule)
    }

    void rule(String path) {
        def rule = RuleSetUtil.loadRuleScriptFile(path)
        allRuleSet.addRule(rule)
    }

    void rule(String path, Closure closure) {
        def rule = RuleSetUtil.loadRuleScriptFile(path)
        closure.delegate = rule
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
        allRuleSet.addRule(rule)
    }

    void description(String description) {
        // Do nothing
    }

    protected RuleSet getRuleSet() {
        return allRuleSet
    }
}

class RuleSetDelegate {
    RuleSet ruleSet

    RuleSetDelegate(RuleSet ruleSet) {
        this.ruleSet = new FilteredRuleSet(ruleSet)
    }

    void exclude(String excludeNames) {
        ruleSet.addExclude(excludeNames)
    }

    void include(String includeNames) {
        ruleSet.addInclude(includeNames)
    }

    def methodMissing(String name, args) {
        def rule = findRule(name)
        if (!rule) {
            throw new RuntimeException("No such rule named [$name]")
        }

        def arg = args[0]
        if (arg instanceof Closure) {
            arg.delegate = rule
            arg.setResolveStrategy(Closure.DELEGATE_FIRST)
            arg.call()
        }
        else {
            // Assume it is a Map
            arg.each { key, value -> rule[key] = value }
        }
    }

    private Rule findRule(String name) {
        ruleSet.rules.find { rule -> rule.name == name }
    }
}