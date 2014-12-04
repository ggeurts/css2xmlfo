package be.re.css;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Places elements with the display type "foreign" in an
 * fo:instream-foreign-object element and removes attributes in the CSS
 * namespace below it.
 * @author Werner Donn\u00e9
 */

class ForeignFilter extends XMLFilterImpl

{

  private Stack	stack = new Stack();



  ForeignFilter()
  {
  }



  ForeignFilter(XMLReader parent)
  {
    super(parent);
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    boolean	foreign = ((Boolean) stack.pop()).booleanValue();
    boolean	foreignParent =
      !stack.isEmpty() && ((Boolean) stack.peek()).booleanValue();

    super.endElement(namespaceURI, localName, qName);

    if (!foreignParent && foreign)
    {
      super.endElement
      (
        Constants.XSLFO,
        "instream-foreign-object",
        "fo:instream-foreign-object"
      );
    }
  }



  private static Attributes
  removeCSS(Attributes atts)
  {
    AttributesImpl	result = new AttributesImpl();

    for (int i = 0; i < atts.getLength(); ++i)
    {
      if
      (
        !Constants.CSS.equals(atts.getURI(i))		&&
        !Constants.SPECIF.equals(atts.getURI(i))
      )
      {
        result.addAttribute
        (
          atts.getURI(i),
          atts.getLocalName(i),
          atts.getQName(i),
          atts.getType(i),
          atts.getValue(i)
        );
      }
    }

    return result;
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
    boolean	foreignParent =
      !stack.isEmpty() && ((Boolean) stack.peek()).booleanValue();
    boolean	foreign =
      foreignParent ||
        "foreign".equals(atts.getValue(Constants.CSS, "display"));

    if (!foreignParent && foreign)
    {
      super.startElement
      (
        Constants.XSLFO,
        "instream-foreign-object",
        "fo:instream-foreign-object",
        new AttributesImpl()
      );
    }

    stack.push(new Boolean(foreign));

    super.startElement
    (
      namespaceURI,
      localName,
      qName,
      foreign ? removeCSS(atts) : atts
    );
  }

} // ForeignFilter
