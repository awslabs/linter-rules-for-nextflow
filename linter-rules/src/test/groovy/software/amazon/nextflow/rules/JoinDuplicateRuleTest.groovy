package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class JoinDuplicateRuleTest extends AbstractRuleTestCase<JoinDuplicateRule> {
    protected JoinDuplicateRule createRule() {
        return new JoinDuplicateRule()
    }

    @Test
    void ruleProperties() {
        assert rule.name == "JoinDuplicateRule"
        assert rule.priority == 3
    }

    @Test
    void joinWithDuplicateArg_NoViolation() {
        final SOURCE = '''
left = LEFT(ch_param)
right = RIGHT(ch_param)

ch_joined = left.join(right, failOnMismatch: true, failOnDuplicate: true)
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void joinWithDuplicateArgFalse_NoViolation() {
        final SOURCE = '''
left = LEFT(ch_param)
right = RIGHT(ch_param)

ch_joined = left.join(right, failOnDuplicate: false)
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void joinWithoutMismatchArg_Violation() {
        final SOURCE = '''
left = LEFT(ch_param)
right = RIGHT(ch_param)

ch_joined = left.join(right)
'''
        assertSingleViolation(SOURCE,
                        5,
                        "ch_joined = left.join(right)",
                        "The join doesn't explicitly define a strategy for duplicates. Recommend adding a 'failOnDuplicate' argument."
        )
    }

    @Test
    void joinWithoutMismatchTwice_Violation() {
        final SOURCE = '''
left = LEFT(ch_param)
right = RIGHT(ch_param)

ch_joined = left.join(right)
ch_joined2 = right.join(left, failOnMismatch: false)
'''
        assertTwoViolations(SOURCE,
                5,
                "ch_joined = left.join(right)",
                "The join doesn't explicitly define a strategy for duplicates. Recommend adding a 'failOnDuplicate' argument.",
                6,
                "ch_joined2 = right.join(left, failOnMismatch: false)",
                "The join doesn't explicitly define a strategy for duplicates. Recommend adding a 'failOnDuplicate' argument."
        )
    }
}
