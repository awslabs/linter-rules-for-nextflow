package software.amazon.nextflow.rules

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class ModuleIncludedTwiceRuleTest extends AbstractRuleTestCase<ModuleIncludedTwiceRule> {
    protected ModuleIncludedTwiceRule createRule(){
        return new ModuleIncludedTwiceRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == 'ModuleIncludedTwiceRule'
        assert rule.priority == 2
    }

    @Test
    void moduleIncludedTwice_Violation(){
        final SOURCE=
'''
include { TOOL } from 'my/path/tool/main'
include { TOOL as TOOL_2 } from 'my/path/tool/main'
'''
        assertSingleViolation(
                SOURCE,
                3,
                "include { TOOL as TOOL_2 } from 'my/path/tool/main'",
                "include with same module and path declared twice"
        )
    }

    @Test
    void oneInclude_NoViolation(){
        final SOURCE=
                '''
include { TOOL } from 'my/path/tool/main'
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void differentPaths_NoViolation(){
        final SOURCE=
                '''
include { TOOL } from 'my/path/tool/main'
include { TOOL as TOOL_2 } from 'different/path/tool/main'
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void moduleIncludedThrice_Violation(){
        final SOURCE=
                '''
include { TOOL } from 'my/path/tool/main'
include { TOOL as TOOL_2 } from 'my/path/tool/main'
include { TOOL as TOOL_3 } from 'my/path/tool/main'
'''
        assertTwoViolations(
                SOURCE,
                3,
                "include { TOOL as TOOL_2 } from 'my/path/tool/main'",
                "include with same module and path declared twice",
                4,
                "include { TOOL as TOOL_3 } from 'my/path/tool/main'",
                "include with same module and path declared twice"
        )
    }
}
