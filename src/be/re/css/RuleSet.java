/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gerke Geurts
 */
public class RuleSet
{
    private Compiled compiled = new Compiled();
    private final List<CSSPageRule> pageRules = new ArrayList<>();
    private int position = 0;
    private boolean isStale;
    
    public Compiled getCompiledRules()
    {
        if (isStale)
        {
            compiled.generateDFA();
            isStale = false;
        }
        return compiled;
    }
    
    public List<CSSPageRule> getPageRules()
    {
        return pageRules;
    }
    
    /**
     * Use values like -1, 0 and +1 for <code>offset</code>. This will shift the
     * specificity up or down, which is needed to account for the style sheet
     * source.
     */
    public void addRuleSet(CSSRuleSet ruleSet, int offset)
    {
        for (CSSRuleSet include : ruleSet.getIncludesRecursive())
        {
            addRuleSetCore(include, offset);
        }
        addRuleSetCore(ruleSet, offset);
    }

    private void addRuleSetCore(CSSRuleSet cssRuleSet, int offset)
    {
        for (CSSRule rule : cssRuleSet.getRules())
        {
            addRule(rule, offset);
        }
        pageRules.addAll(cssRuleSet.getPageRules());
    }

    public void addRule(CSSRule cssRule, int offset)
    {
        for (Property p : cssRule.getProperties())
        {
            int specificity = cssRule.getSpecificity() + offset * 10000000;
            compiled.addRule(new Rule(cssRule, p, position++, specificity));
            isStale = true;
        }
    }
  
    public void clear()
    {
        compiled = new Compiled();
        pageRules.clear();
        position = 0;
    }
}
