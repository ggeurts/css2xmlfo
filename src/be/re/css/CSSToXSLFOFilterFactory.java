/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import be.re.xml.sax.TransformerHandlerFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.XMLReader;

/**
 * A factory of {@link CSSToXSLFOFilter} instances. This factory caches resolved 
 * style sheets, XSLT templates and other factories.
 * @author Gerke Geurts
 */
public class CSSToXSLFOFilterFactory
{
    private final CSSResolver cssResolver;
    private final SAXTransformerFactory transformerFactory;
    private final Templates transformerTemplates;
    private boolean debug;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public CSSToXSLFOFilterFactory() throws CSSToXSLFOException
    {
        cssResolver = new DefaultCSSResolver();
        
        try
        {
            transformerFactory = createTransformerFactory();
            transformerTemplates = transformerFactory.newTemplates(
                    new StreamSource(
                            CSSToXSLFOFilter.class.getResource("style/css.xsl").openStream()));
        }
        catch (IOException | TransformerConfigurationException e)
        {
            throw new CSSToXSLFOException(e);
        }
    }
    
    public CSSResolver getCssResolver()
    {
        return cssResolver;
    }
    
    public SAXTransformerFactory getTransformerFactory()
    {
        return transformerFactory;
    }
    
    public Templates getTransformerTemplates()
    {
        return transformerTemplates;
    }
    
    public boolean getDebug()
    {
        return debug;
    }
    public void setDebug(boolean value)
    {
        debug = value;
    }
    
    protected SAXTransformerFactory createTransformerFactory() throws TransformerConfigurationException
    {
        SAXTransformerFactory result = be.re.xml.sax.Util.newSAXTransformerFactory();
        result.setURIResolver(new URIResolver()
        {
            @Override
            public Source resolve(String href, String base)
            {
                try
                {
                    URL xslUrl = base != null && be.re.net.Util.isUrl(base)
                            ? new URL(new URL(base), href)
                            : new URL(CSSToXSLFOFilter.class.getResource("style/css.xsl"), href);
                    return new StreamSource(xslUrl.openStream());
                }
                catch (Exception e)
                {
                    return null;
                }
            }
        });
        return result;
    }

    public TransformerHandlerFilter createTransformerHandlerFilter(Map<String, String> userAgentParameters) throws TransformerConfigurationException
    {
        TransformerHandler transformerHandler = transformerFactory.newTransformerHandler(transformerTemplates);
        Transformer transformer = transformerHandler.getTransformer();
        for (Map.Entry<String, String> entry : userAgentParameters.entrySet())
        {
            transformer.setParameter(entry.getKey(), entry.getValue());
        }
        return new TransformerHandlerFilter(transformerHandler);
    }
    
    public CSSToXSLFOFilter createFilter(URL baseUrl, URL userAgentStyleSheet, Map<String, String> userAgentParameters, XMLReader parent) throws CSSToXSLFOException
    {
        CSSToXSLFOFilter result = new CSSToXSLFOFilter(this, baseUrl, userAgentStyleSheet, userAgentParameters);
        result.setParent(parent);
        return result;
    }
}
