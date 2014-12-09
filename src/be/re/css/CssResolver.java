/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import java.net.URL;
import org.w3c.css.sac.CSSException;

/**
 *
 * @author ggeurts
 */
public interface CssResolver
{
    /**
     * Retrieves and parses style sheet rules from a given CSS document URL.
     * @param styleSheetUrl The style sheet location.
     * @return The compiled rule set as retrieved from styleSheetUrl.
     */
    CssRuleSet getRuleSet(URL styleSheetUrl) throws CSSException;

    /**
     * Retrieves and parses style sheet rules from a given CSS string.
     * @param baseUrl Optional URL of document from which style sheet contents originate.
     * @param styleSheet The style sheet contents.
     * @return The compiled rule set as retrieved from styleSheetUrl.
     */
    CssRuleSet getRuleSet(URL baseUrl, String styleSheet) throws CSSException;
}
