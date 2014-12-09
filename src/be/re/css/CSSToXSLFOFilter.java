package be.re.css;

import be.re.xml.sax.FilterOfFilters;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A filter that accepts an XML document and produces an XSL-FO document.
 *
 * @author Werner Donne\u00e9
 * @author Gerke Geurts
 */
public class CSSToXSLFOFilter extends XMLFilterImpl
{
    private static SAXTransformerFactory factory;
    private XMLFilterImpl filter;
    private PageSetupFilter pageSetupFilter;
    private Util.PostProjectionFilter postProjectionFilter;
    private ProjectorFilter projectorFilter;
    private static Templates templates = loadStyleSheet();
    private Map userAgentParameters;

    public CSSToXSLFOFilter(CSSToXSLFOFilterFactory factory, URL baseUrl, URL userAgentStyleSheet, Map<String, String> userAgentParameters) throws CSSToXSLFOException
    {
        try
        {
            this.userAgentParameters = userAgentParameters != null
                    ? userAgentParameters
                    : new HashMap();
            
            Context context = new Context();

            projectorFilter = new ProjectorFilter(baseUrl, userAgentStyleSheet, userAgentParameters, context, factory.getCssResolver());
            postProjectionFilter = Util.createPostProjectionFilter(baseUrl, userAgentParameters, factory.getDebug());
            pageSetupFilter = new PageSetupFilter(context, baseUrl, userAgentParameters, factory.getDebug());

            XMLFilter[] nestedFilters = new XMLFilter[]
            {
                projectorFilter,
                new FOMarkerFilter(),
                postProjectionFilter.getFilter(),
                pageSetupFilter,
                factory.createTransformerHandlerFilter(userAgentParameters),
                new SpaceCorrectionFilter()
            };
            filter = new FilterOfFilters(nestedFilters, factory.getDebug());

            super.setContentHandler(filter);
            super.setDTDHandler(filter);
            super.setEntityResolver(filter);
            super.setErrorHandler(filter);
        } 
        catch (TransformerConfigurationException e)
        {
            throw new CSSToXSLFOException(e);
        }
    }

    public URL getBaseUrl()
    {
        return projectorFilter.getBaseUrl();
    }

    @Override
    public ContentHandler getContentHandler()
    {
        return filter.getContentHandler();
    }

    @Override
    public DTDHandler getDTDHandler()
    {
        return filter.getDTDHandler();
    }

    @Override
    public EntityResolver getEntityResolver()
    {
        return filter.getEntityResolver();
    }

    @Override
    public ErrorHandler getErrorHandler()
    {
        return filter.getErrorHandler();
    }

    public Map getParameters()
    {
        return userAgentParameters;
    }

    public URL getUserAgentStyleSheet()
    {
        return projectorFilter.getUserAgentStyleSheet();
    }

    private static Templates loadStyleSheet()
    {
        try
        {
            factory = be.re.xml.sax.Util.newSAXTransformerFactory();

            factory.setURIResolver((String href, String base) ->
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
            });

            return factory.newTemplates(
                    new StreamSource(
                            CSSToXSLFOFilter.class.getResource("style/css.xsl").openStream()));
        } 
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException
    {
        if (getBaseUrl() == null && input.getSystemId() != null)
        {
            setBaseUrl(new URL(input.getSystemId()));
        }
        filter.parse(input);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException
    {
        if (getBaseUrl() == null && systemId != null)
        {
            setBaseUrl(new URL(systemId));
        }
        filter.parse(systemId);
    }

    private void setBaseUrl(URL baseUrl)
    {
        projectorFilter.setBaseUrl(baseUrl);
        pageSetupFilter.setBaseUrl(baseUrl);
        postProjectionFilter.setBaseUrl(baseUrl);
    }
    
    @Override
    public void setContentHandler(ContentHandler handler)
    {
        filter.setContentHandler(handler);
    }

    @Override
    public void setDTDHandler(DTDHandler handler)
    {
        filter.setDTDHandler(handler);
    }

    @Override
    public void setEntityResolver(EntityResolver resolver)
    {
        filter.setEntityResolver(resolver);
    }

    @Override
    public void setErrorHandler(ErrorHandler handler)
    {
        filter.setErrorHandler(handler);
    }

    @Override
    public void setParent(XMLReader parent)
    {
        super.setParent(parent);
        // Some XMLFilterImpl functions seem to use parent directly instead of getParent.
        filter.setParent(parent);
        parent.setContentHandler(filter);
    }
} // CSSToXSLFOFilter
