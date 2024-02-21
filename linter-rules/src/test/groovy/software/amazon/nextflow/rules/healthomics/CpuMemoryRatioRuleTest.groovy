/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class CpuMemoryRatioRuleTest extends AbstractRuleTestCase<CpuMemoryRatioRule>{
    @Override
    protected CpuMemoryRatioRule createRule() {
        return new CpuMemoryRatioRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'CpuMemoryRatioRule'
        assert rule.priority == 3
    }

    @Test
    void noMemoryOrCpu_NoRatioViolation(){
        def final SOURCE = 'process MY_PROCESS {}'
        assertNoViolations(SOURCE)
    }

    @Test
    void onlyCpu_NoRatioViolation(){
        def final SOURCE = 'process MY_PROCESS {cpu 4}'
        assertNoViolations(SOURCE)
    }

    @Test
    void onlyMemory_NoRatioViolation(){
        def final SOURCE = "process MY_PROCESS {memory '16 GB'}"
        assertNoViolations(SOURCE)
    }

    @Test
    void shouldIgnoreParameters_NoRatioViolation(){
        def final SOURCE=
'''process FOO {
        cpus params.cpus
        memory params.memory
    }
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void doesNotTestRatioWithInvalidValues_NoRatioViolation(){
        def final SOURCE =
'''process FOO {
        cpus 'foo'
        memory 'lots of GB'
    }
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void twoToOneRatio_NoRatioViolation(){
        def final SOURCE =
'''process FOO {
        cpus 2
        memory '4 GB'
    }
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void fourToOneRatio_NoRatioViolation(){
        def final SOURCE =
                '''process FOO {
        cpus 2
        memory '8 GB'
    }
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void eightToOneRatio_NoRatioViolation(){
        def final SOURCE =
                '''process FOO {
        cpus 2
        memory '16 GB'
    }
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void threeToOneRatio_RatioViolation(){
        def final SOURCE =
'''process FOO {
        cpus 2
        memory '6 GB'
    }
'''
        assertSingleViolation(
                SOURCE,
                1,
                'process FOO {',
                "Memory to cpu ratio may not be optimal for HealthOmics " +
                        "instance placement. Ratios of 2:1, 4:1 or 8:1 are optimal"
        )
    }
}
