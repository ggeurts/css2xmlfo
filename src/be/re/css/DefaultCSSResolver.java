/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

/**
 * Default implementation of {@link CSSResolver} interface. Retrieves and
 * caches CSS style sheets. This class is thread-safe.
 * @author Gerke Geurts
 */
public class DefaultCSSResolver implements CSSResolver
{
    ConcurrentMap<URL, CSSRuleSet> cachedRules = new ConcurrentHashMap<>();
    
    @Override
    public CSSRuleSet getRuleSet(URL styleSheetUrl) throws CSSException
    {
        CSSRuleSet result = cachedRules.get(styleSheetUrl);
        if (result != null) return result;
        
        result = parseStyleSheet(styleSheetUrl);
        return cachedRules.putIfAbsent(styleSheetUrl, result);
    }
    
    private CSSRuleSet parseStyleSheet(URL styleSheetUrl) throws CSSException
    {
        try
        {
            InputSource source = new InputSource(styleSheetUrl.toString());
            CSSRuleSet.Builder builder = new CSSRuleSet.Builder(styleSheetUrl, this);
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
}
