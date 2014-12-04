package be.re.css;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Fetches images specified in <code>list-style-image</code> properties and
 * sets their width as the <code>list-label-width</code> property.
 * @author Werner Donn\u00e9
 */

class ListImageLabelFilter extends XMLFilterImpl

{

  ListImageLabelFilter()
  {
  }



  ListImageLabelFilter(XMLReader parent)
  {
    super(parent);
  }



  private static String
  decodeUrl(String url)
  {
    return
      url.startsWith("url(") && url.endsWith(")") ?
        url.substring(4, url.length() - 1) : url;
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
    String	url = atts.getValue(Constants.CSS, "list-style-image");

    if (url != null)
    {
      if (url.equals("none"))
      {
        atts = new AttributesImpl(atts);

        Util.removeAttribute
        (
          (AttributesImpl) atts,
          Constants.CSS,
          "list-style-image"
        );

        if (atts.getValue(Constants.CSS, "list-style-type") == null)
        {
          ((AttributesImpl) atts).addAttribute
          (
            Constants.CSS,
            "list-style-type",
            "css:list-style-type",
            "CDATA",
            "none"
          );
        }
      }
      else
      {
        try
        {
          BufferedImage	image = ImageIO.read(new URL(decodeUrl(url)));

          if (image != null)
          {
            atts = new AttributesImpl(atts);

            ((AttributesImpl) atts).addAttribute
            (
              Constants.CSS,
              "list-label-width",
              "css:list-label-width",
              "CDATA",
              String.valueOf(image.getWidth() + 5) + "px"
            );
          }
        }

        catch (IOException e)
        {
          throw new SAXException(e);
        }
      }
    }

    super.startElement(namespaceURI, localName, qName, atts);
  }

} // ListImageLabelFilter
