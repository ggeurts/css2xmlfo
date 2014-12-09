/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

/**
 *
 * @author ggeurts
 */
public class DefaultCssResolver implements CssResolver
{
    ConcurrentMap<URL, CssRuleSet> cachedRules = new ConcurrentHashMap<>();
    
    @Override
    public CssRuleSet getRuleSet(URL styleSheetUrl) throws CSSException
    {
        CssRuleSet result = cachedRules.get(styleSheetUrl);
        if (result != null) return result;
        
        result = parseStyleSheet(styleSheetUrl);
        return cachedRules.putIfAbsent(styleSheetUrl, result);
    }
    
    @Override
    public CssRuleSet getRuleSet(URL baseUrl, String styleSheet) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(new StringReader(styleSheet));
            CssRuleSet.Builder builder = new CssRuleSet.Builder(baseUrl, this);
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
    
    private CssRuleSet parseStyleSheet(URL styleSheetUrl) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(styleSheetUrl.toString());
            CssRuleSet.Builder builder = new CssRuleSet.Builder(styleSheetUrl, this);
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
}
