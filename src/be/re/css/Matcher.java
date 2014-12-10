package be.re.css;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.NegativeCondition;
import org.w3c.css.sac.PositionalCondition;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Finds the matching rules as the document goes through it.
 *
 * @author Werner Donn\u00e9
 */
public class Matcher implements ContentHandler
{

    private static final String DEFAULT_LANGUAGE = "en-GB";

    private final Stack<Element> elements = new Stack<>();
    private final Compiled.DFAState startState;
    private static final boolean trace = System.getProperty("be.re.css.trace") != null;

    public Matcher(Compiled styleSheet)
    {
        startState = styleSheet.startState;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
    }

    private static boolean checkAttributeCondition(Element e, AttributeCondition c, TestAttribute test)
    {
        if (c.getNamespaceURI() != null)
        {
            int index = DocumentHandler.SAC_NO_URI.equals(c.getNamespaceURI())
                    ? e.attributes.getIndex(c.getLocalName())
                    : e.attributes.getIndex(c.getNamespaceURI(), c.getLocalName());
            return index != -1 && test.test(e.attributes, index, c);
        }

        for (int i = 0; i < e.attributes.getLength(); ++i)
        {
            if (e.attributes.getLocalName(i).equals(c.getLocalName()) && test.test(e.attributes, i, c))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean checkAttributeCondition(Element e, AttributeCondition c)
    {
        return checkAttributeCondition(
                e,
                c,
                new TestAttribute()
                {
                    @Override
                    public boolean test(Attributes atts, int i, AttributeCondition c)
                    {
                        return c.getValue() == null || c.getValue().equals(atts.getValue(i));
                    }
                }
        );
    }

    private static boolean checkBeginHyphenAttributeCondition(Element e, AttributeCondition c)
    {
        return checkAttributeCondition(
                e,
                c,
                new TestAttribute()
                {
                    @Override
                    public boolean test(Attributes atts, int i, AttributeCondition c)
                    {
                        return atts.getValue(i).startsWith(c.getValue() + "-")
                        || atts.getValue(i).equals(c.getValue());
                    }
                }
        );
    }

    private static boolean checkClassCondition(Element e, AttributeCondition c)
    {
        String value;

        return (value = e.attributes.getValue("class")) != null
                && hasToken(value, c.getValue());
    }

    private static boolean checkCondition(Element e, Condition c)
    {
        switch (c.getConditionType())
        {
            case Condition.SAC_AND_CONDITION:
                return checkCondition(e, ((CombinatorCondition) c).getFirstCondition())
                        && checkCondition(e, ((CombinatorCondition) c).getSecondCondition());

            case Condition.SAC_ATTRIBUTE_CONDITION:
                return checkAttributeCondition(e, (AttributeCondition) c);

            case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
                return checkBeginHyphenAttributeCondition(e, (AttributeCondition) c);

            case Condition.SAC_CLASS_CONDITION:
                return checkClassCondition(e, (AttributeCondition) c);

            case Condition.SAC_ID_CONDITION:
                return checkIdCondition(e, (AttributeCondition) c);

            case Condition.SAC_LANG_CONDITION:
                return checkLangCondition(e, (LangCondition) c);

            case Condition.SAC_NEGATIVE_CONDITION:
                return !checkCondition(e, ((NegativeCondition) c).getCondition());

            case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
                return checkOneOfAttributeCondition(e, (AttributeCondition) c);

            case Condition.SAC_OR_CONDITION:
                return checkCondition(e, ((CombinatorCondition) c).getFirstCondition())
                        || checkCondition(e, ((CombinatorCondition) c).getSecondCondition());

            case Condition.SAC_POSITIONAL_CONDITION:
                return checkPositionalCondition(e, ((PositionalCondition) c).getPosition());

            case Condition.SAC_PSEUDO_CLASS_CONDITION:
                return checkPseudoClassCondition(e, (AttributeCondition) c);

            default:
                return false; // Ignore non-CSS2 or irrelevant condition types.
        }
    }

    private static boolean checkIdCondition(Element e, AttributeCondition c)
    {
        for (int i = 0; i < e.attributes.getLength(); ++i)
        {
            if ("ID".equals(e.attributes.getType(i)) && c.getValue().equals(e.attributes.getValue(i)))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean checkLangCondition(Element e, LangCondition c)
    {
        return e.language.startsWith(((LangCondition) c).getLang() + "-")
                || e.language.equals(((LangCondition) c).getLang());
    }

    private static boolean checkOneOfAttributeCondition(Element e, AttributeCondition c)
    {
        return checkAttributeCondition(e, c, new TestAttribute()
        {
            @Override
            public boolean test(Attributes atts, int i, AttributeCondition c1)
            {
                return hasToken(atts.getValue(i), c1.getValue());
            }
        });
    }

    private static boolean checkPositionalCondition(Element e, int position)
    {
    // The element on the top of the stack is not yet in the child list of its
        // parent. The preceding sibling is the last element in the parent's child
        // list.

        return e.parent.children.size() == position;
    }

    private static boolean checkPseudoClassCondition(Element e, AttributeCondition c)
    {
        return "after".equals(c.getValue()) 
                || "before".equals(c.getValue())
                || ("first-child".equals(c.getValue()) && checkPositionalCondition(e, 0));
    }

    @Override
    public void endDocument() throws SAXException
    {
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        Element element = elements.pop();

        elements.peek().children.add(element);
        element.children = null;
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException
    {
    }

    @SuppressWarnings("StringEquality")
    private String getLanguage(String namespaceURI, Attributes attributes, Element parent)
    {
        String result = null;

        if (Constants.XHTML == namespaceURI)
        {
            result = attributes.getValue("lang");
        }

        if (result == null)
        {
            result = attributes.getValue("xml:lang");
        }

        if (result == null)
        {
            result = parent.language;
        }

        return result;
    }

    private static Set<Compiled.DFAState> getSiblingStates(Collection<Compiled.DFAState> states)
    {
        Set<Compiled.DFAState> result = new HashSet<>();

        for (Compiled.DFAState state : states)
        {
            Compiled.DFAState nextState = state.events.get(Compiled.Event.SiblingElement);
            if (nextState != null)
            {
                result.add(nextState);
            }
        }

        return result;
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
    }

    private static boolean hasToken(String s, String token)
    {
        int i;

        return (i = s.indexOf(token)) != -1 && (i == 0 || s.charAt(i - 1) == ' ')
                && (i == s.length() - token.length() || s.charAt(i + token.length()) == ' ');
    }

    /**
     * Returns the rules that match a pseudo element sorted from least to most
     * specific.
     * @return 
     */
    public SortedSet<Rule> matchingPseudoRules()
    {
        SortedSet<Rule> result = new TreeSet<>(new RuleComparator());
        for (Compiled.DFAState state : elements.peek().states)
        {
            result.addAll(state.pseudoRules);
        }
        return result;
    }

    /**
     * Returns the rules that match a normal element sorted from least to most
     * specific.
     * @return 
     */
    public SortedSet<Rule> matchingRules()
    {
        SortedSet<Rule> result = new TreeSet<Rule>(new RuleComparator());
        for (Compiled.DFAState state : elements.peek().states)
        {
            result.addAll(state.rules);
        }
        return result;
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
    }

    @Override
    public void skippedEntity(String name) throws SAXException
    {
    }

    @Override
    public void startDocument() throws SAXException
    {
        elements.clear();

        Element root = new Element("", "/");

        root.language = DEFAULT_LANGUAGE;
        elements.push(root);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        Element parent = elements.peek();
        Set<Compiled.DFAState> currentStates = parent.states;
        Element element = new Element(namespaceURI, localName);

        element.attributes = atts;
        element.language = getLanguage(namespaceURI, atts, parent);
        element.parent = parent;
        elements.push(element);

        traceElement(namespaceURI + "|" + localName, atts);
        stepStates(parent.states, element);

        if (parent.children.size() > 0)
        {
            stepStates(
                    getSiblingStates(parent.children.get(parent.children.size() - 1).states),
                    element
            );
        }

    // At every element new rules can be started, because they are relative.
        step(startState, element);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
    }

    /**
     * More than one state transition can occur because when the candidate
     * conditions are fulfilled, they constitute an event. The universal
     * selector transitions are also tried.
     */
    private static void step(Compiled.DFAState state, Element element)
    {
        Compiled.Event event = new Compiled.Event(element.namespaceURI, element.localName);
        stepOneEvent(state, element, event);

        if (event.hasNamespaceUri())
        {
            stepOneEvent(state, element, event.forAnyNamespace());
            stepOneEvent(state, element, event.forAnyLocalName());
        } 
        else
        {
            stepOneEvent(state, element, event.forNamespace(DocumentHandler.SAC_NO_URI));
        }

        stepOneEvent(state, element, Compiled.Event.AnyElement);
    }

    private static void stepOneEvent(Compiled.DFAState state, Element element, Compiled.Event eventKey)
    {
        Compiled.DFAState nextState = state.events.get(eventKey);

        if (nextState != null)
        {
            traceTransition(state, nextState, eventKey);
            element.states.add(nextState);
            stepThroughConditions(nextState, element);
        }
    }

    private static void stepStates(Collection<Compiled.DFAState> states, Element element)
    {
        for (Compiled.DFAState state : states)
        {
            step(state, element);
        }
    }

    private static void stepThroughConditions(Compiled.DFAState state, Element element)
    {
        for (Condition c : state.candidateConditions.keySet())
        {
            Compiled.DFAState nextState = state.candidateConditions.get(c);

            if (nextState != null && checkCondition(element, c))
            {
                traceTransition(state, nextState, c);
                element.states.add(nextState);
            }
        }
    }

    private static void traceElement(String qName, Attributes atts)
    {
        if (trace)
        {
            System.out.print(qName + ": ");

            for (int i = 0; i < atts.getLength(); ++i)
            {
                System.out.print(atts.getQName(i) + "=" + atts.getValue(i) + " ");
            }

            System.out.println();
        }
    }

    private static void traceTransition(Compiled.DFAState from, Compiled.DFAState to, Object event)
    {
        if (trace)
        {
            System.out.println(
                    String.valueOf(from.state) + " -> " + String.valueOf(to.state)
                    + ": "
                    + (event instanceof Condition
                            ? Util.conditionText((Condition) event) 
                            : event.toString())
            );
        }
    }

    private static class Element
    {
        private Attributes attributes;
        private List<Element> children = new ArrayList<>();
        private String language;
        private String localName;
        private String namespaceURI;
        private Element parent;
        private Set<Compiled.DFAState> states = new HashSet<>();

        private Element(String namespaceURI, String localName)
        {
            this.namespaceURI = namespaceURI != null ? namespaceURI : "";
            this.localName = localName;
        }
    } // Element

    private interface TestAttribute
    {
        public boolean test(Attributes atts, int i, AttributeCondition c);
    } // TestAttribute
} // Matcher
