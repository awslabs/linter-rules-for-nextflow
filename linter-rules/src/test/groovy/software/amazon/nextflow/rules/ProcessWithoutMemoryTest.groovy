package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class ProcessWithoutMemoryTest extends AbstractRuleTestCase<ProcessWithoutMemoryRule> {
    protected ProcessWithoutMemoryRule createRule(){
        return new ProcessWithoutMemoryRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'ProcessWithoutMemoryRule'
        assert rule.priority == 2
    }

    @Test
    void processWithMemory_NoViolation() {
        final SOURCE = '''
process big_job {
  cpus 8
  memory '2 GB'

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
    void processWithLabelAndMemory_NoViolation() {
        final SOURCE = '''
process bigTask {
  label 'big_mem'
  memory ${foo}

  """
  task script
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void processWithoutMemoryOrLabel_Violation() {
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
                "No 'label' or 'memory' directive found. This may reduce the portability of this process."
        )
    }

    @Test
    void processesWithoutCpusOrLabel_Violation() {
        final SOURCE = '''
process bigTask {
  cpus ${foo}
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
  cpus ${foo}
  """
  task script
  """
}

process alsoValid {
  cpus 12
  memory "8 GB"
  """
  task script
  """
}
'''
        assertTwoViolations(
                SOURCE,
                2,
                "process bigTask {",
                "No 'label' or 'memory' directive found. This may reduce the portability of this process.",
                16,
                "process anotherTask {",
                "No 'label' or 'memory' directive found. This may reduce the portability of this process."
        )
    }
}
