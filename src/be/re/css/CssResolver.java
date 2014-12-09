/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import java.net.URL;
import org.w3c.css.sac.CSSException;

/**
 * Interface for resolver of external CSS style sheets.
 * @author Gerke Geurts
 */
public interface CssResolver
{
    /**
     * Retrieves and parses style sheet rules from a given CSS document URL.
     * @param styleSheetUrl The style sheet location.
     * @return The compiled rule set as retrieved from styleSheetUrl.
     */
    CssRuleSet getRuleSet(URL styleSheetUrl) throws CSSException;
}
