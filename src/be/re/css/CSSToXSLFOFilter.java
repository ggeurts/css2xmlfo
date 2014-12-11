package be.re.css;

import be.re.xml.sax.FilterOfFilters;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.TransformerConfigurationException;
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
    private XMLFilterImpl filter;
    private PageSetupFilter pageSetupFilter;
    private Util.PostProjectionFilter postProjectionFilter;
    private ProjectorFilter projectorFilter;
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
