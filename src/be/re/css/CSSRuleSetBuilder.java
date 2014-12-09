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
 * @author Gerke Geurts
 */
public interface CSSRuleSetBuilder
{
    URL getUrl();
    void addPageRule(CSSPageRule pageRule);
    void addRule(CSSRule rule);
    void include(String uri) throws CSSException;
}
