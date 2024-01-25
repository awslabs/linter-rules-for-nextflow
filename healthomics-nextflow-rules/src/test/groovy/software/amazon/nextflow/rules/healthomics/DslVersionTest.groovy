/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class DslVersionTest extends AbstractRuleTestCase<DslVersionRule>{
    @Override
    protected DslVersionRule createRule() {
        return new DslVersionRule()
    }

    @Test
    void ruleProperties(){
        assert rule.priority == 1
        assert rule.name == 'DslVersionRule'
    }

    @Test
    void dsl2_NoViolations(){
        final SOURCE = '''
nextflow.enable.dsl=2

process foo {
    cpus 2
    """
    script here
    """
}

workflow {
    data = channel.fromPath('/some/path/*.txt')
    foo()
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void dsl1_OneViolation(){
        final SOURCE = '''
nextflow.enable.dsl=1

process foo {
    cpus 2
    """
    script here
    """
}

workflow {
    data = channel.fromPath('/some/path/*.txt')
    foo()
}
'''
        assertSingleViolation(SOURCE, 2, 'nextflow.enable.dsl=1', "HealthOmics only supports DSL version 2, found 1")
    }

    @Test
    void nonConstant_OneViolation(){
        final SOURCE = '''
nextflow.enable.dsl=1+1

process foo {
    cpus 2
    """
    script here
    """
}

workflow {
    data = channel.fromPath('/some/path/*.txt')
    foo()
}
'''
        assertSingleViolation(SOURCE, 2, 'nextflow.enable.dsl=1', "Expected a constant with a value of 2, found (1 + 1)")

    }
}
