package be.re.css;

import be.re.xml.sax.FilterOfFilters;
import be.re.xml.sax.TransformerHandlerFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.URIResolver;
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
 * @author Werner Donne\u00e9
 */

public class CSSToXSLFOFilter extends XMLFilterImpl

{

  private static SAXTransformerFactory	factory;
  private XMLFilterImpl			filter;
  private PageSetupFilter		pageSetupFilter;
  private Util.PostProjectionFilter	postProjectionFilter;
  private ProjectorFilter		projectorFilter;
  private static Templates		templates = loadStyleSheet();
  private Map				userAgentParameters;



  public
  CSSToXSLFOFilter() throws CSSToXSLFOException
  {
    this(null, null, new HashMap(), false);
  }



  public
  CSSToXSLFOFilter(URL baseUrl) throws CSSToXSLFOException
  {
    this(baseUrl, null, new HashMap(), false);
  }



  public
  CSSToXSLFOFilter(URL baseUrl, URL userAgentStyleSheet)
    throws CSSToXSLFOException
  {
    this(baseUrl, userAgentStyleSheet, new HashMap(), false);
  }



  public
  CSSToXSLFOFilter
  (
    URL	baseUrl,
    URL	userAgentStyleSheet,
    Map	userAgentParameters
  ) throws CSSToXSLFOException
  {
    this(baseUrl, userAgentStyleSheet, userAgentParameters, false);
  }



  public
  CSSToXSLFOFilter
  (
    URL		baseUrl,
    URL		userAgentStyleSheet,
    Map		userAgentParameters,
    boolean	debug
  ) throws CSSToXSLFOException
  {
    this.userAgentParameters =
      userAgentParameters != null ? userAgentParameters : new HashMap();

    try
    {
      Context	context = new Context();

      projectorFilter =
        new ProjectorFilter
        (
          baseUrl,
          userAgentStyleSheet,
          userAgentParameters,
          context
        );

      postProjectionFilter =
        Util.createPostProjectionFilter(baseUrl, userAgentParameters, debug);
      pageSetupFilter =
        new PageSetupFilter(context, baseUrl, userAgentParameters, debug);

      filter =
        new FilterOfFilters
        (
          new XMLFilter[]
          {
            projectorFilter,
            new FOMarkerFilter(),
            postProjectionFilter.getFilter(),
            pageSetupFilter,
            new TransformerHandlerFilter
            (
              be.re.xml.sax.Util.
                newTemplatesHandler(templates, userAgentParameters, factory)
            ),
            new SpaceCorrectionFilter()
          },
          debug
        );

      super.setContentHandler(filter);
      super.setDTDHandler(filter);
      super.setEntityResolver(filter);
      super.setErrorHandler(filter);
    }

    catch (Exception e)
    {
      throw new CSSToXSLFOException(e);
    }
  }



  public
  CSSToXSLFOFilter(XMLReader parent) throws CSSToXSLFOException
  {
    this(null, null, new HashMap(), parent, false);
  }



  public
  CSSToXSLFOFilter(URL baseUrl, XMLReader parent) throws CSSToXSLFOException
  {
    this(baseUrl, null, new HashMap(), parent, false);
  }



  public
  CSSToXSLFOFilter(URL baseUrl, URL userAgentStyleSheet, XMLReader parent)
    throws CSSToXSLFOException
  {
    this(baseUrl, userAgentStyleSheet, new HashMap(), parent, false);
  }



  public
  CSSToXSLFOFilter
  (
    URL		baseUrl,
    URL		userAgentStyleSheet,
    Map		userAgentParameters,
    XMLReader	parent
  ) throws CSSToXSLFOException
  {
    this(baseUrl, userAgentStyleSheet, userAgentParameters, parent, false);
  }



  public
  CSSToXSLFOFilter
  (
    URL		baseUrl,
    URL		userAgentStyleSheet,
    Map		userAgentParameters,
    XMLReader	parent,
    boolean	debug
  ) throws CSSToXSLFOException
  {
    this(baseUrl, userAgentStyleSheet, userAgentParameters, debug);
    setParent(parent);
  }



  public URL
  getBaseUrl()
  {
    return projectorFilter.getBaseUrl();
  }



  public ContentHandler
  getContentHandler()
  {
    return filter.getContentHandler();
  }



  public DTDHandler
  getDTDHandler()
  {
    return filter.getDTDHandler();
  }



  public EntityResolver
  getEntityResolver()
  {
    return filter.getEntityResolver();
  }



  public ErrorHandler
  getErrorHandler()
  {
    return filter.getErrorHandler();
  }



  public Map
  getParameters()
  {
    return userAgentParameters;
  }



  public URL
  getUserAgentStyleSheet()
  {
    return projectorFilter.getUserAgentStyleSheet();
  }



  private static Templates
  loadStyleSheet()
  {
    try
    {
      factory = be.re.xml.sax.Util.newSAXTransformerFactory();

      factory.setURIResolver
      (
        new URIResolver()
        {
          public Source
          resolve(String href, String base)
          {
            try
            {
              return
                new StreamSource
                (
                  base != null && be.re.net.Util.isUrl(base) ?
                    new URL(new URL(base), href).toString() :
                    new URL
                    (
                      CSSToXSLFOFilter.class.getResource("style/css.xsl"), href
                    ).toString()
                );
            }

            catch (Exception e)
            {
              return null;
            }
          }
        }
      );

      return
        factory.newTemplates
        (
          new StreamSource
          (
            CSSToXSLFOFilter.class.getResource("style/css.xsl").toString()
          )
        );
    }

    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }



  public void
  parse(InputSource input) throws IOException, SAXException
  {
    if (getBaseUrl() == null && input.getSystemId() != null)
    {
      setBaseUrl(new URL(input.getSystemId()));
    }

    filter.parse(input);
  }



  public void
  parse(String systemId) throws IOException, SAXException
  {
    if (getBaseUrl() == null && systemId != null)
    {
      setBaseUrl(new URL(systemId));
    }

    filter.parse(systemId);
  }



  public void
  setBaseUrl(URL baseUrl)
  {
    projectorFilter.setBaseUrl(baseUrl);
    pageSetupFilter.setBaseUrl(baseUrl);
    postProjectionFilter.setBaseUrl(baseUrl);
  }



  public void
  setContentHandler(ContentHandler handler)
  {
    filter.setContentHandler(handler);
  }



  public void
  setDTDHandler(DTDHandler handler)
  {
    filter.setDTDHandler(handler);
  }



  public void
  setEntityResolver(EntityResolver resolver)
  {
    filter.setEntityResolver(resolver);
  }



  public void
  setErrorHandler(ErrorHandler handler)
  {
    filter.setErrorHandler(handler);
  }



  public void
  setParameters(Map userAgentParameters)
  {
    this.userAgentParameters = userAgentParameters;
  }



  public void
  setParent(XMLReader parent)
  {
    super.setParent(parent);
      // Some XMLFilterImpl functions seem to use parent directly instead of
      // getParent.
    filter.setParent(parent);
    parent.setContentHandler(filter);
  }



  public void
  setUserAgentStyleSheet(URL userAgentStyleSheet)
  {
    projectorFilter.setUserAgentStyleSheet(userAgentStyleSheet);
  }

} // CSSToXSLFOFilter
