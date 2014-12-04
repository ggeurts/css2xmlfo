package be.re.css;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * This filter removes elements with the display type "wrapper". The inherited
 * properties on the element are propagated to its children.
 * @author Werner Donn\u00e9
 */

class WrapperFilter extends XMLFilterImpl

{

  private Stack	elements = new Stack();



  WrapperFilter()
  {
  }



  WrapperFilter(XMLReader parent)
  {
    super(parent);
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    if (!((Element) elements.pop()).remove)
    {
      super.endElement(namespaceURI, localName, qName);
    }
  }



  private static Attributes
  mergeInheritedProperties(Attributes atts, Attributes inheritedProperties)
  {
    return
      inheritedProperties == null ?
        atts : Util.mergeAttributes(inheritedProperties, atts);
  }



  private static Attributes
  selectInheritedProperties(Attributes atts)
  {
    AttributesImpl	result = new AttributesImpl();

    for (int i = 0; i < atts.getLength(); ++i)
    {
      if
      (
        Constants.CSS.equals(atts.getURI(i))	&&
        Util.isInherited(atts.getLocalName(i))
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
    Element	element = new Element();

    if ("wrapper".equals(atts.getValue(Constants.CSS, "display")))
    {
      element.remove = true;
      element.inheritedProperties = selectInheritedProperties(atts);
    }
    else
    {
      super.startElement
      (
        namespaceURI,
        localName,
        qName,
        mergeInheritedProperties
        (
          atts,
          elements.isEmpty() ?
            null : ((Element) elements.peek()).inheritedProperties
        )
      );
    }

    elements.push(element);
  }



  private static class Element

  {

    private Attributes	inheritedProperties;
    private boolean	remove;

  } // Element

} // WrapperFilter
