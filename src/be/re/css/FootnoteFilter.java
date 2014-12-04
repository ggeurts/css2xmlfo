package be.re.css;

import be.re.xml.Accumulator;
import be.re.xml.DOMToContentHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Processes footnotes.
 * @author Werner Donn\u00e9
 */

class FootnoteFilter extends XMLFilterImpl

{

  private Element	footnoteReference = null;



  FootnoteFilter()
  {
  }



  FootnoteFilter(XMLReader parent)
  {
    super(parent);
  }



  public void
  characters(char[] ch, int start, int length) throws SAXException
  {
    if (footnoteReference != null)
    {
      if (!Util.isWhitespace(ch, start, length))
      {
        flushFootnoteReference();
        super.characters(ch, start, length);
      }
    }
    else
    {
      super.characters(ch, start, length);
    }
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    flushFootnoteReference();
    super.endElement(namespaceURI, localName, qName);
  }



  private void
  flushFootnoteReference() throws SAXException
  {
    if (footnoteReference != null && getContentHandler() != null)
    {
      footnoteReference.setAttributeNS(Constants.CSS, "css:display", "inline");
      DOMToContentHandler.
        elementToContentHandler(footnoteReference, getContentHandler());
      footnoteReference = null;
    }
  }



  private static Element
  getBeforePseudoElement(Node node)
  {
    return
      node == null ?
        null :
        (
          node instanceof Element &&
            Constants.CSS.equals(node.getNamespaceURI()) &&
            "before".equals(node.getLocalName()) &&
            "footnote-reference".equals
            (
              ((Element) node).getAttributeNS(Constants.CSS, "display")
            ) ?
            (Element) node :
            getBeforePseudoElement(node.getNextSibling())
        );
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
    String	display = atts.getValue(Constants.CSS, "display");

    if ("footnote-reference".equals(display))
    {
      flushFootnoteReference();

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
            footnoteReference = element;
          }
        }
      );
    }
    else
    {
      if ("footnote-body".equals(display))
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
              transform(element);
            }
          }
        );
      }
      else
      {
        flushFootnoteReference();
        super.startElement(namespaceURI, localName, qName, atts);
      }
    }
  }



  private void
  transform(Element element) throws SAXException
  {
    Element	before = getBeforePseudoElement(element.getFirstChild());

    if (footnoteReference == null && before == null)
    {
      return;
    }

    if (footnoteReference == null)
    {
      footnoteReference = before;
    }

    super.startElement
    (
      Constants.CSS,
      "footnote",
      "css:footnote",
      new AttributesImpl()
    );

    super.startElement
    (
      Constants.CSS,
      "footnote-reference",
      "css:footnote-reference",
      new AttributesImpl()
    );

    flushFootnoteReference();

    super.endElement
    (
      Constants.CSS,
      "footnote-reference",
      "css:footnote-reference"
    );

    super.startElement
    (
      Constants.CSS,
      "footnote-body",
      "css:footnote-body",
      new AttributesImpl()
    );

    element.setAttributeNS(Constants.CSS, "css:display", "block");
    DOMToContentHandler.elementToContentHandler(element, getContentHandler());
    super.endElement(Constants.CSS, "footnote-body", "css:footnote-body");
    super.endElement(Constants.CSS, "footnote", "css:footnote");
  }

} // FootnoteFilter
