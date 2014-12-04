package be.re.css;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Length properties without a unit are given the unit "px".
 * @author Werner Donn\u00e9
 */

class LengthAdjustFilter extends XMLFilterImpl

{

  private static final Set	ofLengthType =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "background-position",
          "border-bottom-width",
          "border-left-width",
          "border-right-width",
          "border-spacing",
          "border-top-width",
          "bottom",
          "font-size",
          "height",
          "img-height",
          "img-width",
          "left",
          "letter-spacing",
          "line-height",
          "margin-bottom",
          "margin-left",
          "margin-right",
          "margin-top",
          "marker-offset",
          "max-height",
          "max-width",
          "min-height",
          "min-width",
          "outline-width",
          "padding-bottom",
          "padding-left",
          "padding-right",
          "padding-top",
          "right",
          "size",
          "text-indent",
          "text-shadow",
          "top",
          "vertical-align",
          "width",
          "word-spacing"
        }
      )
    );



  LengthAdjustFilter()
  {
  }



  LengthAdjustFilter(XMLReader parent)
  {
    super(parent);
  }



  private static Attributes
  adjustAttributes(Attributes atts)
  {
    AttributesImpl	result = null;

    for (int i = 0; i < atts.getLength(); ++i)
    {
      if
      (
        Constants.CSS.equals(atts.getURI(i))			&&
        ofLengthType.contains(atts.getLocalName(i))
      )
      {
        boolean		changed = false;
        String		newValue = "";
        StringTokenizer	tokenizer = new StringTokenizer(atts.getValue(i), " ");

        while (tokenizer.hasMoreTokens())
        {
          String	token = tokenizer.nextToken();

          if (mustReplace(token))
          {
            changed = true;
            newValue += (newValue.equals("") ? "" : " " ) + token + "px";
          }
          else
          {
            newValue += (newValue.equals("") ? "" : " " ) + token;
          }
        }

        if (changed)
        {
          if (result == null)
          {
            result = new AttributesImpl(atts);
          }

          result.setValue(i, newValue);
        }
      }
    }

    return result != null ? (Attributes) result : atts;
  }



  private static boolean
  mustReplace(String s)
  {
    try
    {
      return Integer.parseInt(s) > 0;
    }

    catch (NumberFormatException e)
    {
      return false;
    }
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
    super.startElement(namespaceURI, localName, qName, adjustAttributes(atts));
  }

} // LengthAdjustFilter
