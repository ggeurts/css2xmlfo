package be.re.css;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.PositionalCondition;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;

/**
 * Represents one CSS2 rule with one or more {@link Property} instances.
 *
 * @author Werner Donn\u00e9
 * @author Gerke Geurts
 */
public class CSSRule
{
    private String elementName;
    private final List<Property> properties = new ArrayList<>();
    private String pseudoElementName;
    private final Selector selector;
    private final Selector[] selectorChain;
    private final int specificity;

    /** 
     * Constructs a rule that represents a style attribute
     */
    CSSRule()
    {
        this.selector = null;
        this.selectorChain = null;
        this.specificity = 10000;
    }
    
    CSSRule(Selector selector)
    {
        this.selector = selector instanceof ElementSelector
                ? new InternedElementSelector((ElementSelector) selector) 
                : selector;
        selectorChain = Util.getSelectorChain(selector);
        specificity = specificity();
        elementName = getElementName(selectorChain, selectorChain.length - 1);
        pseudoElementName = getPseudoElementName(selectorChain);

        if (elementName != null)
        {
            elementName = elementName.intern();
        }

        if (pseudoElementName != null)
        {
            pseudoElementName = pseudoElementName.intern();
        }
    }

    /**
     * The created rule physically shares the selector and specificity
     * information. This makes it possible to match a set of rules resulting
     * after a split by picking only one of them.
     */
    private CSSRule(CSSRule source)
    {
        this.selector = source.selector;
        this.elementName = source.elementName;
        this.pseudoElementName = source.pseudoElementName;
        this.selectorChain = source.selectorChain;
        this.specificity = source.specificity;
    }

    void addProperty(Property property)
    {
        properties.add(property);
    }

    /**
     * Returns the interned name of the element this rule applies to. If it
     * doesn't apply to an element <code>null</code> is returned.
     * @return 
     */
    public String getElementName()
    {
        return elementName;
    }

    private static String getElementName(Selector[] selectorChain, int position)
    {
        switch (selectorChain[position].getSelectorType())
        {
            case Selector.SAC_ELEMENT_NODE_SELECTOR:
                return ((ElementSelector) selectorChain[position]).getLocalName();
            case Selector.SAC_PSEUDO_ELEMENT_SELECTOR:
                return getElementName(selectorChain, position - 1);
            default:
                return null;
        }
    }

    List<Property> getProperties()
    {
        return properties;
    }

    public Property getProperty()
    {
        if (properties.size() != 1)
        {
            throw new RuntimeException("Unsplit rule");
        }
        return properties.get(0);
    }

    private static void getPseudoClassConditions(Condition c, List<String> result)
    {
        if (c.getConditionType() == Condition.SAC_PSEUDO_CLASS_CONDITION)
        {
            result.add(((AttributeCondition) c).getValue());
        } 
        else if (c.getConditionType() == Condition.SAC_AND_CONDITION)
        {
            getPseudoClassConditions(((CombinatorCondition) c).getFirstCondition(), result);
            getPseudoClassConditions( ((CombinatorCondition) c).getSecondCondition(), result);
        }
    }

    /**
     * Returns the interned pseudo element name or <code>null</code> if the rule
     * doesn't apply to a pseudo element.
     * @return 
     */
    public String getPseudoElementName()
    {
        return pseudoElementName;
    }

    private static String getPseudoElementName(Selector[] selectorChain)
    {
        if (selectorChain[selectorChain.length - 1].getSelectorType() == Selector.SAC_PSEUDO_ELEMENT_SELECTOR)
        {
            return ((ElementSelector) selectorChain[selectorChain.length - 1]).getLocalName();
        }

        if (selectorChain.length > 1
                && selectorChain[selectorChain.length - 2].getSelectorType()
                == Selector.SAC_CONDITIONAL_SELECTOR)
        {
            List<String> conditions = new ArrayList<>();
            getPseudoClassConditions(
                    ((ConditionalSelector) selectorChain[selectorChain.length - 2]).getCondition(),
                    conditions
            );

            return conditions.contains("before")
                    ? "before"
                    : (conditions.contains("after")
                            ? "after"
                            : (conditions.contains("first-line") ? "first-line" : null));
        }

        return null;
    }

