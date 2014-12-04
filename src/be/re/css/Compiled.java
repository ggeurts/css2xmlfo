package be.re.css;

import be.re.util.DigitalTree;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;



/**
 * Represents a CSS style sheet in compiled form.
 * @author Werner Donn\u00e9
 */

public class Compiled

{

  static final String		ANY_ELEMENT = "*|*".intern();
  private static final int	END_STATE = 1;
  private static final String	EPSILON = "EPSILON".intern();
  private static final int	ERROR_STATE = -1;
  static final String		SIBLING = "SIBLING".intern();
  private static final int	START_STATE = 0;

  private int			dfaStateCounter = 0;
  private int			nfaStateCounter = 0;
  private NFAState[]		nfa =
    new NFAState[]{new NFAState(), new NFAState()};
  DFAState			startState = null;
  private static final boolean	trace =
    System.getProperty("be.re.css.trace") != null;



  /**
   * Adds the rule to the NFA being built using the Thompson construction.
   * The rule should be split, i.e. it should have exactly one property.
   */

  void
  addRule(Rule rule)
  {
    NFAState[]	states = constructNFA(rule.getSelector());

    if (rule.getPseudoElementName() == null)
    {
      states[END_STATE].rules.add(rule);
    }
    else
    {
      states[END_STATE].pseudoRules.add(rule);
    }

    nfa[START_STATE].next.add(new Next(EPSILON, states[START_STATE]));
    states[END_STATE].next.add(new Next(EPSILON, nfa[END_STATE]));
  }



  private static Map
  collectNextSets(SortedSet set)
  {
    Map	result = new HashMap();

    for (Iterator i = set.iterator(); i.hasNext();)
    {
      for (Iterator j = ((NFAState) i.next()).next.iterator(); j.hasNext();)
      {
        Next	next = (Next) j.next();

        if (next.event != EPSILON)
        {
          SortedSet	nextSet = (SortedSet) result.get(next.event);

          if (nextSet == null)
          {
            nextSet = new TreeSet(set.comparator());
            result.put(next.event, nextSet);
          }

          nextSet.add(next.state);
        }
      }
    }

    for (Iterator i = result.values().iterator(); i.hasNext();)
    {
      TreeSet	nextSet = (TreeSet) i.next();
      SortedSet	copy = (SortedSet) nextSet.clone();

      for (Iterator j = copy.iterator(); j.hasNext();)
      {
        epsilonMove(nextSet, (NFAState) j.next());
      }
    }

    return result;
  }



  private static NFAState[]
  constructAnd(NFAState[] first, NFAState[] second)
  {
    first[END_STATE].next.add(new Next(EPSILON, second[START_STATE]));

    return new NFAState[]{first[START_STATE], second[END_STATE]};
  }



  private NFAState[]
  constructChild(DescendantSelector selector)
  {
    return
      selector.getSimpleSelector().getSelectorType() ==
        Selector.SAC_PSEUDO_ELEMENT_SELECTOR ?
        constructNFA(selector.getAncestorSelector()) :
        constructAnd
        (
          constructNFA(selector.getAncestorSelector()),
          constructNFA(selector.getSimpleSelector())
        );
  }



  private NFAState[]
  constructConditional(ConditionalSelector selector)
  {
    NFAState[]	first = constructNFA(selector.getSimpleSelector());
    NFAState	end = new NFAState();

    first[END_STATE].condition = selector.getCondition();
    first[END_STATE].next.add(new Next(selector.getCondition(), end));

    return new NFAState[]{first[START_STATE], end};
  }



  private NFAState[]
  constructDescendant(DescendantSelector selector)
  {
    return
      constructAnd
      (
        constructAnd
        (
          constructNFA(selector.getAncestorSelector()),
          constructKleeneClosure(ANY_ELEMENT)
        ),
        constructNFA(selector.getSimpleSelector())
      );
  }



  private NFAState[]
  constructElement(ElementSelector selector)
  {
    NFAState	start = new NFAState();
    NFAState	end = new NFAState();

    start.next.add
    (
      new Next
      (
        (
          (
            selector.getNamespaceURI() != null ?
              selector.getNamespaceURI() : "*"
          ) + "|" +
            (
              selector.getLocalName() != null ?
                selector.getLocalName().intern() : "*"
            )
        ).intern(),
        end
      )
    );

    return new NFAState[]{start, end};
  }



