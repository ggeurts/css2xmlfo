package be.re.xml;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * With this class a DOM document can be fed into a SAX chain.
 * @author Werner Donn\u00e9
 */

public class DOMToContentHandler

{

  /**
   * Extracts the attributes of an element for use in a SAX-environment. It
   * leaves out namespace declarations, which are emitted as events.
   */

  public static Attributes
  createAttributes(Element element)
  {
    AttributesImpl	attributes = new AttributesImpl();
    NamedNodeMap	map = element.getAttributes();

    for (int i = 0; i < map.getLength(); ++i)
    {
      Attr	attribute = (Attr) map.item(i);

      if (attribute.getNamespaceURI() != null)
      {
        attributes.addAttribute
        (
          attribute.getNamespaceURI(),
          attribute.getLocalName(),
          attribute.getPrefix() != null ?
            (attribute.getPrefix() + ":" + attribute.getLocalName()) :
            attribute.getLocalName(),
          "CDATA",
          attribute.getValue()
        );
      }
      else
      {
        if
        (
          !"xmlns".equals(attribute.getName())		&&
          !attribute.getName().startsWith("xmlns:")
        )
        {
          attributes.addAttribute
          (
            "",
            attribute.getName(),
            attribute.getName(),
            "CDATA",
            attribute.getValue()
          );
        }
      }
    }

    return attributes;
  }



  /**
   * Runs a complete DOM-document through a <code>ContentHandler</code>.
   */

  public static void
  documentToContentHandler(Document document, ContentHandler handler)
    throws SAXException
  {
    XMLFilterImpl	h = realHandler(handler);

    h.startDocument();
    elementToContentHandler(document.getDocumentElement(), h);
    h.endDocument();
  }



  /**
   * Runs a complete DOM-element through a <code>ContentHandler</code>.
   */

  public static void
  elementToContentHandler(Element element, ContentHandler handler)
    throws SAXException
  {
    elementToContentHandler(element, realHandler(handler));
  }



  private static void
  elementToContentHandler(Element element, XMLFilterImpl handler)
    throws SAXException
  {
    String[]	prefixes = startPrefixMappings(element, handler);

    startElement(element, handler);
    siblingsToContentHandler(element.getFirstChild(), handler);
    endElement(element, handler);

    for (int i = prefixes.length - 1; i >= 0; --i)
    {
      handler.endPrefixMapping(prefixes[i]);
    }
  }



  public static void
  endElement(Element element, ContentHandler handler) throws SAXException
  {
    if (element.getNamespaceURI() != null)
    {
      handler.endElement
      (
        element.getNamespaceURI(),
        element.getLocalName(),
        element.getPrefix() != null ?
          (element.getPrefix() + ":" + element.getLocalName()) :
          element.getLocalName()
      );
    }
    else
    {
      handler.endElement("", element.getTagName(), element.getTagName());
    }
  }



  /**
   * Puts an <code>XMLFilterImpl</code> in front of <code>handler</code> in
   * order to provide the possiblity to the latter to insert an
   * <code>Accumulator</code> dynamically.
   */

  private static XMLFilterImpl
  realHandler(ContentHandler handler)
  {
    XMLFilterImpl	result = new XMLFilterImpl();

    result.setContentHandler(handler);

    if (handler instanceof XMLFilter)
    {
      ((XMLFilter) handler).setParent(result);
    }

    return result;
  }



  /**
   * Runs a complete sibling list through a <code>ContentHandler</code>
   * starting with <code>node</code>.
   */

  public static void
  siblingsToContentHandler(Node node, ContentHandler handler)
    throws SAXException
  {
    siblingsToContentHandler(node, realHandler(handler));
  }



  private static void
  siblingsToContentHandler(Node node, XMLFilterImpl handler)
    throws SAXException
  {
    if (node == null)
    {
      return;
    }

    switch (node.getNodeType())
    {
      case Node.ELEMENT_NODE:
        elementToContentHandler((Element) node, handler);
        break;

      case Node.PROCESSING_INSTRUCTION_NODE:
        handler.processingInstruction
        (
          ((ProcessingInstruction) node).getTarget(),
          ((ProcessingInstruction) node).getData()
        );

        break;

      case Node.TEXT_NODE:
        {
          char[]	chars = ((Text) node).getData().toCharArray();

          handler.characters(chars, 0, chars.length);
        }

        break;
    }

    siblingsToContentHandler(node.getNextSibling(), handler);
  }



  public static void
  startElement(Element element, ContentHandler handler) throws SAXException
  {
    if (element.getNamespaceURI() != null)
    {
      handler.startElement
      (
        element.getNamespaceURI(),
        element.getLocalName(),
        element.getPrefix() != null ?
          (element.getPrefix() + ":" + element.getLocalName()) :
          element.getLocalName(),
        createAttributes(element)
      );
    }
    else
    {
      handler.startElement
      (
        "",
        element.getTagName(),
        element.getTagName(),
        createAttributes(element)
      );
    }
  }



  private static String[]
  startPrefixMappings(Element element, ContentHandler handler)
    throws SAXException
  {
    NamedNodeMap	map = element.getAttributes();
    List		result = new ArrayList();

    for (int i = 0; i < map.getLength(); ++i)
    {
      Attr	attribute = (Attr) map.item(i);
      String	name = attribute.getName();

      if (name.startsWith("xmlns"))
      {
        String	prefix =
          name.startsWith("xmlns:") ?
            name.substring(name.indexOf(':') + 1) : "";

        result.add(prefix);
        handler.startPrefixMapping(prefix, attribute.getValue());
      }
    }

    return (String[]) result.toArray(new String[result.size()]);
  }

} // DOMToContentHandler
