package be.re.xml.sax;

import be.re.xml.CatalogResolver;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;



public class Util

{

  public static XMLReader
  getParser(URL catalog, boolean validating) throws SAXException
  {
    try
    {
      return getParser(newSAXParserFactory(validating), catalog);
    }

    catch (ParserConfigurationException e)
    {
      throw new SAXException(e);
    }
  }



  public static XMLReader
  getParser(SAXParserFactory factory, URL catalog)
    throws SAXException
  {
    try
    {
      XMLReader	parser = factory.newSAXParser().getXMLReader();

      parser.setErrorHandler(new ErrorHandler(false));

      if (catalog != null)
      {
        CatalogResolver	resolver = new CatalogResolver(catalog);

        parser.setEntityResolver(resolver);
        trySchemaLocation(parser, resolver);
      }

      return parser;
    }

    catch (SAXException e)
    {
      throw e;
    }

    catch (Exception e)
    {
      throw new SAXException(e);
    }
  }



  public static SAXParserFactory
  newSAXParserFactory(boolean validating) throws ParserConfigurationException
  {
    try
    {
      String		className =
        be.re.util.Util.getSystemProperty("javax.xml.parsers.SAXParserFactory");
      SAXParserFactory	factory =
        className != null ?
          (SAXParserFactory) Class.forName(className).newInstance() :
          SAXParserFactory.newInstance();

      factory.setNamespaceAware(true);
      factory.setValidating(validating);
      tryFactoryProperties(factory, validating);

      return factory;
    }

    catch (Exception e)
    {
      throw new ParserConfigurationException(e.getMessage());
    }
  }



  public static SAXTransformerFactory
  newSAXTransformerFactory() throws TransformerConfigurationException
  {
    return (SAXTransformerFactory) be.re.xml.Util.newTransformerFactory();
  }



  public static XMLFilter
  newTemplatesFilter
  (
    Templates			templates,
    Map				parameters,
    SAXTransformerFactory	factory
  ) throws TransformerConfigurationException
  {
    if
    (
      !"net.sf.saxon.PreparedStylesheet".equals(templates.getClass().getName())
    )
    {
      return
        factory.
          newXMLFilter(new ParameterizableTemplate(templates, parameters));
    }

    XMLFilter	result = factory.newXMLFilter(templates);

    try
    {
      be.re.xml.Util.setTransformerParameters
      (
        (Transformer)
          result.getClass().getMethod("getTransformer", new Class[0]).
            invoke(result, new Object[0]),
        parameters
      );
    }

    catch (Exception e)
    {
      throw new TransformerConfigurationException(e);
    }

    return result;
  }



  public static TransformerHandler
  newTemplatesHandler
  (
    Templates			templates,
    Map				parameters,
    SAXTransformerFactory	factory
  ) throws TransformerConfigurationException
  {
    TransformerHandler	result = factory.newTransformerHandler(templates);

    be.re.xml.Util.
      setTransformerParameters(result.getTransformer(), parameters);

    return result;
  }



  private static void
  tryFactoryProperties(SAXParserFactory factory, boolean validating)
  {
    try
    {
      factory.setFeature
      (
        "http://apache.org/xml/features/validation/schema",
        validating
      );

      factory.setFeature
      (
        "http://apache.org/xml/features/validation/schema-full-checking",
        validating
      );
    }

    catch (Exception e)
    {
    }
  }



  private static void
  trySchemaLocation(XMLReader parser, CatalogResolver resolver)
  {
    try
    {
      String		schemaLocation = "";

      for
      (
        Iterator i = resolver.getSystemIdentifierMappings().keySet().iterator();
        i.hasNext();
      )
      {
        String	key = (String) i.next();

        schemaLocation +=
          key + " " + key + " ";
          //key + " " + resolver.getSystemIdentifierMappings().get(key) + " ";
          // The postman always rings twice.
      }

      parser.setProperty
      (
        "http://apache.org/xml/properties/schema/external-schemaLocation",
        schemaLocation
      );
    }

    catch (Exception e)
    {
    }
  }



  private static class ParameterizableTemplate implements Templates

  {

    private Templates	delegate;
    private Map		parameters;



    private
    ParameterizableTemplate(Templates delegate, Map parameters)
    {
      this.delegate = delegate;
      this.parameters = parameters;
    }



    public Properties
    getOutputProperties()
    {
      return delegate.getOutputProperties();
    }



    public Transformer
    newTransformer() throws TransformerConfigurationException
    {
      Transformer	transformer = delegate.newTransformer();

      be.re.xml.Util.setTransformerParameters(transformer, parameters);

      return transformer;
    }

  } // ParameterizableTemplate

} // Util
