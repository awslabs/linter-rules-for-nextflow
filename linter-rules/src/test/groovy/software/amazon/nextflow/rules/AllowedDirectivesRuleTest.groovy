/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

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
    void machineType_NoViolations(){
        final SOURCE = '''
process foo {
  machineType 'n1-highmem-8'

  """
  <your script here>
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void maxSubmitAwait_NoViolations() {
        final SOURCE = '''
process foo {
  errorStrategy 'retry'
  maxSubmitAwait '10 mins'
  maxRetries 3
  queue "\\${task.submitAttempt==1 : 'spot-compute' : 'on-demand-compute'}"
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
    void maxForks_NoViolations(){
        final SOURCE = '''
process doNotParallelizeIt {
  maxForks 1

  \'\'\'
  <your script here>
  \'\'\'
}
'''
        assertNoViolations(SOURCE)
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
    executor 'sge\'

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
        assertNoViolations(SOURCE)
    }

    @Test
    void penv_NoViolations(){
        final SOURCE = '''
process big_job {
  cpus 4
  penv 'smp\'
  executor 'sge\'

  """
  blastp -query input_sequence -num_threads ${task.cpus}
  """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void pod_NoViolations(){
        final SOURCE = '''
process your_task {
  pod env: 'FOO', value: 'bar\'

  \'\'\'
  echo $FOO
  \'\'\'
}
'''
        assertNoViolations(SOURCE)
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
    void queue_NoViolations(){
        final SOURCE = '''
process grid_job {
    queue 'long\'
    executor 'sge\'

    """
    your task script here
    """
}
'''
        assertNoViolations(SOURCE)
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
        assertNoViolations(SOURCE)
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
        assertNoViolations(SOURCE)
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
        assertNoViolations(SOURCE)
    }

    @Test
    void stageInMode_NoViolations(){
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
        assertNoViolations(SOURCE)
    }

    @Test
    void stageOutMode_NoViolations(){
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
        assertNoViolations(SOURCE)
    }

    @Test
    void storeDir_NoViolations(){
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
        assertNoViolations(SOURCE)
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