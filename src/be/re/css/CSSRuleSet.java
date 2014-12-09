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
 * Collection of fully processed CSS2 rules, page rules and inclusions of other 
 * style sheets.
 * @author Gerke Geurts
 */
public class CSSRuleSet
{
    private final URL url;
    private final List<CSSRule> rules = new ArrayList<>();
    private final List<CSSPageRule> pageRules = new ArrayList<>();
    private final List<CSSRuleSet> includes = new ArrayList<>();
    private final int offset;
    
    public CSSRuleSet(URL url)
    {
        this.url = url;
        this.offset = 0;
    }

    public CSSRuleSet(int offset)
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

    public List<CSSRuleSet> getIncludes()
    {
        return includes;
    }

    public List<CSSRuleSet> getIncludesRecursive()
    {
        List<CSSRuleSet> result = new ArrayList<>();
        addIncludesRecursive(result);
        return result;
    }
    
    public boolean hasIncludeRecursive(URL cssUrl)
    {
        for (CSSRuleSet include : includes)
        {
            if (cssUrl.equals(include.url) || include.hasIncludeRecursive(cssUrl)) return true;
        }
        return false;
    }
    
    private void addIncludesRecursive(List<CSSRuleSet> result)
    {
        for (CSSRuleSet include : includes)
        {
            if (!result.contains(include))
            {
                include.addIncludesRecursive(result);
                result.add(include);
            }
        }
    }

    public List<CSSRule> getRules()
    {
        return rules;
    }
    
    public List<CSSPageRule> getPageRules()
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
    public static CSSRuleSet parse(URL styleSheetUrl, CSSResolver cssResolver) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(styleSheetUrl.toString());
            Builder builder = new CSSRuleSet.Builder(styleSheetUrl, cssResolver);
            CSSRuleCollector collector = new CSSRuleCollector(builder);
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
    public static CSSRuleSet parse(URL baseUrl, String styleSheet, CSSResolver cssResolver) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(new StringReader(styleSheet));
            Builder builder = new Builder(baseUrl, cssResolver);
            CSSRuleCollector collector = new CSSRuleCollector(builder);
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
    public static CSSRuleSet parse(String styleSheet) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(new StringReader(styleSheet));
            Builder builder = new Builder(null, null);
            CSSRuleCollector collector = new CSSRuleCollector(builder);
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
        CSSRuleSet ruleSet;
        CSSResolver resolver;
        
        public Builder(URL url, CSSResolver resolver)
        {
            this.ruleSet = new CSSRuleSet(url);
            this.resolver = resolver;
        }

        public URL getUrl()
        {
            return ruleSet.url;
        }
        
        public CSSRuleSet getRuleSet()
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
        
        public void addRule(CSSRule rule)
        {
            ruleSet.rules.add(rule);
        }
    
        public void addPageRule(CSSPageRule pageRule)
        {
            ruleSet.pageRules.add(pageRule);
        }
    }
}
