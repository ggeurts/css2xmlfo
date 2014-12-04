package be.re.css;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Compensates the differences between CSS and XSL-FO with respect to
 * horizontal margins and width. There should be no shorthand properties
 * anymore.
 * @author Werner Donne\u00e9
 */

class WidthAndMarginsFilter extends XMLFilterImpl

{

  private Stack	stack = new Stack();



  WidthAndMarginsFilter()
  {
  }



  WidthAndMarginsFilter(XMLReader parent)
  {
    super(parent);
  }



  private static String
  adjustMargin(String margin, Attributes atts, String edge)
  {
    String	s;

    return
      "auto".equals(margin) ?
        margin :
        (
          margin +
            (Util.isZeroLength(s = getBorderWidth(atts, edge)) ? "" : ("+" + s))
        );
  }



  private static String
  canonicLength(String value)
  {
    return Util.isZeroLength(value) ? "0pt" : value;
  }



  private static Attributes
  correctBlock(Attributes atts, Attributes parent)
  {
    AttributesImpl	result = new AttributesImpl(atts);

    String	marginLeft = getImplicitZeroProperty(atts, "margin-left");
    String	marginRight = getImplicitZeroProperty(atts, "margin-right");
    String	width = atts.getValue(Constants.CSS, "width");

    if ("auto".equals(width))
    {
      marginLeft = "0pt";
      marginRight = "0pt";
    }
    else
    {
      if
      (
        width != null			&&
        !width.equals("auto")		&&
        !marginLeft.equals("auto")	&&
        !marginRight.equals("auto")
      ) // Over-constraint.
      {
        String	direction = atts.getValue(Constants.CSS, "direction");

        if (direction == null || direction.equals("ltr"))
        {
          marginRight = "auto";
        }
        else
        {
          marginLeft = "auto";
        }
      }
    }

    if (marginLeft.equals("auto") && marginRight.equals("auto"))
    {
      marginRight = "0pt";
    }

    setValue(result, "margin-left", adjustMargin(marginLeft, atts, "left"));
    setValue(result, "margin-right", adjustMargin(marginRight, atts, "right"));

    if (width != null)
    {
      setValue(result, "width", width);
    }

    return result;
  }



  private static Attributes
  correctInline(Attributes atts)
  {
    AttributesImpl	result = new AttributesImpl(atts);
    int			index = result.getIndex(Constants.CSS, "width");

    if (index != -1)
    {
      result.removeAttribute(index);
    }

    makeAutoExplicit(result, "margin-left");
    makeAutoExplicit(result, "margin-right");

    return result;
  }



  private static Attributes
  correctFloat(Attributes atts)
  {
    AttributesImpl	result = new AttributesImpl(atts);

    // The "width" property is not touched here because that could disable
    // replaced elements (width="auto" should be set to "0"). We can't
    // distinguish replaced and non-replaced elements here.

    makeAutoExplicit(result, "margin-left");
    makeAutoExplicit(result, "margin-right");

    return result;
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    super.endElement(namespaceURI, localName, qName);
    stack.pop();
  }



  private static String
  getBorderWidth(Attributes atts, String edge)
  {
    String	value =
      atts.getValue(Constants.CSS, "border-" + edge + "-width");

    return
      atts.getValue(Constants.CSS, "border-" + edge + "-style") == null ||
        "none".equals
        (
          atts.getValue(Constants.CSS, "border-" + edge + "-style")
        ) ?
        "0pt" :
        (
          value == null ?
            "0.6pt" : // medium
            (
              "thin".equals(value) ?
                "0.2pt" :
                (
                  "medium".equals(value) ?
                    "0.6pt" :
                    ("thick".equals(value) ? "1pt" : canonicLength(value))
                )
            )
        );
  }



  private static String
  getImplicitZeroProperty(Attributes atts, String property)
  {
    String	value = atts.getValue(Constants.CSS, property);

    return value == null ? "0pt" : canonicLength(value);
  }



  private static String
  getPadding(Attributes atts, String edge)
  {
    String	value = atts.getValue(Constants.CSS, "padding-" + edge);

    return value == null ? "0pt" : canonicLength(value);
  }



  private static void
  makeAutoExplicit(AttributesImpl atts, String property)
  {
    int	index = atts.getIndex(Constants.CSS, property);

    if (index == -1)
    {
      atts.addAttribute
      (
        Constants.CSS,
        property,
        "css:" + property,
        "CDATA",
        "0pt"
      );
    }
    else
    {
      if ("auto".equals(atts.getValue(index)))
      {
        atts.setValue(index, "0pt");
      }
    }
  }



  private static void
  setValue(AttributesImpl atts, String name, String value)
  {
    int	index = atts.getIndex(Constants.CSS, name);

    if (index != -1)
    {
      atts.setValue(index, value);
    }
    else
    {
      atts.addAttribute(Constants.CSS, name, "css:" + name, "CDATA", value);
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
    String	display = atts.getValue(Constants.CSS, "display");

    if (display != null)
    {
      if (display.equals("inline"))
      {
        atts = correctInline(atts);
      }
      else
      {
        if
        (
          atts.getValue(Constants.CSS, "float") != null		&&
          !"none".equals(atts.getValue(Constants.CSS, "float"))
        )
        {
          atts = correctFloat(atts);
        }
        else
        {
          // Absolute and fixed positioning is left to the following processor
          // because layout calculation results are needed.

          if
          (
            Util.inArray
            (
              new String[]
                {"block", "compact", "list-item", "run-in", "table"},
              display
            )								 &&
            !"absolute".equals(atts.getValue(Constants.CSS, "position")) &&
            !"fixed".equals(atts.getValue(Constants.CSS, "position"))	 &&
            (
              // If the parent is a table-cell we leave it (too complicated).
              stack.isEmpty()						 ||
              !"table-cell".equals
              (
                ((Attributes) stack.peek()).
                  getValue(Constants.CSS, "display")
              )
            )
          )
          {
            atts =
              correctBlock
              (
                atts,
                !stack.isEmpty() ?
                  (Attributes) stack.peek() : new AttributesImpl()
              );
          }
        }
      }
    }

    stack.push(new AttributesImpl(atts));
    super.startElement(namespaceURI, localName, qName, atts);
  }

} // WidthAndMarginsFilter
