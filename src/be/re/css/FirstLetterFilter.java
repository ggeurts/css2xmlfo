package be.re.css;

import be.re.xml.Accumulator;
import be.re.xml.DOMToContentHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



class FirstLetterFilter extends XMLFilterImpl

{

  FirstLetterFilter()
  {
  }



  FirstLetterFilter(XMLReader parent)
  {
    super(parent);
  }



  private static Element
  getFirstLetter(Node node)
  {
    return
      node == null ?
        null :
        (
          Constants.CSS.equals(node.getNamespaceURI()) &&
            "first-letter".equals(node.getLocalName()) ?
            (Element) node :
            getFirstLetter(node.getNextSibling())
        );
  }



  private static Text
  getFirstTextNode(Node node)
  {
    return
      node == null ?
        null :
        (
          node instanceof Text && ((Text) node).getLength() > 0 ?
            (Text) node :
            (
              node instanceof Element ?
                (
                  "inline".equals
                  (
                    ((Element) node).getAttributeNS(Constants.CSS, "display")
                  ) ? getFirstTextNode(node.getFirstChild()) : null
                ) :
                (
                  node.getNextSibling() != null ?
                    getFirstTextNode(node.getNextSibling()) :
                    getFirstTextNode(node.getParentNode().getNextSibling())
                )
            )
        );
  }



  private static Map
  getOriginalProperties(Element firstLetter)
  {
    NamedNodeMap	attributes = firstLetter.getAttributes();
    Map			result = new HashMap();

    for (int i = 0; i < attributes.getLength(); ++i)
    {
      if (Constants.CSS.equals(attributes.item(i).getNamespaceURI()))
      {
        result.put
        (
          attributes.item(i).getLocalName(),
          attributes.item(i).getNodeValue()
        );
      }
    }

    return result;
  }



  private static boolean
  isPunctuation(char c)
  {
    return
      Character.getType(c) == Character.END_PUNCTUATION ||
        Character.getType(c) == Character.START_PUNCTUATION ||
        Character.getType(c) == Character.INITIAL_QUOTE_PUNCTUATION ||
        Character.getType(c) == Character.FINAL_QUOTE_PUNCTUATION ||
        Character.getType(c) == Character.OTHER_PUNCTUATION;
  }



  private static void
  mergeProperties(Element firstLetter, Node text)
  {
    for
    (
      Node n = text.getParentNode();
      n.getParentNode() != null;
      n = n.getParentNode()
    )
    {
      NamedNodeMap	attributes = n.getAttributes();

      for (int i = 0; i < attributes.getLength(); ++i)
      {
        if
        (
          Constants.CSS.equals(attributes.item(i).getNamespaceURI())	&&
          Util.isInherited(attributes.item(i).getLocalName())		&&
          firstLetter.getAttributeNS
          (
            Constants.CSS,
            attributes.item(i).getLocalName()
          ).equals("")
        )
        {
          firstLetter.setAttributeNS
          (
            Constants.CSS,
            "css:" + attributes.item(i).getLocalName(),
            attributes.item(i).getNodeValue()
          );
        }
      }
    }
  }



  private static void
  removeOriginalProperties(Element element, Map properties)
  {
    for (Iterator i = properties.keySet().iterator(); i.hasNext();)
    {
      element.removeAttributeNS(Constants.CSS, (String) i.next());
    }
  }



  private static void
  setOriginalProperties(Element element, Map properties)
  {
    for (Iterator i = properties.keySet().iterator(); i.hasNext();)
    {
      String	localName = (String) i.next();

      element.setAttributeNS
      (
        Constants.CSS,
        "css:" + localName,
        (String) properties.get(localName)
      );
    }
  }



