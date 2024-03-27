/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class ProcessWithoutInputRuleTest extends AbstractRuleTestCase<ProcessWithoutInputRule> {

    @Override
    protected ProcessWithoutInputRule createRule(){
        return new ProcessWithoutInputRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'ProcessWithoutInputRule'
        assert rule.priority == 2
    }

    @Test
    void inputFound_NoViolation() {
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
    void inputtNotFound_Violation() {
        final SOURCE = '''
process basicExample {
  output:
  path "foo"

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
                "No 'input:' expression for process. This will limit composition and flexibility."
        )
    }

    @Test
    void inputNotFound_Violations() {
        final SOURCE = '''
process basicExample {
  output:
  path: "foo"

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
  output:
  path "foo"

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
                "No 'input:' expression for process. This will limit composition and flexibility.",
                19,
                "process basicExample2 {",
                "No 'input:' expression for process. This will limit composition and flexibility.",
        )
    }
}
