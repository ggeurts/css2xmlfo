package be.re.css;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
 * @author Werner Donn\u00e9
 */

public class Matcher implements ContentHandler

{

  private static final String	DEFAULT_LANGUAGE = "en-GB";

  private Stack			elements = new Stack();
  private Compiled.DFAState	startState;
  private static final boolean	trace =
    System.getProperty("be.re.css.trace") != null;



  public
  Matcher(Compiled styleSheet)
  {
    startState = styleSheet.startState;
  }



  public void
  characters(char[] ch, int start, int length) throws SAXException
  {
  }



  private static boolean
  checkAttributeCondition(Element e, AttributeCondition c, TestAttribute test)
  {
    if (c.getNamespaceURI() != null)
    {
      int	index =
        DocumentHandler.SAC_NO_URI.equals(c.getNamespaceURI()) ?
          e.attributes.getIndex(c.getLocalName()) :
          e.attributes.getIndex(c.getNamespaceURI(), c.getLocalName());

      return index != -1 && test.test(e.attributes, index, c);
    }

    for (int i = 0; i < e.attributes.getLength(); ++i)
    {
      if
      (
        e.attributes.getLocalName(i).equals(c.getLocalName())	&&
        test.test(e.attributes, i, c)
      )
      {
        return true;
      }
    }

    return false;
  }



  private static boolean
  checkAttributeCondition(Element e, AttributeCondition c)
  {
    return
      checkAttributeCondition
      (
        e,
        c,
        new TestAttribute()
        {
          public boolean
          test(Attributes atts, int i, AttributeCondition c)
          {
            return
              c.getValue() == null || c.getValue().equals(atts.getValue(i));
          }
        }
      );
  }



  private static boolean
  checkBeginHyphenAttributeCondition(Element e, AttributeCondition c)
  {
    return
      checkAttributeCondition
      (
        e,
        c,
        new TestAttribute()
        {
          public boolean
          test(Attributes atts, int i, AttributeCondition c)
          {
            return
              atts.getValue(i).startsWith(c.getValue() + "-") ||
                atts.getValue(i).equals(c.getValue());
          }
        }
      );
  }



  private static boolean
  checkClassCondition(Element e, AttributeCondition c)
  {
    String	value;

    return
      (value = e.attributes.getValue("class")) != null &&
        hasToken(value, c.getValue());
  }



  private static boolean
  checkCondition(Element e, Condition c)
  {
    switch (c.getConditionType())
    {
      case Condition.SAC_AND_CONDITION:
        return
          checkCondition(e, ((CombinatorCondition) c).getFirstCondition()) &&
            checkCondition(e, ((CombinatorCondition) c).getSecondCondition());

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
        return
          checkCondition(e, ((CombinatorCondition) c).getFirstCondition()) ||
            checkCondition(e, ((CombinatorCondition) c).getSecondCondition());

      case Condition.SAC_POSITIONAL_CONDITION:
        return
          checkPositionalCondition
          (
            e,
            ((PositionalCondition) c).getPosition()
          );

      case Condition.SAC_PSEUDO_CLASS_CONDITION:
        return checkPseudoClassCondition(e, (AttributeCondition) c);

      default:
        return false; // Ignore non-CSS2 or irrelevant condition types.
    }
  }



  private static boolean
  checkIdCondition(Element e, AttributeCondition c)
  {
    for (int i = 0; i < e.attributes.getLength(); ++i)
    {
      if
      (
        "ID".equals(e.attributes.getType(i))		&&
        c.getValue().equals(e.attributes.getValue(i))
      )
      {
        return true;
      }
    }

    return false;
  }



  private static boolean
  checkLangCondition(Element e, LangCondition c)
  {
    return
      e.language.startsWith(((LangCondition) c).getLang() + "-") ||
        e.language.equals(((LangCondition) c).getLang());
  }



  private static boolean
  checkOneOfAttributeCondition(Element e, AttributeCondition c)
  {
    return
      checkAttributeCondition
      (
        e,
        c,
        new TestAttribute()
        {
          public boolean
          test(Attributes atts, int i, AttributeCondition c)
          {
            return hasToken(atts.getValue(i), c.getValue());
          }
        }
      );
  }



  private static boolean
  checkPositionalCondition(Element e, int position)
  {
    // The element on the top of the stack is not yet in the child list of its
    // parent. The preceding sibling is the last element in the parent's child
    // list.

    return e.parent.children.size() == position;
  }



  private static boolean
  checkPseudoClassCondition(Element e, AttributeCondition c)
  {
    return
      "after".equals(c.getValue()) || "before".equals(c.getValue()) ||
        ("first-child".equals(c.getValue()) && checkPositionalCondition(e, 0));
  }



  public void
  endDocument() throws SAXException
  {
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    Element	element = (Element) elements.pop();

    ((Element) elements.peek()).children.add(element);
    element.children = null;
  }



  public void
  endPrefixMapping(String prefix) throws SAXException
  {
  }



