/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class ProcessWithoutExecEnvironmentTest extends AbstractRuleTestCase<ProcessWithoutExecEnvironmentRule> {
    protected ProcessWithoutExecEnvironmentRule createRule(){
        return new ProcessWithoutExecEnvironmentRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'ProcessWithoutExecEnvironmentRule'
        assert rule.priority == 2
    }

    @Test
    void processWithLabel_NoViolation() {
        final SOURCE = '''
process big_job {
  cpus 8
  memory '2 GB'
  label "foo"
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void processWithContainer_NoViolation() {
        final SOURCE = '''
process bigTask {
  container 'foo/baa:latest'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void processWithConda_NoViolation() {
        final SOURCE = '''
process bigTask {
  conda 'foo'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void processWithModule_NoViolation() {
        final SOURCE = '''
process bigTask {
  module 'foo'
}
'''
        assertNoViolations(SOURCE)
    }
    @Test
    void processWithSpack_NoViolation() {
        final SOURCE = '''
process bigTask {
  spack 'foo'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void processWithoutExecEnv_Violation() {
        final SOURCE = '''
process bigTask {
  cpus ${foo}
  """
  task script
  """
}
'''
        assertSingleViolation(
                SOURCE,
                2,
                "process bigTask {",
                "No execution environment is declared using one of ${rule.directives.toString()}. " +
                        "This may reduce the portability of this process."
        )
    }

    @Test
    void processesWithoutExecEnv_Violation() {
        final SOURCE = '''
process bigTask {
  cpus ${foo}
  """
  task script
  """
}

process valid {
  label "foo"
  container "image:latest"
  """
  task script
  """
}

process anotherTask {
  cpus ${foo}
  """
  task script
  """
}

process alsoValid {
  cpus 12
  memory "8 GB"
  container "image:latest"
  """
  task script
  """
}
'''
        assertTwoViolations(
                SOURCE,
                2,
                "process bigTask {",
                "No execution environment is declared using one of ${rule.directives.toString()}. " +
                        "This may reduce the portability of this process.",
                17,
                "process anotherTask {",
                "No execution environment is declared using one of ${rule.directives.toString()}. " +
                        "This may reduce the portability of this process."
        )
    }
}
