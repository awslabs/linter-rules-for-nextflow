package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class AllowedDirectivesRuleTest extends AbstractRuleTestCase<AllowedDirectivesRule> {
    @Override
    protected AllowedDirectivesRule createRule() {
        return new AllowedDirectivesRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'AllowedDirectivesRule'
        assert rule.priority == 1
    }

    @Test
    void unrecognizedDirective_Violation() {
        final SOURCE =
'''process foo {
    accelerator 4, type: 'nvidia-tesla-k80'
    
    unrecognized 'abc'

    script:
    """
    your_gpu_enabled --command --line
    """
}
'''
        assertSingleViolation(SOURCE, 4, "unrecognized 'abc'", "The process directive unrecognized is not recognized. Check the value and correct if needed")
    }

    @Test
    void acceleratorDirective_NoViolation(){
        final SOURCE =
'''process foo {
    accelerator 4, type: 'nvidia-tesla-k80\'

    script:
    """
    your_gpu_enabled --command --line
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void afterScriptDirective_NoViolation(){
        final SOURCE =
                '''process foo {
    accelerator 4, type: 'nvidia-tesla-k80\'

    script:
    """
    your_gpu_enabled --command --line
    """
    
    afterScript 'rm somefile.txt'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void archDirective_NoViolation(){
        final SOURCE =
'''process cpu_task {
    spack 'blast-plus@2.13.0\'
    arch 'linux/x86_64', target: 'cascadelake\'

    """
    blastp -query input_sequence -num_threads ${task.cpus}
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void beforeScript_NoViolation(){
        final SOURCE =
'''process foo {
  beforeScript 'source /cluster/bin/setup\'

  """
  echo bar
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void cache_NoViolation(){
        final SOURCE =
'''
process noCacheThis {
  cache false

  script:
  """echo foo"""
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void clusterOptions_NoViolation(){
        final SOURCE =
'''
process foo {
  clusterOptions 'abc'

  script:
  """echo foo"""
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void conda_NoViolation(){
        final SOURCE =
'''
process foo {
  conda 'bwa=0.7.15'

  script:
  """echo foo"""
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void container_NoViolation(){
        final SOURCE =
'''
process runThisInDocker {
  container 'dockerbox:tag'

  """
  <your holy script here>
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void containerOptions_NoViolation(){
        final SOURCE =
'''
process runThisInDocker {
  container 'dockerbox:tag'
  containerOptions '--volume /data/db:/db'
  
  """
  <your holy script here>
  """
}
'''
        assertNoViolations(SOURCE)
    }


    @Test
    void cpus_NoViolation(){
        final SOURCE =
'''
process big_job {
  cpus 8
  executor 'sge\'

  """
  blastp -query input_sequence -num_threads ${task.cpus}
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void debug_NoViolation(){
        final SOURCE =
                '''
process sayHello {
  debug true

  script:
  "echo Hello"
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void disk_NoViolation(){
        final SOURCE = '''
process big_job {
    disk '2 GB'
    executor 'cirrus'

    """
    your task script here
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void echo_NoViolation(){
        final SOURCE = '''
process big_job {
    echo true
    
    """
    your task script here
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void errorStrategy_NoViolation(){
        final SOURCE = '''
process foo {
    errorStrategy 'ignore'
    """ do do de do """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void executor_noViolation() {
        final SOURCE = '''
process foo {
    executor 'slurm'
    """ echo foo """
}
'''
        assertNoViolations(SOURCE)
    }
}