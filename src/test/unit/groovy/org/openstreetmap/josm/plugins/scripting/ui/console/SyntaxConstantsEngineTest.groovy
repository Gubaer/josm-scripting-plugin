package org.openstreetmap.josm.plugins.scripting.ui.console

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test

class SyntaxConstantsEngineTest extends GroovyTestCase {

    @Test
    void "can load syntax constants"() {
        var syntaxConstants = SyntaxConstantsEngine.getAvailableSyntaxConstants()
        assertTrue(syntaxConstants.size() > 0)
    }

    @Test
    void "can create a Rule"() {
        var rule = new SyntaxConstantsEngine.Rule("text/javascript", ~/.*javascript.*/)
        assertNotNull(rule)

        shouldFail(NullPointerException) {
            new SyntaxConstantsEngine.Rule("text/javascript", null)
        }

        shouldFail(NullPointerException) {
            new SyntaxConstantsEngine.Rule(null, ~/.*javascript.*/)
        }
    }

    @Test
    void "can create rule from property values"() {
        var rule
        rule = SyntaxConstantsEngine.Rule.fromProperties(
            "text/javascript",
            /.*javascript.*/.toString()
        )
        assertNotNull(rule)

        assertEquals("text/javascript", rule.getSyntaxStyle())
    }

    @Test
    void "fromProperties() - null parameters should throw"() {
        var rule
        shouldFail(NullPointerException) {
            rule = SyntaxConstantsEngine.Rule.fromProperties(
                null,
                /.*javascript.*/.toString()
            )
        }

        shouldFail(NullPointerException) {
            rule = SyntaxConstantsEngine.Rule.fromProperties(
                "text/javascript",
                null
            )
        }
    }

    @Test
    void "fromProperties() - blank parameters should result in null rule"() {
        var rule
        rule = SyntaxConstantsEngine.Rule.fromProperties("text/javascript", "   ")
        assertNull(rule)
        rule = SyntaxConstantsEngine.Rule.fromProperties("", /.*javascript.*/.toString())
        assertNull(rule)
    }

    @Test
    void "fromProperties() - invalid regexp pattern should result in null rule"() {
        var rule
        rule = SyntaxConstantsEngine.Rule.fromProperties(
            "text/javascript",
            "[Jj][Av[Vv[Aa]" // an invalid regexp pattern
        )
        assertNull(rule)
    }

    @Test
    void "match() - rule should match for mime-type"() {
        var rule = new SyntaxConstantsEngine.Rule("text/javascript", ~/(?i).*javascript.*/)
        var result = rule.matches("text/javascript")
        assertTrue(result)
        result = rule.matches("text/JavaScript")
        assertTrue(result)
        result = rule.matches("text/groovy")
        assertFalse(result)
    }

    @Test
    void "Rules - can create rules"() {
        var rules = new SyntaxConstantsEngine.Rules([
            new SyntaxConstantsEngine.Rule("text/javascript", ~/(?i).*javascript.*/),
            new SyntaxConstantsEngine.Rule("text/groovy", ~/(?i).*groovy.*/)
        ])
        assertNotNull(rules)

        rules = new SyntaxConstantsEngine.Rules(null)
        assertNotNull(rules)
    }

    @Test
    void "Rules - can derive syntax style"() {
        var rules = new SyntaxConstantsEngine.Rules([
            new SyntaxConstantsEngine.Rule("text/javascript", ~/(?i).*javascript.*/),
            new SyntaxConstantsEngine.Rule("text/groovy", ~/(?i).*groovy.*/)
        ])
        var style = rules.deriveSuitableSyntaxStyle("text/javascript")
        assertEquals("text/javascript", style)
        style = rules.deriveSuitableSyntaxStyle("application/GrOOvY")
        assertEquals("text/groovy", style)

        // no matching rule
        style = rules.deriveSuitableSyntaxStyle("text/python")
        assertNull(style)
    }

    @Test
    void "Rules - can load valid properties"() {
        var source = """
        rule.1.syntax-style=text/javascript
        rule.1.pattern=(?i).*javascript.*
        
        rule.2.syntax-style=text/groovy
        rule.2.pattern=(?i).*groovy.*
        """
        var properties = new Properties()
        properties.load(new StringReader(source))

        var rules = SyntaxConstantsEngine.Rules.loadFromProperties(properties)
        var style = rules.deriveSuitableSyntaxStyle("text/javascript")
        assertEquals("text/javascript", style)
        style = rules.deriveSuitableSyntaxStyle("application/GrOOvY")
        assertEquals("text/groovy", style)

        // no matching rule
        style = rules.deriveSuitableSyntaxStyle("text/python")
        assertNull(style)
    }

    @Test
    void "Rules - can load invalid properties with empty styles"() {
        var source = """
        rule.1.syntax-style=  
        rule.1.pattern=(?i).*javascript.*/
        
        rule.2.syntax-style=text/groovy
        rule.2.pattern=(?i).*groovy.*
        """
        var properties = new Properties()
        properties.load(new StringReader(source))

        // loading should work
        var rules = SyntaxConstantsEngine.Rules.loadFromProperties(properties)

        // but no rule for javascript
        var style = rules.deriveSuitableSyntaxStyle("text/javascript")
        assertNull(style)

        // rule for groovy should fire
        style = rules.deriveSuitableSyntaxStyle("application/GrOOvY")
        assertEquals("text/groovy", style)
    }

    @Test
    void "Rules - can load invalid properties with invalid regexp"() {
        var source = """
        rule.1.syntax-style=text/javascript
        rule.1.pattern=[Jj][Aa[Vv[Aa]
        
        rule.2.syntax-style=text/groovy
        rule.2.pattern=(?i).*groovy.*
        """
        var properties = new Properties()
        properties.load(new StringReader(source))

        // loading should work
        var rules = SyntaxConstantsEngine.Rules.loadFromProperties(properties)

        // but no rule for javascript
        var style = rules.deriveSuitableSyntaxStyle("text/javascript")
        assertNull(style)

        // rule for groovy should fire
        style = rules.deriveSuitableSyntaxStyle("application/GrOOvY")
        assertEquals("text/groovy", style)
    }
}
