package be.re.css;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Properties;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Process space characters as in Jirka's Kosek spaces.xsl DocBook style sheet.
 * @author Werner Donn\u00e9
 */

class SpaceCorrectionFilter extends XMLFilterImpl

{

  private static final	String[]	widths = loadWidths();



  SpaceCorrectionFilter()
  {
  }



  SpaceCorrectionFilter(XMLReader parent)
  {
    super(parent);
  }



  public void
  characters(char[] ch, int start, int length) throws SAXException
  {
    int	position = start;

    for (int i = 0; i < length; ++i)
    {
      if
      (
        ch[start + i] >= '\u2000'			&&
        ch[start + i] <= '\u200A'			&&
        widths[ch[start + i] - 0x2000].length() > 0
      )
      {
        super.characters(ch, position, start + i - position);
        position = start + i + 1;

        AttributesImpl	atts = new AttributesImpl();

        atts.addAttribute
        (
          "",
          "leader-length",
          "leader-length",
          "CDATA",
          widths[ch[start + i] - 0x2000]
        );

        super.startElement(Constants.XSLFO, "leader", "fo:leader", atts);
        super.endElement(Constants.XSLFO, "leader", "fo:leader");
      }
    }

    if (position < start + length)
    {
      super.characters(ch, position, start + length - position);
    }
  }



  private static String[]
  loadWidths()
  {
    try
    {
      Properties	properties = new Properties();
      String[]		result = new String[11];

      properties.load
      (
        SpaceCorrectionFilter.class.
          getResourceAsStream("res/space_correction.prop")
      );

      for (int i = 0; i < result.length; ++i)
      {
        String	value =
          properties.
            getProperty(Integer.toString(0x2000 + i, 16).toLowerCase());

        if (value == null)
        {
          value =
            properties.
              getProperty(Integer.toString(0x2000 + i, 16).toUpperCase());
        }

        result[i] = value != null ? value : "";
      }

      return result;
    }

    catch (Exception e)
    {
      throw new UndeclaredThrowableException(e);
    }
  }

} // SpaceCorrectionFilter