  private NFAState[]
  constructKleeneClosure(Object event)
  {
    NFAState	start = new NFAState();
    NFAState	end = new NFAState();
    NFAState	from = new NFAState();
    NFAState	to = new NFAState();

    from.next.add(new Next(event, to));
    start.next.add(new Next(EPSILON, from));
    start.next.add(new Next(EPSILON, end));
    to.next.add(new Next(EPSILON, end));
    end.next.add(new Next(EPSILON, from));

    return new NFAState[]{start, end};
  }



  /**
   * Applies the Thompson construction.
   */

  private NFAState[]
  constructNFA(Selector selector)
  {
    switch (selector.getSelectorType())
    {
      case Selector.SAC_CONDITIONAL_SELECTOR:
        return constructConditional((ConditionalSelector) selector);

      case Selector.SAC_CHILD_SELECTOR:
        return constructChild((DescendantSelector) selector);

      case Selector.SAC_DESCENDANT_SELECTOR:
        return constructDescendant((DescendantSelector) selector);

      case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
        return constructSibling((SiblingSelector) selector);

      case Selector.SAC_ELEMENT_NODE_SELECTOR:
        return constructElement((ElementSelector) selector);

      default:
        return null; // Ignore non-CSS2 selector types.
    }
  }



  private NFAState[]
  constructSibling(SiblingSelector selector)
  {
    return
      constructAnd
      (
        constructAnd
        (
          constructNFA(selector.getSelector()),
          constructSiblingTransition()
        ),
        constructNFA(selector.getSiblingSelector())
      );
  }



  private NFAState[]
  constructSiblingTransition()
  {
    NFAState	start = new NFAState();
    NFAState	end = new NFAState();

    start.next.add(new Next(SIBLING, end));

    return new NFAState[]{start, end};
  }



  void
  dumpDFA(PrintWriter out)
  {
    if (trace)
    {
      out.println();
      out.println("DFA START");
      out.println();
      dumpDFA(startState, new HashSet(), out);
      out.println("DFA END");
      out.println();
      out.flush();
    }
  }



  private static void
  dumpDFA(DFAState state, Set seen, PrintWriter out)
  {
    if (seen.contains(new Integer(state.state)))
    {
      return;
    }

    out.println(String.valueOf(state.state) + ":");

    List	values = new ArrayList();

    for (Iterator i = state.events.keySet().iterator(); i.hasNext();)
    {
      String	event = (String) i.next();
      DFAState	nextState = (DFAState) state.events.get(event);

      out.println("  " + event + " -> " + String.valueOf(nextState.state));
      values.add(nextState);
    }

    for
    (
      Iterator i = state.candidateConditions.keySet().iterator();
      i.hasNext();
    )
    {
      Condition	event = (Condition) i.next();
      DFAState	nextState = (DFAState) state.candidateConditions.get(event);

      out.println
      (
        "  " + Util.conditionText(event) + " -> " +
          String.valueOf(nextState.state)
      );

      values.add(nextState);
    }

    dumpRules(state.rules, out);
    dumpRules(state.pseudoRules, out);
    out.println();
    seen.add(new Integer(state.state));

    for (Iterator i = values.iterator(); i.hasNext();)
    {
      dumpDFA((DFAState) i.next(), seen, out);
    }
  }



  void
  dumpNFA(PrintWriter out)
  {
    if (trace)
    {
      out.println();
      out.println("NFA START");
      out.println();
      dumpNFA(nfa[START_STATE], new HashSet(), out);
      out.println("NFA END");
      out.println();
      out.flush();
    }
  }



  private static void
  dumpNFA(NFAState state, Set seen, PrintWriter out)
  {
    if (seen.contains(new Integer(state.state)))
    {
      return;
    }

    out.println(String.valueOf(state.state) + ":");

    for (Iterator i = state.next.iterator(); i.hasNext();)
    {
      Next	next = (Next) i.next();

      out.println
      (
        "  " +
          (
            next.event instanceof Condition ?
              Util.conditionText((Condition) next.event) : next.event.toString()
          ) + " -> " + String.valueOf(next.state.state)
      );
    }

    dumpRules(state.rules, out);
    dumpRules(state.pseudoRules, out);
    out.println();
    seen.add(new Integer(state.state));

    for (Iterator i = state.next.iterator(); i.hasNext();)
    {
      dumpNFA(((Next) i.next()).state, seen, out);
    }
  }



