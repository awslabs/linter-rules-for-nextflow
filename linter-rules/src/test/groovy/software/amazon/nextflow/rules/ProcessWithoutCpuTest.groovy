package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class ProcessWithoutCpuTest extends AbstractRuleTestCase<ProcessWithoutCpuRule> {
    protected ProcessWithoutCpuRule createRule(){
        return new ProcessWithoutCpuRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'ProcessWithoutCpuRule'
        assert rule.priority == 2
    }

    @Test
    void processWithCpus_NoViolation() {
        final SOURCE = '''
process big_job {
  cpus 8
  executor 'sge'

  """
  blastp -query input_sequence -num_threads ${task.cpus}
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void processWithLabel_NoViolation() {
        final SOURCE = '''
process bigTask {
  label 'big_mem'

  """
  task script
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void processWithLabelAndCpus_NoViolation() {
        final SOURCE = '''
process bigTask {
  label 'big_mem'
  cpus ${foo}

  """
  task script
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void processWithoutCpusOrLabel_Violation() {
        final SOURCE = '''
process bigTask {
  memory ${foo}
  """
  task script
  """
}
'''
        assertSingleViolation(
                SOURCE,
                2,
                "process bigTask {",
                "No 'label' or 'cpus' directive found. This may reduce the portability of this process."
        )
    }

    @Test
    void processesWithoutCpusOrLabel_Violation() {
        final SOURCE = '''
process bigTask {
  memory ${foo}
  """
  task script
  """
}

process valid {
  label "foo"
  """
  task script
  """
}

process anotherTask {
  memory ${foo}
  """
  task script
  """
}

process alsoValid {
  cpus 12
  """
  task script
  """
}
'''
        assertTwoViolations(
                SOURCE,
                2,
                "process bigTask {",
                "No 'label' or 'cpus' directive found. This may reduce the portability of this process.",
                16,
                "process anotherTask {",
                "No 'label' or 'cpus' directive found. This may reduce the portability of this process."
        )
    }
}
