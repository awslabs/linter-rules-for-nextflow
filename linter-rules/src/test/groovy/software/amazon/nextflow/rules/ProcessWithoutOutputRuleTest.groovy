/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class ProcessWithoutOutputRuleTest extends AbstractRuleTestCase<ProcessWithoutOutputRule> {

    @Override
    protected ProcessWithoutOutputRule createRule(){
        return new ProcessWithoutOutputRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'ProcessWithoutOutputRule'
        assert rule.priority == 2
    }

    @Test
    void outputFound_NoViolation() {
        final SOURCE = '''
process basicExample {
  input:
  val x

  output:
  path $foo

  "echo process job $x > $foo"
}

workflow {
  def num = Channel.of(1,2,3)
  basicExample(num)
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void outputNotFound_Violation() {
        final SOURCE = '''
process basicExample {
  input:
  val x

  "echo process job $x > $foo"
}

workflow {
  def num = Channel.of(1,2,3)
  basicExample(num)
}
'''
        assertSingleViolation(
                SOURCE,
                2,
                "process basicExample {",
                "No 'output:' expression for process. This will limit composition."
        )
    }

    @Test
    void outputNotFound_Violations() {
        final SOURCE = '''
process basicExample {
  input:
  val x

  "echo process job $x > $foo"
}

process validExample {
  input:
  val x
  
  output:
  path $foo

  "echo process job $x > $foo"
}

process basicExample2 {
  input:
  val x

  "echo process job $x > $foo"
}

workflow {
  def num = Channel.of(1,2,3)
  basicExample(num)
}
'''
        assertTwoViolations(
                SOURCE,
                2,
                "process basicExample {",
                "No 'output:' expression for process. This will limit composition.",
                19,
                "process basicExample2 {",
                "No 'output:' expression for process. This will limit composition.",
        )
    }
}