  private static void
  splitText(Element firstLetter, Text text, int offset)
  {
    firstLetter.appendChild
    (
      firstLetter.getOwnerDocument().
        createTextNode(text.getData().substring(0, offset))
    );

    mergeProperties(firstLetter, text);

    text.getParentNode().insertBefore
    (
      text.getOwnerDocument().createTextNode(text.getData().substring(offset)),
      text
    );

    text.getParentNode().removeChild(text);
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
    if
    (
      "block".equals(atts.getValue(Constants.CSS, "display"))		&&
      "1".equals(atts.getValue(Constants.CSS, "has-first-letter"))
    )
    {
      Accumulator.preAccumulate
      (
        namespaceURI,
        localName,
        qName,
        atts,
        this,
        new Accumulator.ProcessElement()
        {
          public void
          process(Element element, XMLFilter filter) throws SAXException
          {
            DOMToContentHandler.elementToContentHandler
            (
              transform(element),
              filter.getContentHandler()
            );
          }
        }
      );
    }
    else
    {
      super.startElement(namespaceURI, localName, qName, atts);
    }
  }



  private static Element
  transform(Element element)
  {
    element.removeAttributeNS(Constants.CSS, "has-first-letter");

    Element	firstLetter = getFirstLetter(element.getFirstChild());

    if (firstLetter == null)
    {
      return element;
    }

    Map	originalProperties = getOriginalProperties(firstLetter);

    firstLetter.setAttributeNS(Constants.CSS, "css:display", "inline");

    Text	text = getFirstTextNode(firstLetter.getNextSibling());

    if (text == null)
    {
      return element;
    }

    if (isPunctuation(text.getData().charAt(0)))
    {
      if (text.getLength() > 1)
      {
        splitText(firstLetter, text, 2);
      }
      else
      {
        Text	nextText =
          getFirstTextNode
          (
            text.getNextSibling() != null ?
              text.getNextSibling() : text.getParentNode().getNextSibling()
          );

        if (nextText == null)
        {
          splitText(firstLetter, text, 1);
        }
        else
        {
          Element	second = (Element) firstLetter.cloneNode(true);

          element.insertBefore(second, firstLetter.getNextSibling());
          splitText(firstLetter, text, 1);
          splitText(second, nextText, 1);
        }
      }
    }
    else
    {
      splitText(firstLetter, text, 1);
    }

    String	floatValue = firstLetter.getAttributeNS(Constants.CSS, "float");

    if (!"".equals(floatValue) && !"none".equalsIgnoreCase(floatValue))
    {
      wrapInFloat
      (
        firstLetter,
        floatValue,
        firstLetter.getAttributeNS(Constants.CSS, "clear"),
        originalProperties
      );
    }

    return element;
  }



  private static void
  wrapInFloat
  (
    Element	firstLetter,
    String	floatValue,
    String	clearValue,
    Map		originalProperties
  )
  {
    Element	block =
      firstLetter.getOwnerDocument().
        createElementNS(Constants.CSS, "css:block");
    Element	floating =
      firstLetter.getOwnerDocument().
        createElementNS(Constants.CSS, "css:float");

    floating.appendChild(block);
    block.setAttributeNS(Constants.CSS, "css:display", "block");
    floating.setAttributeNS(Constants.CSS, "css:float", floatValue);

    if (!"".equals(clearValue))
    {
      floating.setAttributeNS(Constants.CSS, "css:clear", clearValue);
    }

    Map	blockProperties = new HashMap(originalProperties);
    Map	inlineProperties = new HashMap(originalProperties);

    blockProperties.remove("float");
    blockProperties.remove("clear");
    blockProperties.remove("vertical-align");
    inlineProperties.remove("vertical-align");

    setOriginalProperties(block, blockProperties);
    firstLetter.getParentNode().insertBefore(floating, firstLetter);
    removeOriginalProperties(firstLetter, inlineProperties);

    Element	second =
      Constants.CSS.equals(firstLetter.getNextSibling().getNamespaceURI()) &&
        "first-letter".equals(firstLetter.getNextSibling().getLocalName()) ?
        (Element) firstLetter.getNextSibling() : null;

    block.appendChild(firstLetter);

    if (second != null)
    {
      removeOriginalProperties(second, inlineProperties);
      block.appendChild(second);
    }
  }

} // FirstLetterFilter
