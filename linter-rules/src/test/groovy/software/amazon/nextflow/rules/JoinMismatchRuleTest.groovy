package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class JoinMismatchRuleTest extends AbstractRuleTestCase<JoinMismatchRule> {
    protected JoinMismatchRule createRule() {
        return new JoinMismatchRule()
    }

    @Test
    void ruleProperties() {
        assert rule.name == "JoinMismatchRule"
        assert rule.priority == 3
    }

    @Test
    void joinWithMismatchArg_NoViolation() {
        final SOURCE = '''
left = LEFT(ch_param)
right = RIGHT(ch_param)

ch_joined = left.join(right, failOnMismatch: true)
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void joinWithMismatchArgFalse_NoViolation() {
        final SOURCE = '''
left = LEFT(ch_param)
right = RIGHT(ch_param)

ch_joined = left.join(right, failOnMismatch: false)
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
                        "A join will silently discard mismatched items. Use 'failOnMismatch' argument to make your intention explicit."
        )
    }

    @Test
    void joinWithoutMismatchTwice_Violation() {
        final SOURCE = '''
left = LEFT(ch_param)
right = RIGHT(ch_param)

ch_joined = left.join(right)
ch_joined2 = right.join(left, failOnDuplicate: false)
'''
        assertTwoViolations(SOURCE,
                5,
                "ch_joined = left.join(right)",
                "A join will silently discard mismatched items. Use 'failOnMismatch' argument to make your intention explicit.",
                6,
                "ch_joined2 = right.join(left, failOnDuplicate: false)",
                "A join will silently discard mismatched items. Use 'failOnMismatch' argument to make your intention explicit."
        )
    }
}
