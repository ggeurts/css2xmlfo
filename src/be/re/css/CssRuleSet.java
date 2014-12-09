/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

/**
 * Collection of parsed rules, page rules and inclusions of other style sheets.
 * This collection is immutable after construction by a CssRuleSet.Builder 
 * instance.
 * @author Gerke Geurts
 */
public class CssRuleSet
{
    private final URL url;
    private final List<CssRule> rules = new ArrayList<>();
    private final List<CssPageRule> pageRules = new ArrayList<>();
    private final List<CssRuleSet> includes = new ArrayList<>();
    private final int offset;
    
    public CssRuleSet(URL url)
    {
        this.url = url;
        this.offset = 0;
    }

    public CssRuleSet(int offset)
    {
        this.url = null;
        this.offset = offset;
    }
    
    public URL getUrl()
    {
        return url;
    }
    
    public int getOffset()
    {
        return offset;
    }

    public List<CssRuleSet> getIncludes()
    {
        return includes;
    }

    public List<CssRuleSet> getIncludesRecursive()
    {
        List<CssRuleSet> result = new ArrayList<>();
        addIncludesRecursive(result);
        return result;
    }
    
    public boolean hasIncludeRecursive(URL cssUrl)
    {
        for (CssRuleSet include : includes)
        {
            if (cssUrl.equals(include.url) || include.hasIncludeRecursive(cssUrl)) return true;
        }
        return false;
    }
    
    private void addIncludesRecursive(List<CssRuleSet> result)
    {
        for (CssRuleSet include : includes)
        {
            if (!result.contains(include))
            {
                include.addIncludesRecursive(result);
                result.add(include);
            }
        }
    }

    public List<CssRule> getRules()
    {
        return rules;
    }
    
    public List<CssPageRule> getPageRules()
    {
        return pageRules;
    }

    /**
     * Parses an external CSS2 style sheet.
     * @param styleSheetUrl The style sheet URL.
     * @param cssResolver The resolver to use to retrieve included style sheets.
     * @return A rule set with the parsed CSS rules.
     * @throws CSSException 
     */
    public static CssRuleSet parse(URL styleSheetUrl, CssResolver cssResolver) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(styleSheetUrl.toString());
            Builder builder = new CssRuleSet.Builder(styleSheetUrl, cssResolver);
            CssRuleCollector collector = new CssRuleCollector(builder);
            Parser parser = Util.getSacParser();
            parser.setDocumentHandler(collector);
            parser.parseStyleSheet(source);
            return builder.getRuleSet();
        }
        catch (IOException e)
        {
            throw new CSSException(e);
        }
    }

    /**
     * Parses an embedded CSS2 style sheet.
     * @param styleSheetUrl The URL of document that contains the embedded style sheet.
     * @param cssResolver The resolver to use to retrieve included style sheets.
     * @return A rule set with the parsed CSS rules.
     * @throws CSSException 
     */
    public static CssRuleSet parse(URL baseUrl, String styleSheet, CssResolver cssResolver) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(new StringReader(styleSheet));
            Builder builder = new Builder(baseUrl, cssResolver);
            CssRuleCollector collector = new CssRuleCollector(builder);
            Parser parser = Util.getSacParser();
            parser.setDocumentHandler(collector);
            parser.parseStyleSheet(source);
            return builder.getRuleSet();
        }
        catch (IOException e)
        {
            throw new CSSException(e);
        }
    }

    /**
     * Parses an embedded CSS2 style sheet.
     * @param styleSheetUrl The URL of document that contains the embedded style sheet.
     * @param cssResolver The resolver to use to retrieve included style sheets.
     * @return A rule set with the parsed CSS rules.
     * @throws CSSException 
     */
    public static CssRuleSet parse(String styleSheet) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(new StringReader(styleSheet));
            Builder builder = new Builder(null, null);
            CssRuleCollector collector = new CssRuleCollector(builder);
            Parser parser = Util.getSacParser();
            parser.setDocumentHandler(collector);
            parser.parseStyleSheet(source);
            return builder.getRuleSet();
        }
        catch (IOException e)
        {
            throw new CSSException(e);
        }
    }
    
    public static class Builder 
    {
        CssRuleSet ruleSet;
        CssResolver resolver;
        
        public Builder(URL url, CssResolver resolver)
        {
            this.ruleSet = new CssRuleSet(url);
            this.resolver = resolver;
        }

        public URL getUrl()
        {
            return ruleSet.url;
        }
        
        public CssRuleSet getRuleSet()
        {
            return ruleSet;
        }

        public void include(String uri) throws CSSException
        {
            if (resolver == null) return;
            
            try
            {
                URL cssUrl = ruleSet.url != null
                        ? new URL(ruleSet.url, uri)
                        : new URL(uri);
                include(cssUrl);
            }
            catch (MalformedURLException e)
            {
                throw new CSSException(e);
            }
        }
        
        public void include(URL cssUrl) throws CSSException
        {
            if (resolver == null) return;

            if (cssUrl != null && !ruleSet.hasIncludeRecursive(cssUrl))
            {
                ruleSet.includes.add(resolver.getRuleSet(cssUrl));
            }
        }
        
        public void addRule(CssRule rule)
        {
            ruleSet.rules.add(rule);
        }
    
        public void addPageRule(CssPageRule pageRule)
        {
            ruleSet.pageRules.add(pageRule);
        }
    }
}
