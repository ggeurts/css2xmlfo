package be.re.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Context
{
    List<CssPageRule> pageRules = new ArrayList<>();
    Map<String, Map<String, org.w3c.dom.Element>> regions = new HashMap<>();
} // Context
