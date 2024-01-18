nextflow.enable.dsl=2

process foo {
    container 'ubuntu:latest'
    cpus 1

    output:
    path 'foo.txt'

    script:
    """
    your_command > foo.txt
    """
}

process bar {
    container 'ubuntu:latest'
    cpus 1
    publishDir '/foo'

    input:
    path x

    output:
    path 'bar.txt'

    script:
    """
    another_command $x > bar.txt
    """
}

workflow {
    data = channel.fromPath('/some/path/*.txt')
    foo()
    bar(data)
}