  private static void
  dumpRules(List rules, PrintWriter out)
  {
    for (Iterator i = rules.iterator(); i.hasNext();)
    {
      Rule	rule = (Rule) i.next();

      out.println
      (
        "  " + (rule.getElementName() != null ? rule.getElementName() : "") +
          (
            rule.getPseudoElementName() != null ?
              rule.getPseudoElementName() : ""
          ) + ": " + rule.getProperty().getName() + ": " +
          rule.getProperty().getValue()
      );
    }
  }



  private static void
  epsilonMove(Set set, NFAState state)
  {
    for (Iterator i = state.next.iterator(); i.hasNext();)
    {
      Next	next = (Next) i.next();

      if (next.event == EPSILON && set.add(next.state))
      {
        epsilonMove(set, next.state);
      }
    }
  }



  void
  generateDFA()
  {
    dumpNFA(new PrintWriter(System.out));
    startState = generateDFA(nfa);
    dumpDFA(new PrintWriter(System.out));
  }



  /**
   * Applies the subset construction. Returns the start state.
   */

  private DFAState
  generateDFA(NFAState[] nfa)
  {
    SortedSet	set =
      new TreeSet // Sorting of the NFA states makes the labels unique.
      (
        new Comparator()
        {
          public int
          compare(Object o1, Object o2)
          {
            return ((NFAState) o1).state - ((NFAState) o2).state;
          }
        }
      );
    Map		states = new HashMap();

    set.add(nfa[START_STATE]);
    epsilonMove(set, nfa[START_STATE]);

    DFAState	result = new DFAState();

    states.put(label(set), result);
    generateTransitions(result, set, states);

    return result;
  }



  private void
  generateTransitions(DFAState from, SortedSet set, Map states)
  {
    Map		nextSets = collectNextSets(set);

    for (Iterator i = nextSets.keySet().iterator(); i.hasNext();)
    {
      Object	event = i.next();
      SortedSet	nextSet = (SortedSet) nextSets.get(event);

      if (nextSet.size() > 0)
      {
        DFAState	nextState;
        String		s = label(nextSet);
        DFAState	state = (DFAState) states.get(s);

        if (state == null)
        {
          nextState = new DFAState();
          states.put(s, nextState);
        }
        else
        {
          nextState = state;
        }

        if (event instanceof Condition)
        {
          from.candidateConditions.put(event, nextState);
        }
        else
        {
          from.events.put((String) event, nextState);
        }

        for (Iterator j = nextSet.iterator(); j.hasNext();)
        {
          NFAState	next = (NFAState) j.next();

          nextState.rules.addAll(next.rules);
          nextState.pseudoRules.addAll(next.pseudoRules);
        }

        if (state == null)
        {
          generateTransitions(nextState, nextSet, states);
        }
      }
    }
  }



  private static String
  label(SortedSet set)
  {
    String	result = "";

    for (Iterator i = set.iterator(); i.hasNext();)
    {
      result += "#" + String.valueOf(((NFAState) i.next()).state);
    }

    return result;
  }



  /**
   * Contains all matching rules sorted from least to most specific.
   */

  class DFAState

  {

    Map		candidateConditions = new HashMap();
    DigitalTree	events = new DigitalTree(trace);
    List	pseudoRules = new ArrayList();
    List	rules = new ArrayList();
    int		state;



    private
    DFAState()
    {
      state = dfaStateCounter++;
    }

  } // DFAState



  private static class Next

  {

    private
    Next(Object event, NFAState state)
    {
      this.event = event;
      this.state = state;
    }



    private Object	event;
    private NFAState	state;

  } // Next



  private class NFAState

  {

    private Condition	condition;
    private List	next = new ArrayList();
    private List	pseudoRules = new ArrayList();
    private List	rules = new ArrayList();
    private int		state;



    private
    NFAState()
    {
      state = nfaStateCounter++;
    }

  } // NFAState

} // Compiled