  private String
  getLanguage(String namespaceURI, Attributes attributes, Element parent)
  {
    String	result = null;

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



  private static Set
  getSiblingStates(Collection states)
  {
    Set	result = new HashSet();

    for (Iterator i = states.iterator(); i.hasNext();)
    {
      Object	nextState =
        ((Compiled.DFAState) i.next()).events.get(Compiled.SIBLING);

      if (nextState != null)
      {
        result.add(nextState);
      }
    }

    return result;
  }



  public void
  ignorableWhitespace(char[] ch, int start, int length) throws SAXException
  {
  }



  private static boolean
  hasToken(String s, String token)
  {
    int	i;

    return
      (i = s.indexOf(token)) != -1 && (i == 0 || s.charAt(i - 1) == ' ') &&
        (
          i == s.length() - token.length() ||
            s.charAt(i + token.length()) == ' '
        );
  }



  /**
   * Returns the rules that match a pseudo element sorted from least to most
   * specific.
   */

  public Rule[]
  matchingPseudoRules()
  {
    SortedSet	result = new TreeSet(new RuleComparator());

    for
    (
      Iterator i = ((Element) elements.peek()).states.iterator();
      i.hasNext();
    )
    {
      result.addAll(((Compiled.DFAState) i.next()).pseudoRules);
    }

    return (Rule[]) result.toArray(new Rule[0]);
  }



  /**
   * Returns the rules that match a normal element sorted from least to most
   * specific.
   */

  public Rule[]
  matchingRules()
  {
    SortedSet	result = new TreeSet(new RuleComparator());

    for
    (
      Iterator i = ((Element) elements.peek()).states.iterator();
      i.hasNext();
    )
    {
      result.addAll(((Compiled.DFAState) i.next()).rules);
    }

    return (Rule[]) result.toArray(new Rule[0]);
  }



  public void
  processingInstruction(String target, String data) throws SAXException
  {
  }



  public void
  setDocumentLocator(Locator locator)
  {
  }



  public void
  skippedEntity(String name) throws SAXException
  {
  }



  public void
  startDocument() throws SAXException
  {
    elements.clear();

    Element	root = new Element("", "/");

    root.language = DEFAULT_LANGUAGE;
    elements.push(root);
  }



  public void
  startElement
  (
    String	namespaceURI,
    String	localName,
    String	qName,
    Attributes	atts
  ) throws SAXException
  {
    Element	parent = (Element) elements.peek();
    Set		currentStates = parent.states;
    Element	element = new Element(namespaceURI, localName);

    element.attributes = atts;
    element.language = getLanguage(namespaceURI, atts, parent);
    element.parent = parent;
    elements.push(element);

    traceElement(namespaceURI + "|" + localName, atts);
    stepStates(parent.states, element);

    if (parent.children.size() > 0)
    {
      stepStates
      (
        getSiblingStates
        (
          ((Element) parent.children.get(parent.children.size() - 1)).states
        ),
        element
      );
    }

    // At every element new rules can be started, because they are relative.

    step(startState, element);
  }



  public void
  startPrefixMapping(String prefix, String uri) throws SAXException
  {
  }



  /**
   * More than one state transition can occur because when the candidate
   * conditions are fullfilled, they constitute an event. The universal
   * selector transitions are also tried.
   */

  private static void
  step(Compiled.DFAState state, Element element)
  {
    stepOneEvent
    (
      state,
      element,
      ("".equals(element.namespaceURI) ? "*" : element.namespaceURI) + "|" +
        element.localName
    );

    if (!"".equals(element.namespaceURI))
    {
      stepOneEvent(state, element, "*|" + element.localName);
      stepOneEvent(state, element, element.namespaceURI + "|*");
    }
    else
    {
      stepOneEvent
      (
        state,
        element,
        DocumentHandler.SAC_NO_URI + "|" + element.localName
      );
    }

    stepOneEvent(state, element, Compiled.ANY_ELEMENT);
  }



  private static void
  stepOneEvent(Compiled.DFAState state, Element element, String name)
  {
    Compiled.DFAState	nextState = (Compiled.DFAState) state.events.get(name);

    if (nextState != null)
    {
      traceTransition(state, nextState, name);
      element.states.add(nextState);
      stepThroughConditions(nextState, element);
    }
  }



  private static void
  stepStates(Collection states, Element element)
  {
    for (Iterator i = states.iterator(); i.hasNext();)
    {
      step((Compiled.DFAState) i.next(), element);
    }
  }



  private static void
  stepThroughConditions(Compiled.DFAState state, Element element)
  {
    for
    (
      Iterator i = state.candidateConditions.keySet().iterator();
      i.hasNext();
    )
    {
      Condition		c = (Condition) i.next();
      Compiled.DFAState	nextState =
        (Compiled.DFAState) state.candidateConditions.get(c);

      if (nextState != null && checkCondition(element, c))
      {
        traceTransition(state, nextState, c);
        element.states.add(nextState);
      }
    }
  }



  private static void
  traceElement(String qName, Attributes atts)
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



  private static void
  traceTransition(Compiled.DFAState from, Compiled.DFAState to, Object event)
  {
    if (trace)
    {
      System.out.println
      (
        String.valueOf(from.state) + " -> " + String.valueOf(to.state) +
          ": " +
          (
            event instanceof Condition ?
              Util.conditionText((Condition) event) : event.toString()
          )
      );
    }
  }



  private static class Element

  {

    private Attributes	attributes;
    private List	children = new ArrayList();
    private String	language;
    private String	localName;
    private String	namespaceURI;
    private Element	parent;
    private Set		states = new HashSet();



    private
    Element(String namespaceURI, String localName)
    {
      this.namespaceURI = namespaceURI != null ? namespaceURI : "";
      this.localName = localName;
    }

  } // Element



  private interface TestAttribute

  {

    public boolean	test	(Attributes atts, int i, AttributeCondition c);

  } // TestAttribute

} // Matcher
