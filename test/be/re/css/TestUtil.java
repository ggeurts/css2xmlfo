/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.re.css;

import be.re.xml.Accumulator;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

/**
 *
 * @author ggeurts
 */
public class TestUtil
{
    private static final DocumentBuilder documentBuilder = createDocumentBuilder();

    private static DocumentBuilder createDocumentBuilder() 
    {
        try
        {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static Accumulator createAccumulator()
    {
        return new Accumulator(documentBuilder.newDocument());
    }

    public static String toXmlString(Accumulator accumulator) throws TransformerException
    {
        if (accumulator != null)
        {
            Document document = accumulator.getDocument();
            if (document != null) return toXmlString(document);
        }
        return null;
    }
    
    public static String toXmlString(Document document) throws TransformerException
    {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(document), new StreamResult(sw));
        return sw.toString();
    }
    
    public static String filterXmlString(String xml, XMLFilter filter) throws Exception
    {
        XMLReader parser = be.re.xml.sax.Util.getParser(null, false);
        Accumulator accumulator = createAccumulator();
        
        if (filter != null)
        {
            filter.setParent(parser);
            filter.setContentHandler(accumulator);
        }
        else
        {
            parser.setContentHandler(accumulator);
        }
        parser.parse(xml);
        return accumulator.getDocument().toString();
    }
}