    /**
     * Returns the selector that matches the rule.
     * @return 
     */
    public Selector getSelector()
    {
        return selector;
    }

    /**
     * Flattens the selector expression tree in infix order.
     */
    Selector[] getSelectorChain()
    {
        return selectorChain;
    }

    int getSpecificity()
    {
        return specificity;
    }

    private int specificity()
    {
        Specificity s = new Specificity();
        specificity(selector, s);
        return 10000 * s.ids + 100 * s.attributes + s.names;
    }

    private static void specificity(Selector selector, Specificity s)
    {
        if (selector instanceof ConditionalSelector)
        {
            specificity(((ConditionalSelector) selector).getCondition(), s);
            specificity(((ConditionalSelector) selector).getSimpleSelector(), s);
        } 
        else if (selector instanceof DescendantSelector)
        {
            specificity(((DescendantSelector) selector).getAncestorSelector(), s);
            specificity(((DescendantSelector) selector).getSimpleSelector(), s);
        } 
        else if (selector instanceof NegativeSelector)
        {
            specificity(((NegativeSelector) selector).getSimpleSelector(), s);
        } 
        else if (selector instanceof SiblingSelector)
        {
            specificity(((SiblingSelector) selector).getSelector(), s);
            specificity(((SiblingSelector) selector).getSiblingSelector(), s);
        } 
        else if (selector.getSelectorType() == Selector.SAC_ELEMENT_NODE_SELECTOR
                && ((ElementSelector) selector).getLocalName() != null // There is no name for "*".
                )
        {
            ++s.names;
        }
    }

    private static void specificity(Condition c, Specificity s)
    {
        switch (c.getConditionType())
        {
            case Condition.SAC_ID_CONDITION:
                ++s.ids;
                break;

            case Condition.SAC_ATTRIBUTE_CONDITION:
            case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
            case Condition.SAC_CLASS_CONDITION:
            case Condition.SAC_LANG_CONDITION:
            case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
            case Condition.SAC_PSEUDO_CLASS_CONDITION:
                ++s.attributes;
                break;

            case Condition.SAC_AND_CONDITION:
            case Condition.SAC_OR_CONDITION:
                specificity(((CombinatorCondition) c).getFirstCondition(), s);
                specificity(((CombinatorCondition) c).getSecondCondition(), s);
                break;
        }

        if (c.getConditionType() == Condition.SAC_POSITIONAL_CONDITION
                && ((PositionalCondition) c).getPosition() == 1 // first-child pseudo class.
                )
        {
            ++s.attributes;
        }
    }

    /**
     * Parses a CSS2 style declaration (without '{' and '}').
     * @param style The style declaration.
     * @param baseUrl URL of document from which style declaration originates.
     * @return A rule set with the parsed CSS rules.
     * @throws CSSException 
     */
    public static List<CSSRule> parseStyle(String style, URL baseUrl) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(new StringReader(style));
            Builder builder = new Builder(baseUrl);
            CSSRuleCollector collector = new CSSRuleCollector(new Builder(baseUrl), true);
            Parser parser = Util.getSacParser();
            parser.setDocumentHandler(collector);
            parser.parseStyleDeclaration(source);
            return builder.getRules();
        }
        catch (IOException e)
        {
            throw new CSSException(e);
        }
    }
    
    private static class Specificity
    {
        private int attributes;
        private int ids;
        private int names;
    } // Specificity

    private static class Builder implements CSSRuleSetBuilder 
    {
        private final URL baseUrl;
        private final List<CSSRule> rules = new ArrayList<>();
        
        public Builder(URL baseUrl)
        {
            this.baseUrl = baseUrl;
        }

        public List<CSSRule> getRules()
        {
            return rules;
        }
        
        @Override
        public URL getUrl()
        {
            return baseUrl;
        }
        
        @Override
        public void include(String uri) throws CSSException
        {
            // NOP
        }
        
        @Override
        public void addRule(CSSRule rule)
        {
            rules.add(rule);
        }
    
        @Override
        public void addPageRule(CSSPageRule pageRule)
        {
            // NOP
        }
    }
} // Rule
