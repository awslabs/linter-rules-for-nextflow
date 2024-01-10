package software.amazon.nextflow.rules.healthomics

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class CpuRuleTest extends AbstractRuleTestCase<CpuRule> {
    @Override
    protected CpuRule createRule() {
        new CpuRule()
    }

    @Test
    void ruleProperties(){
        assert rule.priority == 2
        assert rule.name == "CpuRule"
    }

    @Test
    void cpuRule_NoViolationsWithParameter(){
        final SOURCE =
                '''
process MY_PROCESS {
 cpus params.cpus
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void cpuRule_NoViolationsMin(){
        final SOURCE =
'''
process MY_PROCESS {
 cpus 2
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void cpuRule_NoViolationsMax(){
        final SOURCE =
                '''
process MY_PROCESS {
 cpus 96
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void cpuRule_MinViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 cpus 1
}
'''
        assertSingleViolation(SOURCE, 3, 'cpus 1',
                "The minimum CPU count is '2', the supplied value was '1', please replace with a larger value.")
    }

    @Test
    void cpuRule_MaxViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 cpus 97
}
'''
        assertSingleViolation(SOURCE, 3, 'cpus 97',
                "The maximum CPU count is '96', the supplied value was '97', please replace with a smaller value.")
    }

    @Test
    void cpuRule_NonNumericViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 cpus 'A'
}
'''
        assertSingleViolation(SOURCE, 3, "cpus 'A'",
                "'A' is not a valid number. Replace with the desired CPU count.")
    }

    @Test
    void cpuRule_NotEnoughArgsViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 cpus()
}
'''
        assertSingleViolation(SOURCE, 3, "cpus",
                "the cpus directive must have one argument")
    }

    @Test
    void cpuRule_TooManyArgsViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 cpus 14, 18
}
'''
        assertSingleViolation(SOURCE, 3, "cpus 14, 18",
                "the cpus directive must have only one argument")
    }

}
