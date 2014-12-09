package be.re.css;

import java.util.HashMap;
import java.util.Map;

public class Context
{
    public final RuleSet ruleSet = new RuleSet();
    public final Map<String, Map<String, org.w3c.dom.Element>> regions = new HashMap<>();
    
    public void clear()
    {
        ruleSet.clear();
        regions.clear();
    }
} // Context
