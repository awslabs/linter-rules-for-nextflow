package software.amazon.nextflow.rules.healthomics


import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

/**
 * Tests for ContainerUriIsEcrUriRule
 *
 * @author Mark Schreiber
 */
class ContainerUriRuleTest extends AbstractRuleTestCase<ContainerUriRule> {

    @Test
    void RuleProperties() {
        assert rule.priority == 1
        assert rule.name == 'ContainerUriIsEcrUri'
    }

    @Test
    void ecrUri_NoViolations() {
        final SOURCE = '''
process splitSequences {
    publishDir params.publishDir
    container '123456789012.dkr.ecr.us-east-1.amazonaws.com/foo/baa\'
    cpus 1

    input:
    path 'input.fa\'
    path 'input2.txt\'

    output:
    path 'seq_*\'

    """
    awk '/^>/{f="seq_"++d} {print > f}' < input.fa
    """
}
        '''
        assertNoViolations(SOURCE)
    }

    @Test void ecrUriWithTag_NoViolations() {
        final SOURCE = '''
process splitSequences {
    publishDir params.publishDir
    container '123456789012.dkr.ecr.us-east-1.amazonaws.com/foo/baa:latest\'
    cpus 1

    input:
    path 'input.fa\'
    path 'input2.txt\'

    output:
    path 'seq_*\'

    """
    awk '/^>/{f="seq_"++d} {print > f}' < input.fa
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test void ecrWithSha_NoViolations() {
        final SOURCE = '''
process splitSequences {
    publishDir params.publishDir
    container '123456789012.dkr.ecr.us-east-1.amazonaws.com/foo/baa@sha256:33574f446ac991f77bac125fbf6a2340e6db972a3f334e6c61bff94740165938\'
    cpus 1

    input:
    path 'input.fa\'
    path 'input2.txt\'

    output:
    path 'seq_*\'

    """
    awk '/^>/{f="seq_"++d} {print > f}' < input.fa
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test void parameterized_NoViolations() {
        final SOURCE = '''
process splitSequences {
    publishDir params.publishDir
    container params.myImage
    cpus 1

    input:
    path 'input.fa\'
    path 'input2.txt\'

    output:
    path 'seq_*\'

    """
    awk '/^>/{f="seq_"++d} {print > f}' < input.fa
    """
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void dockerHubUri_Violations() {
        final SOURCE = '''         
process splitSequences {
    publishDir params.publishDir
    container 'busybox:latest\'
    cpus 1

    input:
    path 'input.fa\'
    path 'input2.txt\'

    output:
    path 'seq_*\'

    """
    awk '/^>/{f="seq_"++d} {print > f}' < input.fa
    """
}
        '''
        assertViolations(SOURCE,
                [line:4,
                 source:"container 'busybox:latest\'",
                 message:"The container image URI 'busybox:latest' does not match the pattern '\\d{12}\\.dkr\\.ecr\\..+\\.amazonaws.com/.*'. Replace with an ECR image URI."
                ]
        )
    }

    @Override
    protected ContainerUriRule createRule() {
        new ContainerUriRule()
    }
}

