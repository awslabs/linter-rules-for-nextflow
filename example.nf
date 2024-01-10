process foo {
    output:
    path 'foo.txt'

    script:
    """
    your_command > foo.txt
    """
}

process bar {
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