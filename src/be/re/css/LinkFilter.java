package be.re.css;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Processes css:link and css:anchor properties.
 * @author Werner Donn\u00e9
 */

class LinkFilter extends XMLFilterImpl

{

  private static final int	EXTERNAL_LINK = 0;
  private static final int	INTERNAL_LINK = 1;
  private static final int	NO_LINK = 2;

  private URL	baseUrl;
  private Stack	elements = new Stack();



  LinkFilter(URL baseUrl)
  {
    this.baseUrl = baseUrl;
  }



  LinkFilter(URL baseUrl, XMLReader parent)
  {
    super(parent);
    this.baseUrl = baseUrl;
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    super.endElement(namespaceURI, localName, qName);

    int	linkType = ((Element) elements.pop()).linkType;

    if (linkType != NO_LINK)
    {
      String	name =
        (linkType == INTERNAL_LINK ? "internal-link" : "external-link");

      super.endElement(Constants.CSS, name, "css:" + name);
    }
  }



  private void
  handleBaseUrl(Attributes atts) throws SAXException
  {
    String	base = atts.getValue("xml:base");

    try
    {
      ((Element) elements.peek()).baseUrl =
        base != null ?
          new URL(base) :
          (
            elements.size() == 1 ?
              baseUrl : ((Element) elements.get(elements.size() - 2)).baseUrl
          );
    }

    catch (MalformedURLException e)
    {
      throw new SAXException(e);
    }
  }



  private static boolean
  isUrl(URL baseUrl, String target)
  {
    try
    {
      new URL(baseUrl != null ? baseUrl : new URL("file:///nowhere"), target);

      return true;
    }

    catch (MalformedURLException e)
    {
      return false;
    }
  }



  private static Attributes
  resolveAnchor(Attributes atts)
  {
    if (atts.getIndex(Constants.CSS, "anchor") == -1)
    {
      return atts;
    }

    AttributesImpl	result = new AttributesImpl(atts);
    int			index = result.getIndex(Constants.CSS, "anchor");

    if (result.getValue(index).equalsIgnoreCase("none"))
    {
      result.removeAttribute(index);
    }
    else
    {
      result.setValue(index, Util.getIndirectValue(result, "anchor"));
    }

    return result;
  }



  void
  setBaseUrl(URL url)
  {
    baseUrl = url;
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

    elements.push(element);
    handleBaseUrl(atts);

    String	link = atts.getValue(Constants.CSS, "link");

    if (link != null)
    {
      String	target = Util.getIndirectValue(atts, "link");
      String	type = Util.getIndirectType(atts, "link");

      atts = new AttributesImpl(atts);
      ((AttributesImpl) atts).
        removeAttribute(atts.getIndex(Constants.CSS, "link"));

      if (!link.equalsIgnoreCase("none") && target != null)
      {
        AttributesImpl	linkAtts = new AttributesImpl();

        element.linkType =
          target.startsWith("#") || "IDREF".equals(type) ?
            INTERNAL_LINK :
            (isUrl(element.baseUrl, target) ? EXTERNAL_LINK : NO_LINK);

        if (element.linkType != NO_LINK)
        {
          String	name =
            element.linkType == INTERNAL_LINK ?
              "internal-link" : "external-link";

          linkAtts.addAttribute
          (
            "",
            "target",
            "target",
            "CDATA",
            element.linkType == INTERNAL_LINK ?
              target.substring(target.startsWith("#") ? 1 : 0) : target
          );

          super.startElement(Constants.CSS, name, "css:" + name, linkAtts);
        }
      }
      else
      {
        element.linkType = NO_LINK;
      }
    }
    else
    {
      element.linkType = NO_LINK;
    }

    super.startElement(namespaceURI, localName, qName, resolveAnchor(atts));
  }



  private static class Element

  {

    private URL	baseUrl;
    private int	linkType;

  } // Element

} // LinkFilter
