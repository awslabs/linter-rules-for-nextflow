package software.amazon.nextflow.rules.healthomics

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class MemoryRuleTest extends AbstractRuleTestCase<MemoryRule> {

    @Override
    protected MemoryRule createRule() {
        return new MemoryRule()
    }

    @Test
    void ruleProperties(){
        assert rule.priority == 2
        assert rule.name == "MemoryRule"
    }

    @Test
    void memoryRule_NoViolationsWithParameter(){
        final SOURCE =
                '''
process MY_PROCESS {
 memory params.mem
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void memoryRule_NoViolationsMin(){
        final SOURCE =
                '''
process MY_PROCESS {
 memory '4 GB'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void memoryRule_NoViolationsMax(){
        final SOURCE =
                '''
process MY_PROCESS {
 memory '768 GB'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void memoryRule_MinViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 memory '1 GB'
}
'''
        assertSingleViolation(SOURCE, 3, "memory '1 GB'",
                "The minimum memory allowed in AWS HealthOmics is 2 GB. The requested amount was 1 GB please replace with a larger value")
    }

    @Test
    void memoryRule_MaxViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 memory '769 GB'
}
'''
        assertSingleViolation(SOURCE, 3, "memory '769 GB'",
                "The maximum memory allowed in AWS HealthOmics is 768 GB. The requested amount was 769 GB, please replace with a smaller value")
    }

    @Test
    void memoryRule_NonNumericViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 memory 'A'
}
'''
        assertSingleViolation(SOURCE, 3, "memory 'A'",
                "Unexpected memory directive of A. Recommend using a numeric value and GB units such as '2 GB'")
    }

    @Test
    void memoryRule_NotEnoughArgsViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 memory()
}
'''
        assertSingleViolation(SOURCE, 3, "memory",
                "the memory directive must have one argument")
    }

    @Test
    void memoryRule_TooManyArgsViolation(){
        final SOURCE =
                '''
process MY_PROCESS {
 memory '14 GB', '18 GB'
}
'''
        assertSingleViolation(SOURCE, 3, "memory '14 GB', '18 GB'",
                "the memory directive must have only one argument")
    }

}
