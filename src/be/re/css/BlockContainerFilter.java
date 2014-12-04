package be.re.css;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Checks properties to see if an element has to be wrapped in a
 * fo:block-container.
 * @author Werner Donn\u00e9
 */

class BlockContainerFilter extends XMLFilterImpl

{

  private final static Set	containerProperties =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "bottom", "clip", "height", "left", "max-height", "max-width",
            "min-height", "min-width", "orientation", "overflow", "position",
            "right", "top", "width", "z-index"
        }
      )
    );
  private final static Set	triggeringBlockProperties =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "clip", "height", "max-height", "max-width", "min-height",
            "min-width", "orientation", "overflow", "width", "z-index"
        }
      )
    );
  private Stack		stack = new Stack();



  BlockContainerFilter()
  {
  }



  BlockContainerFilter(XMLReader parent)
  {
    super(parent);
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    super.endElement(namespaceURI, localName, qName);

    if (stack.pop().equals(new Boolean(true)))
    {
      super.endElement(Constants.XSLFO, "block", "fo:block");
      super.
        endElement(Constants.XSLFO, "block-container", "fo:block-container");
    }
  }



  private static boolean
  isContainerAttribute(Attributes atts, int index)
  {
    String	name = atts.getLocalName(index);

    return
      Constants.CSS.equals(atts.getURI(index)) &&
        (
          containerProperties.contains(name) || name.startsWith("background-")
            || name.startsWith("border-") || name.startsWith("margin-") ||
            name.startsWith("padding-") || name.startsWith("page-break-")
        );
  }



  private static AttributesImpl
  selectAttributes(Attributes atts, boolean container)
  {
    AttributesImpl	result = new AttributesImpl();

    for (int i = 0; i < atts.getLength(); ++i)
    {
      if (container == isContainerAttribute(atts, i))
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



  private static boolean
  shouldWrap(Attributes atts)
  {
    boolean	block =
      "block".equals(atts.getValue(Constants.CSS, "display"));

    for (int i = 0; i < atts.getLength(); ++i)
    {
      if
      (
        Constants.CSS.equals(atts.getURI(i))				&&
        (
          (
            block							&&
            triggeringBlockProperties.contains(atts.getLocalName(i))
          )								||
          (
            "position".equals(atts.getLocalName(i))			&&
            (
              "absolute".equals(atts.getValue(i))			||
              "fixed".equals(atts.getValue(i))
            )
          )
        )
      )
      {
        return true;
      }
    }

    return false;
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
    boolean	wrap = shouldWrap(atts);

    if (wrap)
    {
      super.startElement
      (
        Constants.XSLFO,
        "block-container",
        "fo:block-container",
        selectAttributes(atts, true)
      );

      super.startElement
      (
        Constants.XSLFO,
        "block",
        "fo:block",
        new AttributesImpl()
      );
    }

    super.startElement
    (
      namespaceURI,
      localName,
      qName,
      wrap ? selectAttributes(atts, false) : atts
    );

    stack.push(new Boolean(wrap));
  }

} // BlockContainerFilter
