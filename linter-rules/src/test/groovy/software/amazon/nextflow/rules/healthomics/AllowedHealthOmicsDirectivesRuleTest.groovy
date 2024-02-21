/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class AllowedHealthOmicsDirectivesRuleTest extends AbstractRuleTestCase<AllowedHealthOmicsDirectivesRule> {
    @Override
    protected AllowedHealthOmicsDirectivesRule createRule() {
        return new AllowedHealthOmicsDirectivesRule()
    }

    private static String makeMsg(String directive){
        "The process directive $directive is not supported by AWS HealthOmics at this time. It may be ignored or it may cause a workflow run to fail"
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'AllowedHealthOmicsDirectivesRule'
        assert rule.priority == 1
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
    void afterScriptDirective_Violation(){
        final SOURCE =
                '''
process foo {
    accelerator 4, type: 'nvidia-tesla-k80\'

    script:
    """
    your_gpu_enabled --command --line
    """
    
    afterScript 'rm somefile.txt'
}
'''
        assertSingleViolation(SOURCE, 10, "afterScript 'rm somefile.txt'",
            makeMsg('afterScript')
        )
    }

    @Test
    void archDirective_Violation(){
        final SOURCE =
                '''
process cpu_task {
    arch 'linux/x86_64', target: 'cascadelake\'

    """
    blastp -query input_sequence -num_threads ${task.cpus}
    """
}
'''
        assertSingleViolation(SOURCE, 3, "arch 'linux/x86_64', target: 'cascadelake'",
            makeMsg('arch')
        )
    }

    @Test
    void beforeScript_NoViolation(){
        final SOURCE =
'''
process foo {
  beforeScript 'source /cluster/bin/setup\'

  """
  echo bar
  """
}
'''
        assertSingleViolation(SOURCE, 3, "beforeScript 'source /cluster/bin/setup'",
            makeMsg('beforeScript')
        )
    }

    @Test
    void cache_Violation() {
        final SOURCE =
                '''
process noCacheThis {
  cache false

  script:
  """echo foo"""
}
'''
        assertSingleViolation(SOURCE, 3, 'cache false', makeMsg('cache'))
    }
    @Test
    void clusterOptions_Violation(){
        final SOURCE =
                '''
process foo {
  clusterOptions 'abc'

  script:
  """echo foo"""
}
'''
        assertSingleViolation(SOURCE, 3, "clusterOptions 'abc'", makeMsg('clusterOptions'))
    }

    @Test
    void conda_Violation(){
        final SOURCE =
                '''
process foo {
  conda 'bwa=0.7.15'

  script:
  """echo foo"""
}
'''
        assertSingleViolation(SOURCE, 3, "conda 'bwa=0.7.15'", makeMsg('conda'))
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
    void containerOptions_Violation(){
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
        assertSingleViolation(SOURCE, 4, "containerOptions '--volume /data/db:/db'", makeMsg('containerOptions'))
    }


    @Test
    void cpus_NoViolation(){
        final SOURCE =
                '''
process big_job {
  cpus 8
  
  """
  blastp -query input_sequence -num_threads ${task.cpus}
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void debug_Violation(){
        final SOURCE =
                '''
process sayHello {
  debug true

  script:
  "echo Hello"
}
'''
        assertSingleViolation(SOURCE, 3, "debug true", makeMsg('debug'))
    }

    @Test
    void disk_Violation(){
        final SOURCE = '''
process big_job {
    disk '2 GB'

    """
    your task script here
    """
}
'''
        assertSingleViolation(SOURCE, 3, "disk '2 GB'", makeMsg('disk'))
    }

    @Test
    void echo_Violation(){
        final SOURCE = '''
process big_job {
    echo true
    
    """
    your task script here
    """
}
'''
        assertSingleViolation(SOURCE, 3, 'echo true', makeMsg('echo'))
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
    void executor_Violation() {
        final SOURCE = '''
process foo {
    executor 'slurm'
    """ echo foo """
}
'''
        assertSingleViolation(SOURCE, 3, "executor 'slurm'", makeMsg('executor'))
    }

    @Test
    void fair_NoViolations(){
        final SOURCE = '''
process foo {
    fair true

    input:
    val x
    output:
    tuple val(task.index), val(x)

    script:
    """
    sleep \\$((RANDOM % 3))
    """
}


workflow {
    channel.of('A','B','C','D') | foo | view
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void machineType_Violation(){
        final SOURCE = '''
process foo {
  machineType 'n1-highmem-8'

  """
  <your script here>
  """
}
'''
        assertSingleViolation(SOURCE, 3, "machineType 'n1-highmem-8'", makeMsg('machineType'))
    }

    @Test
    void maxSubmitAwait_NoViolation() {
        final SOURCE = '''
process foo {
  errorStrategy 'retry'
  maxSubmitAwait '10 mins'
  maxRetries 3
  script:
  """
  your_job --here
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void maxErrors_NoViolations(){
        final SOURCE = '''
process retryIfFail {
  errorStrategy 'retry'
  maxErrors 5

  """
  echo 'do this as that .. '
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void maxForks_Violation(){
        final SOURCE = '''
process doNotParallelizeIt {
  maxForks 1

  \'\'\'
  <your script here>
  \'\'\'
}
'''
        assertSingleViolation(SOURCE, 3, "maxForks 1", makeMsg("maxForks"))
    }

    @Test
    void maxRetries_NoViolations(){
        final SOURCE = '''
process retryIfFail {
    errorStrategy 'retry\'
    maxRetries 3

    """
    echo 'do this as that .. \'
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void memory_NoViolations(){
        final SOURCE = '''
process big_job {
    memory '2 GB\'

    """
    your task script here
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void module_NoViolations(){
        final SOURCE = '''
process basicExample {
  module 'ncbi-blast/2.2.27\'

  """
  blastp -query <etc..>
  """
}
'''
        assertSingleViolation(SOURCE, 3, "module 'ncbi-blast/2.2.27'",
            makeMsg('module')
        )
    }

    @Test
    void penv_Violations(){
        final SOURCE = '''
process big_job {
  cpus 4
  penv 'smp\'

  """
  blastp -query input_sequence -num_threads ${task.cpus}
  """
}
'''
        assertSingleViolation(SOURCE, 4, "penv 'smp'",
            makeMsg('penv')
        )
    }

    @Test
    void pod_Violation(){
        final SOURCE = '''
process your_task {
  pod env: 'FOO', value: 'bar\'

  \'\'\'
  echo $FOO
  \'\'\'
}
'''
        assertSingleViolation(SOURCE, 3, "pod env: 'FOO', value: 'bar'",
            makeMsg('pod')
        )
    }

    @Test
    void publishDir_NoViolations(){
        final SOURCE = '''
process foo {
    publishDir '/data/chunks', mode: 'copy', overwrite: false

    output:
    path 'chunk_*\'

    \'\'\'
    printf 'Hola' | split -b 1 - chunk_
    \'\'\'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void queue_Violation(){
        final SOURCE = '''
process grid_job {
    queue 'long\'

    """
    your task script here
    """
}
'''
        assertSingleViolation(SOURCE, 3, "queue 'long'", makeMsg('queue'))
    }

    @Test
    void resourceLabels_NoViolations(){
        final SOURCE = '''
process my_task {
    resourceLabels region: 'some-region', user: 'some-username\'

    \'\'\'
    <task script>
    \'\'\'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void scratch_NoViolations(){
        final SOURCE = '''
process simpleTask {
  scratch true

  output:
  path 'data_out\'

  \'\'\'
  <task script>
  \'\'\'
}
'''
        assertSingleViolation(SOURCE, 3, 'scratch true',
            makeMsg('scratch')
        )
    }

    @Test
    void shell_NoViolations(){
        final SOURCE = '''
process doMoreThings {
    shell '/bin/bash', '-euo', 'pipefail\'

    \'\'\'
    your_command_here
    \'\'\'
}
'''
        assertSingleViolation(SOURCE, 3, "shell '/bin/bash', '-euo', 'pipefail'",
            makeMsg('shell')
        )
    }

    @Test
    void spack_NoViolations(){
        final SOURCE = '''
process foo {
    spack 'bwa@0.7.15\'

    \'\'\'
    your_command --here
    \'\'\'
}
'''
        assertSingleViolation(SOURCE, 3, "spack 'bwa@0.7.15'",

        )
    }

    @Test
    void stageInMode_Violation(){
        final SOURCE = '''
process foo {    
    stageInMode 'copy'

    input:
    path /foo/baa
    
    \'\'\'
    your_command --here
    \'\'\'
}
'''
        assertSingleViolation(SOURCE, 3, "stageInMode 'copy'", makeMsg('stageInMode'))
    }

    @Test
    void stageOutMode_Violation(){
        final SOURCE = '''
process foo {    
    stageOutMode 'copy'

    input:
    path /foo/baa
    
    \'\'\'
    your_command --here
    \'\'\'
}
'''
        assertSingleViolation(SOURCE, 3, "stageOutMode 'copy'", makeMsg('stageOutMode'))
    }

    @Test
    void storeDir_Violation(){
        final SOURCE = '''
process formatBlastDatabases {
  storeDir '/db/genomes\'

  input:
  path species

  output:
  path "${dbName}.*"

  script:
  dbName = species.baseName
  """
  makeblastdb -dbtype nucl -in ${species} -out ${dbName}
  """
}
'''
        assertSingleViolation(SOURCE, 3, "storeDir '/db/genomes'",
            makeMsg('storeDir')
        )
    }

    @Test
    void tag_NoViolations(){
        final SOURCE = '''
process foo {
  tag "$code"

  input:
  val code

  """
  echo $code
  """
}

workflow {
  Channel.of('alpha', 'gamma', 'omega') | foo
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void time_NoViolations(){
        final SOURCE = '''
process big_job {
    time '1h\'

    """
    your task script here
    """
}
'''
        assertNoViolations(SOURCE)
    }
}
