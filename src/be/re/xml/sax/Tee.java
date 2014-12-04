package be.re.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * This XML filter delegates everything to multiple other handlers.
 * @author Werner Donn\u00e9
 */

public class Tee extends XMLFilterImpl

{

  private ContentHandler[]	tubes;



  public
  Tee(ContentHandler[] tubes)
  {
    this.tubes = tubes;
  }



  public
  Tee(ContentHandler[] tubes, XMLReader parent)
  {
    super(parent);
    this.tubes = tubes;
  }



  public void
  characters(char[] ch, int start, int length) throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].characters(ch, start, length);
    }

    super.characters(ch, start, length);
  }



  public void
  endDocument() throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].endDocument();
    }

    super.endDocument();
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].endElement(namespaceURI, localName, qName);
    }

    super.endElement(namespaceURI, localName, qName);
  }



  public void
  endPrefixMapping(String prefix) throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].endPrefixMapping(prefix);
    }

    super.endPrefixMapping(prefix);
  }



  public void
  ignorableWhitespace(char[] ch, int start, int length) throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].ignorableWhitespace(ch, start, length);
    }

    super.ignorableWhitespace(ch, start,length);
  }



  public void
  processingInstruction(String target, String data) throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].processingInstruction(target, data);
    }

    super.processingInstruction(target, data);
  }



  public void
  setDocumentLocator(Locator locator)
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].setDocumentLocator(locator);
    }

    super.setDocumentLocator(locator);
  }



  public void
  skippedEntity(String name) throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].skippedEntity(name);
    }

    super.skippedEntity(name);
  }



  public void
  startDocument() throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].startDocument();
    }

    super.startDocument();
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
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].startElement(namespaceURI, localName, qName, atts);
    }

    super.startElement(namespaceURI, localName, qName, atts);
  }



  public void
  startPrefixMapping(String prefix, String uri) throws SAXException
  {
    for (int i = 0; i < tubes.length; ++i)
    {
      tubes[i].startPrefixMapping(prefix, uri);
    }

    super.startPrefixMapping(prefix, uri);
  }

} // Tee
