package be.re.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

/**
 * This class collects rules and page rules from the "all" and "print" media.
 * The other media are ignored. Rules without any properties are also ignored.
 *
 * @author Werner Donn\u00e9
 */
class CssRuleCollector implements DocumentHandler
{
    private final CssRuleSet.Builder cssBuilder;
    private final Map prefixMap = new HashMap();
    private CssPageRule currentPageRule = null;
    private final List<CssRule> currentRules = new ArrayList<>();
    private boolean ignore = false;

    CssRuleCollector(CssRuleSet.Builder cssBuilder)
    {
        this.cssBuilder = cssBuilder;
        if (cssBuilder == null)
        {
            currentRules.add(new CssRule());
        }
    }

    public List<CssRule> getRules()
    {
        return currentRules;
    }
    
    @Override
    public void comment(String text) throws CSSException
    {
    }

    @Override
    public void endDocument(InputSource source) throws CSSException
    {
    }

    @Override
    public void endFontFace() throws CSSException
    {
    }

    @Override
    public void endMedia(SACMediaList media) throws CSSException
    {
        ignore = false;
    }

    @Override
    public void endPage(String name, String pseudoPage) throws CSSException
    {
        if (currentPageRule.getProperties().isEmpty())
        {
            cssBuilder.addPageRule(currentPageRule);
        }

        currentPageRule = null;
    }

    @Override
    public void endSelector(SelectorList selectors) throws CSSException
    {
        if (!ignore)
        {
            for (CssRule rule : currentRules)
            {
                if (!rule.getProperties().isEmpty())
                {
                    cssBuilder.addRule(rule);
                }
            }
            currentRules.clear();
        }
    }

    private boolean hasOneOfMedia(SACMediaList media, String[] choices)
    {
        if (media == null)
        {
            return false;
        }

        for (int i = 0; i < media.getLength(); ++i)
        {
            for (int j = 0; j < choices.length; ++j)
            {
                if (media.item(i).equals(choices[j]))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void ignorableAtRule(String atRule) throws CSSException
    {
    }

    @Override
    public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException
    {
        if (!ignore)
        {
            if (media == null || hasOneOfMedia(media, new String[] { "all", "print" }))
            {
                cssBuilder.include(uri);
            }
        }
    }

    @Override
    public void namespaceDeclaration(String prefix, String uri) throws CSSException
    {
        prefixMap.put(prefix, uri);
    }

    @Override
    public void property(String name, LexicalUnit value, boolean important) throws CSSException
    {
        if (!ignore)
        {
            Property[] properties = new Property(name.toLowerCase(), value, important, prefixMap, cssBuilder.getUrl()).split();

            if (!currentRules.isEmpty())
            {
                for (CssRule rule : currentRules)
                {
                    for (int j = 0; j < properties.length; ++j)
                    {
                        rule.addProperty(properties[j]);
                    }
                }
            } 
            else if (currentPageRule != null)
            {
                for (int i = 0; i < properties.length; ++i)
                {
                    LexicalUnit unit = properties[i].getLexicalUnit();

                    if ("counter-reset".equals(properties[i].getName())
                            && unit.getLexicalUnitType() == LexicalUnit.SAC_IDENT
                            && "page".equals(unit.getStringValue())
                            && (unit.getNextLexicalUnit() == null || unit.getNextLexicalUnit().getLexicalUnitType() == LexicalUnit.SAC_INTEGER))
                    {
                        properties[i] = unit.getNextLexicalUnit() == null
                                ? new Property("initial-page-number", "1", properties[i].getImportant(), prefixMap)
                                : new Property("initial-page-number", unit.getNextLexicalUnit(), properties[i].getImportant(), prefixMap, cssBuilder.getUrl());
                    }
                    currentPageRule.addProperty(properties[i]);
                }
            }
        }
    }

    @Override
    public void startDocument(InputSource source) throws CSSException
    {
    }

    @Override
    public void startFontFace() throws CSSException
    {
    }

    @Override
    public void startMedia(SACMediaList media) throws CSSException
    {
        ignore = !hasOneOfMedia(media, new String[]
        {
            "all", "print"
        });
    }

    @Override
    public void startPage(final String name, final String pseudoPage) throws CSSException
    {
        if (!ignore)
        {
            currentPageRule = new CssPageRule(
                    name != null && pseudoPage != null
                            ? (pseudoPage + "-" + name)
                            : (name != null
                                    ? name 
                                    : (pseudoPage != null 
                                            ? pseudoPage 
                                            : "unnamed")));
        }
    }

    @Override
    public void startSelector(SelectorList selectors) throws CSSException
    {
        currentRules.clear();
        if (ignore || selectors.getLength() == 0) return;

        for (int i = 0; i < selectors.getLength(); ++i)
        {
            currentRules.add(new CssRule(selectors.item(i)));
        }
    }
} // RuleCollector
