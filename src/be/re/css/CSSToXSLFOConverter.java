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
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

/**
 * Conversion of CSS to XSL-FO. A single instance can perform multiple conversions
 * efficiently, due to caching of XSLT templates and CSS style sheets.
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

    /**
     * Creates a new converter instance.
     * @param catalog The DTD catalog to use. A <code>null</code> value indicates that 
     * the default XHTML catalog is to be used.
     * @throws IOException
     * @throws TransformerConfigurationException 
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public CSSToXSLFOConverter(URL catalog) throws IOException, TransformerConfigurationException
    {
        cssResolver = createCssResolver();
        catalogResolver = createCatalogResolver(catalog);
        transformerFactory = createTransformerFactory();
        transformerTemplates = transformerFactory.newTemplates(
                new StreamSource(getClass().getResource("style/css.xsl").openStream()));
    }
    
    /**
     * The catalog resolver used to efficiently retrieve external DTDs.
     * @return
     */
    public CatalogResolver getCatalogResolver()
    {
        return catalogResolver;
    }

    /**
     * The CSS resolver used for retrieval of external style sheets.
     * @return 
     */
    public CSSResolver getCssResolver()
    {
        return cssResolver;
    }

    /**
     * Indicates whether debug information is written
     * @return 
     */
    public boolean getDebug()
    {
        return debug;
    }
    /**
     * Enables or disables writing of debug information
     * @param value 
     */
    public void setDebug(boolean value)
    {
        debug = value;
    }

    /**
     * Indicates whether source XML documents are validated.
     * @return 
     */
    public boolean getValidate()
    {
        return validate;
    }
    /**
     * Enables or disables validation of source XML documents.
     * @param value 
     */
    public void setValidate(boolean value)
    {
        validate = value;
    }

    /**
     * Performs conversion of XML document to XSL-FO content.
     * @param source The source XML document.
     * @param out The {@link ContentHandler} that will receive the XSL-FO output.
     * @param baseUrl Optional base URL to be used for resolution of relative URLs.
     * @param userAgentStyleSheet Optional URL of CSS stylesheet to be used. A 
     * <code>null</code> value indicates that the default CSS stylesheet for XHTML 
     * is to be used.
     * @param userAgentParameters Optional parameters.
     * @param preprocessor Optional {@link XMLFilter} that preprocesses XML input 
     * before the transformation to XSL-FO output.
     * @param postprocessor Optional {@link XMLFilter} that post-processes XSL-FO output.
     * @throws SAXException
     * @throws TransformerConfigurationException
     * @throws IOException 
     */
    public void convert(
            InputSource source, 
            ContentHandler out, 
            URL baseUrl, 
            URL userAgentStyleSheet, 
            Map<String, String> userAgentParameters, 
            XMLFilter preprocessor, 
            XMLFilter postprocessor) throws SAXException, TransformerConfigurationException, IOException
    {
        if (baseUrl != null)
        {
            source.setSystemId(baseUrl.toString());
        }
        else if(source.getSystemId() != null)
        {
            baseUrl = Util.createUrl(source.getSystemId());
        }

        XMLFilter filter = createFilter(baseUrl, userAgentStyleSheet, userAgentParameters, preprocessor, postprocessor);
        XMLReader parser = be.re.xml.sax.Util.getParser(catalogResolver, validate);
        filter.setParent(parser);
        filter.setContentHandler(out);
        filter.parse(source);
    }

    /**
     * Creates {@link SAXSource} for conversion of XML document to XSL-FO content.
     * @param source The source XML document
     * @param baseUrl Optional base URL to be used for resolution of relative URLs.
     * @param userAgentStyleSheet Optional URL of CSS stylesheet to be used. A 
     * <code>null</code> value indicates that the default CSS stylesheet for XHTML 
     * is to be used.
     * @param userAgentParameters Optional parameters.
     * @param preprocessor Optional {@link XMLFilter} that preprocesses XML input 
     * before the transformation to XSL-FO output.
     * @param postprocessor Optional {@link XMLFilter} that post-processes XSL-FO output.
     * @return
     * @throws SAXException
     * @throws TransformerConfigurationException
     * @throws IOException 
     */
    public SAXSource createSAXSource(
            InputSource source, 
            URL baseUrl, 
            URL userAgentStyleSheet, 
            Map<String, String> userAgentParameters, 
            XMLFilter preprocessor, 
            XMLFilter postprocessor) throws SAXException, TransformerConfigurationException, IOException
    {
        if (baseUrl != null)
        {
            source.setSystemId(baseUrl.toString());
        }
        else if(source.getSystemId() != null)
        {
            baseUrl = Util.createUrl(source.getSystemId());
        }

        XMLFilter filter = createFilter(baseUrl, userAgentStyleSheet, userAgentParameters, preprocessor, postprocessor);
        XMLReader parser = be.re.xml.sax.Util.getParser(catalogResolver, validate);
        filter.setParent(parser);
        return new SAXSource(filter, source);
    }

    /**
     * Creates a {@link XMLFilter} that can act as preprocessor for <code>convert</code>.
     * @param xslUrls An arbitrary number of URLs that point to XSLT templates.
     * @return
     * @throws TransformerConfigurationException 
     */
    public XMLFilter createPreprocessorFilter(URL[] xslUrls) throws TransformerConfigurationException
    {
        if (xslUrls == null || xslUrls.length == 0) return null;
        
        XMLFilter[] filters = new XMLFilter[xslUrls.length];
        for (int i = 0; i < xslUrls.length; ++i)
        {
            filters[i] = transformerFactory.newXMLFilter(new StreamSource(xslUrls[i].toString()));
        }
        return new FilterOfFilters(filters);
    }
    
    protected CatalogResolver createCatalogResolver(URL catalog) throws IOException
    {
        if (catalog == null)
        {
            catalog = getClass().getResource("/catalog");
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
    
    private TransformerHandlerFilter createTransformerHandlerFilter(Map<String, String> userAgentParameters) throws TransformerConfigurationException
    {
        TransformerHandler transformerHandler = transformerFactory.newTransformerHandler(transformerTemplates);
        Transformer transformer = transformerHandler.getTransformer();
        for (Map.Entry<String, String> entry : userAgentParameters.entrySet())
        {
            transformer.setParameter(entry.getKey(), entry.getValue());
        }
        return new TransformerHandlerFilter(transformerHandler);
    }

    private XMLFilter createFilter(
            URL baseUrl, 
            URL userAgentStyleSheet, 
            Map<String, String> userAgentParameters, 
            XMLFilter preprocessor, 
            XMLFilter postprocessor) throws TransformerConfigurationException, SAXException
    {
        XMLReader parser = be.re.xml.sax.Util.getParser(catalogResolver, validate);
        XMLFilter parent = new ProtectEventHandlerFilter(true, true, parser);
        if (preprocessor != null)
        {
            preprocessor.setParent(parent);
        }
        XMLFilter filter = createFilterCore(baseUrl, userAgentStyleSheet, userAgentParameters);
        if (postprocessor != null)
        {
            filter.setParent(postprocessor);
            filter = postprocessor;
        }
        return filter;
    }
    
    private XMLFilter createFilterCore(
            URL baseUrl, 
            URL userAgentStyleSheet, 
            Map<String, String> userAgentParameters) throws TransformerConfigurationException
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
