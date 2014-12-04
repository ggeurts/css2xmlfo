package be.re.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.PositionalCondition;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;



/**
 * Represents one CSS2 rule.
 * @author Werner Donn\u00e9
 */

public class Rule

{

  private Property[]	cachedArray = null;
  private String	elementName;
  private int		position;
  private Map		properties = new HashMap();
  private String	pseudoElementName;
  private Selector	selector;
  private Selector[]	selectorChain;
  private int		specificity;



  /**
   * Use values like -1, 0 and +1 for <code>offset</code>. This will shift the
   * specificity up or down, which is needed to account for the style sheet
   * source.
   */

  Rule(Selector selector, int position, int offset)
  {
    this.selector =
      selector instanceof ElementSelector ?
        new InternedElementSelector((ElementSelector) selector) : selector;
    this.position = position;
    selectorChain = Util.getSelectorChain(selector);
    specificity = specificity() + offset * 10000000;
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

  private
  Rule(Rule source, int position)
  {
    this.selector = source.selector;
    this.position = position;
    this.elementName = source.elementName;
    this.pseudoElementName = source.pseudoElementName;
    this.selectorChain = source.selectorChain;
    this.specificity = source.specificity;
  }



  void
  addProperty(Property property)
  {
    properties.put(property.getName(), property);
  }



  /**
   * Returns the interned name of the element this rule applies to. If it
   * doesn't apply to an element <code>null</code> is returned.
   */

  public String
  getElementName()
  {
    return elementName;
  }



  private static String
  getElementName(Selector[] selectorChain, int position)
  {
    switch (selectorChain[position].getSelectorType())
    {
      case Selector.SAC_ELEMENT_NODE_SELECTOR:
        return
          ((ElementSelector) selectorChain[position]).
            getLocalName();
      case Selector.SAC_PSEUDO_ELEMENT_SELECTOR:
        return getElementName(selectorChain, position - 1);

      default: return null;
    }
  }



  int
  getPosition()
  {
    return position;
  }



  Property[]
  getProperties()
  {
    if (cachedArray == null || cachedArray.length != properties.size())
    {
      cachedArray =
        (Property[])
          properties.values().toArray(new Property[properties.size()]);
    }

    return cachedArray;
  }



  public Property
  getProperty()
  {
    Property[]	result = getProperties();

    if (result.length != 1)
    {
      throw new RuntimeException("Unsplit rule");
    }

    return result[0];
  }



  private static void
  getPseudoClassConditions(Condition c, List result)
  {
    if (c.getConditionType() == Condition.SAC_PSEUDO_CLASS_CONDITION)
    {
      result.add(((AttributeCondition) c).getValue());
    }
    else
    {
      if (c.getConditionType() == Condition.SAC_AND_CONDITION)
      {
        getPseudoClassConditions
        (
          ((CombinatorCondition) c).getFirstCondition(),
          result
        );

        getPseudoClassConditions
        (
          ((CombinatorCondition) c).getSecondCondition(),
          result
        );
      }
    }
  }



  /**
   * Returns the interned pseudo element name or <code>null</code> if the rule
   * doesn't apply to a pseudo element.
   */

  public String
  getPseudoElementName()
  {
    return pseudoElementName;
  }



  private static String
  getPseudoElementName(Selector[] selectorChain)
  {
    if
    (
      selectorChain[selectorChain.length - 1].getSelectorType() ==
        Selector.SAC_PSEUDO_ELEMENT_SELECTOR
    )
    {
      return
        ((ElementSelector) selectorChain[selectorChain.length - 1]).
          getLocalName();
    }

    if
    (
      selectorChain.length > 1						&&
      selectorChain[selectorChain.length - 2].getSelectorType() ==
        Selector.SAC_CONDITIONAL_SELECTOR
    )
    {
      List	conditions = new ArrayList();

      getPseudoClassConditions
      (
        ((ConditionalSelector) selectorChain[selectorChain.length - 2]).
          getCondition(),
        conditions
      );

      return
        conditions.contains("before") ?
          "before" :
         (
           conditions.contains("after") ?
             "after" :
             (conditions.contains("first-line") ?  "first-line" : null)
         );
    }

    return null;
  }



  /**
   * Returns the selector that matches the rule.
   */

  public Selector
  getSelector()
  {
    return selector;
  }



  /**
   * Flattens the selector expression tree in infix order.
   */

  Selector[]
  getSelectorChain()
  {
    return selectorChain;
  }



  int
  getSpecificity()
  {
    return specificity;
  }



  private int
  specificity()
  {
    Specificity	s = new Specificity();

    specificity(selector, s);

    return 10000 * s.ids + 100 * s.attributes + s.names;
  }



  private static void
  specificity(Selector selector, Specificity s)
  {
    if (selector instanceof ConditionalSelector)
    {
      specificity(((ConditionalSelector) selector).getCondition(), s);
      specificity(((ConditionalSelector) selector).getSimpleSelector(), s);
    }
    else
    {
      if (selector instanceof DescendantSelector)
      {
        specificity(((DescendantSelector) selector).getAncestorSelector(), s);
        specificity(((DescendantSelector) selector).getSimpleSelector(), s);
      }
      else
      {
        if (selector instanceof NegativeSelector)
        {
          specificity(((NegativeSelector) selector).getSimpleSelector(), s);
        }
        else
        {
          if (selector instanceof SiblingSelector)
          {
            specificity(((SiblingSelector) selector).getSelector(), s);
            specificity(((SiblingSelector) selector).getSiblingSelector(), s);
          }
          else
          {
            if
            (
              selector.getSelectorType() ==
                Selector.SAC_ELEMENT_NODE_SELECTOR			&&
              ((ElementSelector) selector).getLocalName() != null
                // There is no name for "*".
            )
            {
              ++s.names;
            }
          }
        }
      }
    }
  }



  private static void
  specificity(Condition c, Specificity s)
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

    if
    (
      c.getConditionType() == Condition.SAC_POSITIONAL_CONDITION	&&
      ((PositionalCondition) c).getPosition() == 1
        // first-child pseudo class.
    )
    {
      ++s.attributes;
    }
  }



  /**
   * Splits this rule into a set of equivalent rules in which there is only one
   * property. For each property of this rule there will be a new one.
   */

  Rule[]
  split()
  {
    Rule[]	result = new Rule[getProperties().length];

    for (int i = 0; i < result.length; ++i)
    {
      result[i] = new Rule(this, getPosition());
      result[i].addProperty(getProperties()[i]);
    }

    return result;
  }



  private static class Specificity

  {

    private int	attributes;
    private int	ids;
    private int	names;

  } // Specificity

} // Rule
