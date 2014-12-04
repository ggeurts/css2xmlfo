package be.re.xml.sax;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Blocks the document events, which is useful for inlining another SAX source
 * in a SAX result.
 * @author Werner Donn\u00e9
 */

public class GobbleDocumentEvents extends XMLFilterImpl

{

  public
  GobbleDocumentEvents()
  {
  }



  public
  GobbleDocumentEvents(XMLReader parent)
  {
    super(parent);
  }



  public void
  endDocument() throws SAXException
  {
  }



  public void
  startDocument() throws SAXException
  {
  }

} // GobbleDocumentEvents
