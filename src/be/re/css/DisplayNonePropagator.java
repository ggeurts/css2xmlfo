package be.re.css;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Propagates a "none" display type down the tree, which makes it unnecessary
 * for subsequent filters to analyse the ancestor chain. Regions can't have
 * their display set to "none", so in that case the display property it set to
 * "block".
 * @author Werner Donn\u00e9
 */

class DisplayNonePropagator extends XMLFilterImpl

{

  private Stack	stack = new Stack();



  DisplayNonePropagator()
  {
  }



  DisplayNonePropagator(XMLReader parent)
  {
    super(parent);
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    super.endElement(namespaceURI, localName, qName);
    stack.pop();
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
    boolean	displayNone =
      (stack.isEmpty() ? false : ((Boolean) stack.peek()).booleanValue()) ||
        (
          "none".equals(atts.getValue(Constants.CSS, "display")) &&
            atts.getValue(Constants.CSS, "region") == null
        );

    stack.push(new Boolean(displayNone));

    if (displayNone)
    {
      atts = new AttributesImpl(atts);

      Util.setAttribute
      (
        (AttributesImpl) atts,
        Constants.CSS,
        "display",
        "css:display",
        "none"
      );
    }
    else
    {
      if
      (
        atts.getValue(Constants.CSS, "region") != null		&&
        "none".equals(atts.getValue(Constants.CSS, "display"))
      )
      {
        atts = new AttributesImpl(atts);

        Util.setAttribute
        (
          (AttributesImpl) atts,
          Constants.CSS,
          "display",
          "css:display",
          "block"
        );
      }
    }

    super.startElement(namespaceURI, localName, qName, atts);
  }

} // DisplayNonePropagator
