/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import be.re.css.Util.PostProjectionFilter;
import be.re.xml.CatalogResolver;
import be.re.xml.sax.FilterOfFilters;
import be.re.xml.sax.ProtectEventHandlerFilter;
import be.re.xml.sax.TransformerHandlerFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

/**
 * A factory of {@link CSSToXSLFOFilter} instances. This factory caches resolved 
 * style sheets, XSLT templates and other factories.
 * @author Gerke Geurts
 */
public class CSSToXSLFOConverter
{
    private final CatalogResolver catalogResolver;
    private final CSSResolver cssResolver;
    private final SAXTransformerFactory transformerFactory;
    private final Templates transformerTemplates;
    private boolean debug;
    private boolean validate;

    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public CSSToXSLFOConverter(URL catalog) throws IOException, TransformerConfigurationException
    {
        cssResolver = createCssResolver();
        catalogResolver = createCatalogResolver(catalog);
        transformerFactory = createTransformerFactory();
        transformerTemplates = transformerFactory.newTemplates(
                new StreamSource(getClass().getResource("style/css.xsl").openStream()));
    }
    
    public CatalogResolver getCatalogResolver()
    {
        return catalogResolver;
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

    public boolean getValidate()
    {
        return validate;
    }
    public void setValidate(boolean value)
    {
        validate = value;
    }

    public void convert(InputSource source, ContentHandler out, URL baseUrl, URL userAgentStyleSheet, Map<String, String> userAgentParameters, URL[] preprocessors) throws SAXException, TransformerConfigurationException, IOException
    {
        XMLReader parser = be.re.xml.sax.Util.getParser(catalogResolver, validate);
        XMLFilter parent = new ProtectEventHandlerFilter(true, true, parser);

        if (preprocessors != null)
        {
            parent = Util.createPreprocessorFilter(preprocessors, parent);
        }

        if (baseUrl != null)
        {
            source.setSystemId(baseUrl.toString());
        }
        else if(source.getSystemId() != null)
        {
            baseUrl = Util.createUrl(source.getSystemId());
        }

        XMLFilter filter = createFilter(baseUrl, userAgentStyleSheet, userAgentParameters);
        filter.setParent(parent);
        filter.setContentHandler(out);
        filter.parse(source);
    }
    
    protected CatalogResolver createCatalogResolver(URL catalog) throws IOException
    {
        if (catalog == null)
        {
            catalog = getClass().getResource("dtd/catalog");
        }
        return new CatalogResolver(catalog);
    }

    protected CSSResolver createCssResolver() throws IOException
    {
        return new DefaultCSSResolver();
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
                    URL xslUrl = getXslUrl(href, base);
                    return new StreamSource(xslUrl.openStream());
                }
                catch (MalformedURLException e)
                {
                    return null;
                }
                catch (IOException e)
                {
                    return null;
                }
            }
        });
        return result;
    }

    private URL getXslUrl(String href, String baseUrl) throws MalformedURLException
    {
        if (baseUrl != null)
        {
            try 
            {
                return new URL(new URL(baseUrl), href);
            }
            catch (MalformedURLException e)
            {
                // NOP
            }
        }
        return new URL(getClass().getResource("style/css.xsl"), href);
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
    
    protected XMLFilter createFilter(URL baseUrl, URL userAgentStyleSheet, Map<String, String> userAgentParameters) throws TransformerConfigurationException
    {
        if (userAgentParameters == null)
        {
            userAgentParameters = new HashMap<>();
        }

        Context context = new Context();

        XMLFilter projectorFilter = new ProjectorFilter(baseUrl, userAgentStyleSheet, userAgentParameters, context, cssResolver);
        PostProjectionFilter postProjectionFilter = Util.createPostProjectionFilter(baseUrl, userAgentParameters, debug);
        XMLFilter pageSetupFilter = new PageSetupFilter(context, baseUrl, userAgentParameters, debug);

        XMLFilter[] nestedFilters = new XMLFilter[]
        {
            projectorFilter,
            new FOMarkerFilter(),
            postProjectionFilter.getFilter(),
            pageSetupFilter,
            createTransformerHandlerFilter(userAgentParameters),
            new SpaceCorrectionFilter()
        };

        return new FilterOfFilters(nestedFilters, debug);
    }
}
