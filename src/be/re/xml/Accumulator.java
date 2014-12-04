package be.re.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * With this class a SAX stream can be accumulated in a DOM document. An
 * instance of it can be reused. The client must call
 * <code>startDocument</code> and <code>endDocument</code>.
 * @author Werner Donn\u00e9
 */

public class Accumulator extends XMLFilterImpl

{

  private static final DocumentBuilder	documentBuilder =
    createDocumentBuilder();

  private Node		currentNode = null;
  private Document	document = null;
  private Stack		prefixMappings = new Stack();
  private Result	result = null;



  public
  Accumulator()
  {
    this((Document) null, (Result) null);
  }



  public
  Accumulator(XMLReader parent)
  {
    this((Document) null, (Result) null, parent);
  }



  public
  Accumulator(Result result)
  {
    this((Document) null, result);
  }



  public
  Accumulator(Result result, XMLReader parent)
  {
    this((Document) null, result, parent);
  }



  public
  Accumulator(Document document)
  {
    this.document = document;
  }



  public
  Accumulator(Document document, XMLReader parent)
  {
    super(parent);
    this.document = document;
  }



  public
  Accumulator(Document document, Result result)
  {
    this.document = document;
    this.result = result;
  }



  public
  Accumulator(Document document, Result result, XMLReader parent)
  {
    super(parent);
    this.document = document;
    this.result = result;
  }



  public void
  characters(char[] ch, int start, int length) throws SAXException
  {
    currentNode.
      appendChild(document.createTextNode(new String(ch, start, length)));
  }



  private static DocumentBuilder
  createDocumentBuilder()
  {
    try
    {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }



  public void
  endDocument() throws SAXException
  {
    prefixMappings.pop();
    currentNode = null;
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    prefixMappings.pop();
    currentNode = currentNode.getParentNode();

    if (result != null && currentNode == document)
    {
      result.report(this);
    }
  }



  public void
  endPrefixMapping(String prefix) throws SAXException
  {
    ((Map) prefixMappings.peek()).remove(prefix);
  }



  public Document
  getDocument()
  {
    return document;
  }



  public void
  ignorableWhitespace(char[] ch, int start, int length) throws SAXException
  {
  }



  /**
   * This installs an accumulator after <code>filter</code>. You would call it
   * in <code>startElement</code>, before you let the event go through.
   */

  public static void
  postAccumulate(XMLFilter filter, ProcessElement process) throws SAXException
  {
    final XMLFilter		f = filter;
    final ContentHandler	handler =
      filter.getContentHandler();
    final ProcessElement	p = process;
    Accumulator			accumulator =
      new Accumulator
      (
        documentBuilder.newDocument(),
        new Result()
        {
          public void
          report(Accumulator accumulator) throws SAXException
          {
            f.setContentHandler
            (
              handler != null ? handler : new XMLFilterImpl()
            );

            p.process(accumulator.getDocument().getDocumentElement(), f);
            accumulator.endDocument();
          }
        }
      );

    filter.setContentHandler(accumulator);
    accumulator.setParent(filter);
    accumulator.startDocument();
  }



  /**
   * This installs an accumulator before <code>filter</code>. You would call it
   * in <code>startElement</code>, and initialize it with the incoming event,
   * which you don't let go through.
   */

  public static void
  preAccumulate
  (
    String		namespaceURI,
    String		localName,
    String		qName,
    Attributes		atts,
    XMLFilter		filter,
    ProcessElement	process
  ) throws SAXException
  {
    if (filter.getParent() == null)
    {
      return;
    }

    final XMLFilter		f = filter;
    final ContentHandler	handler =
      filter.getParent().getContentHandler();
    final ProcessElement	p = process;
    Accumulator			accumulator =
      new Accumulator
      (
        documentBuilder.newDocument(),
        new Result()
        {
          public void
          report(Accumulator accumulator) throws SAXException
          {
            p.process(accumulator.getDocument().getDocumentElement(), f);
            accumulator.endDocument();
            f.getParent().setContentHandler(handler);
          }
        }
      );

    filter.getParent().setContentHandler(accumulator);
    accumulator.setParent(filter.getParent());
    accumulator.startDocument();
    accumulator.startElement(namespaceURI, localName, qName, atts);
  }



  public void
  processingInstruction(String target, String data) throws SAXException
  {
    currentNode.appendChild(document.createProcessingInstruction(target, data));
  }



  public void
  setDocumentLocator(Locator locator)
  {
  }



  private static void
  setAttributes(Element element, Attributes atts)
  {
    for (int i = 0; i < atts.getLength(); ++i)
    {
      if (atts.getURI(i) == null || "".equals(atts.getURI(i)))
      {
        element.setAttribute(atts.getQName(i), atts.getValue(i));
      }
      else
      {
        element.
          setAttributeNS(atts.getURI(i), atts.getQName(i), atts.getValue(i));
      }
    }
  }



  private static void
  setPrefixMappings(Element element, Map mappings)
  {
    for (Iterator i = mappings.keySet().iterator(); i.hasNext();)
    {
      String	prefix = (String) i.next();

      element.setAttribute
      (
        "".equals(prefix) ? "xmlns" : ("xmlns:" + prefix),
        (String) mappings.get(prefix)
      );
    }
  }



  public void
  skippedEntity(String name) throws SAXException
  {
  }



  public void
  startDocument() throws SAXException
  {
    if (document == null)
    {
      try
      {
        document = documentBuilder.newDocument();
      }

      catch (Exception e)
      {
        throw new SAXException(e);
      }
    }
    else
    {
      if (document.getDocumentElement() != null)
      {
        document.removeChild(document.getDocumentElement());
      }
    }

    currentNode = document;
    prefixMappings.push(new HashMap());
  }



  public void
  startElement
  (
    String	namespaceURI,
    String	localName,
    String	qName,
    Attributes	atts
  ) throws SAXException
  {
    Element	element =
      document.
        createElementNS("".equals(namespaceURI) ? null : namespaceURI, qName);

    setAttributes(element, atts);
    setPrefixMappings(element, (Map) prefixMappings.peek());
    prefixMappings.push(new HashMap());
    currentNode.appendChild(element);
    currentNode = element;
  }



  public void
  startPrefixMapping(String prefix, String uri) throws SAXException
  {
    ((Map) prefixMappings.peek()).put(prefix, uri);
  }



  public interface ProcessElement

  {

    public void	process	(Element element, XMLFilter filter) throws SAXException;

  } // ProcessElement



  public interface Result
  {
    public void	report	(Accumulator accumulator) throws SAXException;
  }

} // Accumulator
