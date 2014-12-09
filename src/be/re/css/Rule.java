package be.re.css;

import org.w3c.css.sac.Selector;

/**
 * Represents an atomic, fully compiled CSS rule.
 *
 * @author Werner Donn\u00e9
 * @author Gerke Geurts
 */
public class Rule
{
    private final CssRule cssRule;
    private final Property property;
    private final int specificity;
    private final int position;

    public Rule(CssRule cssRule, Property property, int position, int specificity)
    {
        this.cssRule = cssRule;
        this.property = property;
        this.position = position;
        this.specificity = specificity;
    }

    /**
     * Returns the interned name of the element this rule applies to. If it
     * doesn't apply to an element <code>null</code> is returned.
     */
    public String getElementName()
    {
        return cssRule.getElementName();
    }

    public Property getProperty()
    {
        return property;
    }

    /**
     * Returns the interned pseudo element name or <code>null</code> if the rule
     * doesn't apply to a pseudo element.
     */
    public String getPseudoElementName()
    {
        return cssRule.getPseudoElementName();
    }

    /**
     * Returns the selector that matches the rule.
     */
    public Selector getSelector()
    {
        return cssRule.getSelector();
    }

    /**
     * Flattens the selector expression tree in infix order.
     */
    Selector[] getSelectorChain()
    {
        return cssRule.getSelectorChain();
    }

    int getSpecificity()
    {
        return specificity;
    }
    
    int getPosition()
    {
        return position;
    }
} // Rule
