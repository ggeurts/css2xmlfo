package be.re.xml.sax;

import be.re.xml.CatalogResolver;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class Util
{
    public static XMLReader getParser(CatalogResolver catalogResolver, boolean validating) throws SAXException
    {
        try
        {
            return getParser(newSAXParserFactory(validating), catalogResolver);
        }
        catch (ParserConfigurationException e)
        {
            throw new SAXException(e);
        }
    }

    public static XMLReader getParser(SAXParserFactory factory, CatalogResolver catalogResolver) throws SAXException
    {
        try
        {
            XMLReader parser = factory.newSAXParser().getXMLReader();
            parser.setErrorHandler(new ErrorHandler(false));

            if (catalogResolver != null)
            {
                parser.setEntityResolver(catalogResolver);
                trySchemaLocation(parser, catalogResolver);
            }
            return parser;
        }
        catch (ParserConfigurationException e)
        {
            throw new SAXException(e);
        }
    }

    private static void trySchemaLocation(XMLReader parser, CatalogResolver resolver)
    {
        try
        {
            String schemaLocation = "";
            for (Object key : resolver.getSystemIdentifierMappings().keySet())
            {
                schemaLocation += key + " " + key + " ";
            }
            parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaLocation);
        }
        catch (Exception e)
        {
            // NOP
        }
    }
    
    public static SAXParserFactory newSAXParserFactory(boolean validating) throws ParserConfigurationException
    {
        try
        {
            String className = be.re.util.Util.getSystemProperty("javax.xml.parsers.SAXParserFactory");
            SAXParserFactory factory = className != null
                    ? (SAXParserFactory) Class.forName(className).newInstance()
                    : SAXParserFactory.newInstance();

            factory.setNamespaceAware(true);
            factory.setValidating(validating);
            
            trySetParserFeature(factory, "http://apache.org/xml/features/validation/schema", validating);
            trySetParserFeature(factory, "http://apache.org/xml/features/validation/schema-full-checking", validating);
            trySetParserFeature(factory, "http://apache.org/xml/features/non-validating/load-external-dtd", validating);

            return factory;
        }
        catch (Exception e)
        {
            throw new ParserConfigurationException(e.getMessage());
        }
    }

    private static void trySetParserFeature(SAXParserFactory factory, String feature, boolean value)
    {
        try
        {
            factory.setFeature(feature, value);
        }
        catch (Exception e)
        {
            // NOP
        }
    }
  
    public static SAXTransformerFactory newSAXTransformerFactory() throws TransformerConfigurationException
    {
        try
        {
            String className = be.re.util.Util.getSystemProperty("javax.xml.transform.TransformerFactory");
            return className != null
                    ? (SAXTransformerFactory)Class.forName(className).newInstance()
                    : (SAXTransformerFactory)TransformerFactory.newInstance();
        }
        catch (Exception e)
        {
            throw new TransformerConfigurationException(e);
        }
    }
} // Util
