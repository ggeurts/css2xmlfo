package be.re.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * This filter translates XHTML attributes to the corresponding CSS properties.
 * @author Werner Donn\u00e9
 */

class XHTMLAttributeTranslationFilter extends XMLFilterImpl

{

  private static final String	COL = "col".intern();
  private static final String	COLGROUP = "colgroup".intern();
  private static final String	TABLE = "table".intern();
  private static final String	TBODY = "tbody".intern();
  private static final String	TFOOT = "tfoot".intern();
  private static final String	THEAD = "thead".intern();
  private static final String	TD = "td".intern();
  private static final String	TH = "th".intern();
  private static final String	TR = "tr".intern();

  private static final Map	map =
    loadTable
    (
      new String[][]
      { // element, attribute, attribute value, CSS property, CSS property value
        {"applet", "align", "bottom", "vertical-align", "bottom"},
        {"applet", "align", "left", "text-align", "left"},
        {"applet", "align", "middle", "vertical-align", "middle"},
        {"applet", "align", "right", "text-align", "right"},
        {"applet", "align", "top", "vertical-align", "top"},
        {"applet", "height", null, "height", null},
        {"applet", "hspace", null, "margin-left", null},
        {"applet", "hspace", null, "margin-right", null},
        {"applet", "vspace", null, "margin-bottom", null},
        {"applet", "vspace", null, "margin-top", null},
        {"applet", "width", null, "width", null},
        {"body", "background", null, "background-image", null},
        {"body", "text", null, "color", null},
        {"caption", "align", null, "caption-side", null},
        {"col", "align", "center", "text-align", "center"},
        {"col", "align", "char", "text-align", "@char;."},
        {"col", "align", "justify", "text-align", "justify"},
        {"col", "align", "left", "text-align", "left"},
        {"col", "align", "right", "text-align", "right"},
        {"col", "span", null, "span", null},
        {"col", "valign", null, "vertical-align", null},
        {"col", "width", null, "width", null},
        {"colgroup", "align", "center", "text-align", "center"},
        {"colgroup", "align", "char", "text-align", "@char;."},
        {"colgroup", "align", "justify", "text-align", "justify"},
        {"colgroup", "align", "left", "text-align", "left"},
        {"colgroup", "align", "right", "text-align", "right"},
        {"colgroup", "span", null, "span", null},
        {"colgroup", "valign", null, "vertical-align", null},
        {"colgroup", "width", null, "width", null},
        {"div", "align", null, "text-align", null},
        {"font", "color", null, "color", null},
        {"font", "face", null, "font-family", null},
        {"font", "size", null, "font-size", "f:fontSize"},
        {"h1", "align", null, "text-align", null},
        {"h2", "align", null, "text-align", null},
        {"h3", "align", null, "text-align", null},
        {"h4", "align", null, "text-align", null},
        {"h5", "align", null, "text-align", null},
        {"h6", "align", null, "text-align", null},
        {"hr", "align", null, "text-align", null},
        {"hr", "noshade", null, "border-bottom-style", "solid"},
        {"hr", "noshade", null, "border-left-style", "solid"},
        {"hr", "noshade", null, "border-right-style", "solid"},
        {"hr", "noshade", null, "border-top-style", "solid"},
        {"hr", "size", null, "height", null},
        {"hr", "width", null, "width", null},
        {"img", "border", null, "border-bottom-width", null},
        {"img", "border", null, "border-left-width", null},
        {"img", "border", null, "border-right-width", null},
        {"img", "border", null, "border-top-width", null},
        {"img", "border", null, "border-bottom-style", "solid"},
        {"img", "border", null, "border-left-style", "solid"},
        {"img", "border", null, "border-right-style", "solid"},
        {"img", "border", null, "border-top-style", "solid"},
        {"img", "border", null, "border-after-width.conditionality", "retain"},
        {"img", "border", null, "border-before-width.conditionality", "retain"},
        {"img", "height", null, "height", null},
        {"img", "hspace", null, "margin-left", null},
        {"img", "hspace", null, "margin-right", null},
        {"img", "vspace", null, "margin-bottom", null},
        {"img", "vspace", null, "margin-top", null},
        {"img", "width", null, "width", null},
        {"input", "align", null, "text-align", null},
        {"object", "border", null, "border-bottom-width", null},
        {"object", "border", null, "border-left-width", null},
        {"object", "border", null, "border-right-width", null},
        {"object", "border", null, "border-top-width", null},
        {"object", "border", null, "border-bottom-style", "solid"},
        {"object", "border", null, "border-left-style", "solid"},
        {"object", "border", null, "border-right-style", "solid"},
        {"object", "border", null, "border-top-style", "solid"},
        {
          "object", "border", null, "border-after-width.conditionality",
            "retain"
        },
        {
          "object", "border", null, "border-before-width.conditionality",
            "retain"
        },
        {"li", "compact", null, "list-style-position", "inside"},
        {"li", "type", null, "list-style-type", null},
        {"object", "height", null, "height", null},
        {"object", "hspace", null, "margin-left", null},
        {"object", "hspace", null, "margin-right", null},
        {"object", "vspace", null, "margin-bottom", null},
        {"object", "vspace", null, "margin-top", null},
        {"object", "width", null, "width", null},
        {"ol", "compact", null, "list-style-position", "inside"},
        {"ol", "type", "1", "list-style-type", "decimal"},
        {"ol", "type", "a", "list-style-type", "lower-alpha"},
        {"ol", "type", "A", "list-style-type", "upper-alpha"},
        {"ol", "type", "i", "list-style-type", "lower-roman"},
        {"ol", "type", "I", "list-style-type", "upper-roman"},
        {"p", "align", null, "text-align", null},
        {"span", "align", null, "text-align", null},
        {"table", "width", null, "width", null},
        {"tbody", "align", "center", "text-align", "center"},
        {"tbody", "align", "char", "text-align", "@char;."},
        {"tbody", "align", "justify", "text-align", "justify"},
        {"tbody", "align", "left", "text-align", "left"},
        {"tbody", "align", "right", "text-align", "right"},
        {"tbody", "valign", null, "vertical-align", null},
        {"td", "align", "center", "text-align", "center"},
        {"td", "align", "char", "text-align", "@char;."},
        {"td", "align", "justify", "text-align", "justify"},
        {"td", "align", "left", "text-align", "left"},
        {"td", "align", "right", "text-align", "right"},
        {"td", "colspan", null, "colspan", null},
        {"td", "height", null, "height", null},
        {"td", "nowrap", null, "white-space", "nowrap"},
        {"td", "rowspan", null, "rowspan", null},
        {"td", "valign", null, "vertical-align", null},
        {"td", "width", null, "width", null},
        {"tfoot", "align", "center", "text-align", "center"},
        {"tfoot", "align", "char", "text-align", "@char;."},
        {"tfoot", "align", "justify", "text-align", "justify"},
        {"tfoot", "align", "left", "text-align", "left"},
        {"tfoot", "align", "right", "text-align", "right"},
        {"tfoot", "valign", null, "vertical-align", null},
        {"th", "align", "center", "text-align", "center"},
        {"th", "align", "char", "text-align", "@char;."},
        {"th", "align", "justify", "text-align", "justify"},
        {"th", "align", "left", "text-align", "left"},
        {"th", "align", "right", "text-align", "right"},
        {"th", "colspan", null, "colspan", null},
        {"th", "height", null, "height", null},
        {"th", "nowrap", null, "white-space", "nowrap"},
        {"th", "rowspan", null, "rowspan", null},
        {"th", "valign", null, "vertical-align", null},
        {"th", "width", null, "width", null},
        {"thead", "align", "center", "text-align", "center"},
        {"thead", "align", "char", "text-align", "@char;."},
        {"thead", "align", "justify", "text-align", "justify"},
        {"thead", "align", "left", "text-align", "left"},
        {"thead", "align", "right", "text-align", "right"},
        {"thead", "valign", null, "vertical-align", null},
        {"tr", "align", "center", "text-align", "center"},
        {"tr", "align", "char", "text-align", "@char;."},
        {"tr", "align", "justify", "text-align", "justify"},
        {"tr", "align", "left", "text-align", "left"},
        {"tr", "align", "right", "text-align", "right"},
        {"tr", "bgcolor", null, "background-color", null},
        {"tr", "valign", null, "vertical-align", null},
        {"ul", "compact", null, "list-style-position", "inside"},
        {"ul", "type", null, "list-style-type", null}
      }
    );

  private String	defaultBorderThickness;
  private Stack		elementStack = new Stack();
  private Stack		tableStack = new Stack();



  XHTMLAttributeTranslationFilter()
  {
    this("0.2pt");
  }



  XHTMLAttributeTranslationFilter(String defaultBorderThickness)
  {
    this.defaultBorderThickness = defaultBorderThickness;
  }



  XHTMLAttributeTranslationFilter(XMLReader parent)
  {
    this(parent, "0.2pt");
  }



  XHTMLAttributeTranslationFilter
  (
    XMLReader	parent,
    String	defaultBorderThickness
  )
  {
    super(parent);
    this.defaultBorderThickness = defaultBorderThickness;
  }



  private static String
  callPropertyResolver
  (
    String	function,
    String	element,
    Attributes	atts,
    String	attribute,
    String	value
  )
  {
    try
    {
      return
        (String)
          XHTMLAttributeTranslationFilter.class.getDeclaredMethod
          (
            function,
            new Class[]
              {String.class, Attributes.class, String.class, String.class}
          ).invoke(null, new Object[] {element, atts, attribute, value});
    }

    catch (Exception e)
    {
      return value;
    }
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    if (Constants.XHTML == namespaceURI)
    {
      elementStack.pop();

      if (TABLE == localName)
      {
        tableStack.pop();
      }
    }

    super.endElement(namespaceURI, localName, qName);
  }



  private static boolean
  equivalentSibling(String element1, String element2)
  {
    return
      element1 == element2 ||
        (
          (THEAD == element1 || TBODY == element1 || TFOOT == element1) &&
            (THEAD == element2 || TBODY == element2 || TFOOT == element2)
        );
  }



  private static String
  fontSize(String element, Attributes atts, String attribute, String value)
  {
    try
    {
      return
        value.startsWith("+") ?
          (
            String.valueOf
            (
              (int)
                (
                  100.0 *
                    (100 + 10 * Integer.parseInt(value.substring(1))) / 100
                )
            ) + "%"
          ) :
          (
            value.startsWith("-") ?
              (
                String.valueOf
                (
                  (int)
                    (
                      100.0 *
                        (100 - 10 * Integer.parseInt(value.substring(1))) / 100
                    )
                ) + "%"
              ) : (String.valueOf((Integer.parseInt(value) + 7)) + "pt")
          );
    }

    catch (Exception e)
    {
      return "100%";
    }
  }



  private static Map
  loadTable(String[][] table)
  {
    Map	result = new HashMap();

    for (int i = 0; i < table.length; ++i)
    {
      String	key = table[i][0] + "#" + table[i][1];
      List	tuples = (List) result.get(key);

      if (tuples == null)
      {
        tuples = new ArrayList();
        result.put(key, tuples);
      }

      tuples.add(new Tuple(table[i][2], table[i][3], table[i][4]));
    }

    return result;
  }



  private static Tuple[]
  lookup(String element, Attributes atts, String attribute, String value)
  {
    List	tuples = (List) map.get(element + "#" + attribute);

    if (tuples == null)
    {
      return new Tuple[0];
    }

    List	result = new ArrayList();

    for (int i = 0; i < tuples.size(); ++i)
    {
      Tuple	tuple = (Tuple) tuples.get(i);

      if (tuple.inValue == null || tuple.inValue.equals(value))
      {
        String	otherAttribute;

        result.add
        (
          new Tuple
          (
            tuple.inValue,
            tuple.property,
            tuple.outValue == null ?
              value :
              (
                tuple.outValue.charAt(0) == '@' ?
                  (
                    (
                      otherAttribute =
                        atts.getValue
                        (
                          tuple.outValue.
                            substring(1, tuple.outValue.indexOf(';'))
                        )
                    ) != null ?
                      otherAttribute :
                      tuple.outValue.substring(tuple.outValue.indexOf(';') + 1)
                  ) :
                  (
                    tuple.outValue.startsWith("f:") ?
                      callPropertyResolver
                      (
                        tuple.outValue.substring(2),
                        element,
                        atts,
                        attribute,
                        value
                      ) : tuple.outValue
                  )
              )
          )
        );
      }
    }

    return (Tuple[]) result.toArray(new Tuple[result.size()]);
  }



  private static void
  mergeAttribute
  (
    AttributesImpl	atts,
    String		originalName,
    String		cssName,
    String		value
  )
  {
    int	index1 = atts.getIndex(Constants.CSS, cssName);

    if (index1 == -1)
    {
      atts.
        addAttribute(Constants.CSS, cssName, "css:" + cssName, "CDATA", value);
    }
    else
    {
      int	index2 = atts.getIndex(Constants.SPECIF, cssName);

      if (index2 != -1)
      {
        atts.setValue(index1, value);
      }
    }

    if (originalName != null)
    {
      index1 = atts.getIndex(originalName);

      if (index1 != -1)
      {
        atts.removeAttribute(index1);
      }
    }
  }



  private AttributesImpl
  prepareTableAttributes(String localName, Attributes atts)
  {
    return
      TD == localName || TH == localName ?
         preprocessTableCell
         (
           localName,
           atts,
           new String[] {"all", "cols"},
           "left"
         ) :
         (
           TR == localName ?
             preprocessRulesBorder
             (
               localName,
               atts,
               new String[] {"all", "rows"},
               "top"
             ) :
             (
               COL == localName ?
                 preprocessRulesBorder
                 (
                   localName,
                   atts,
                   new String[] {"all", "cols"},
                   "left"
                 ) :
                 (
                   TABLE == localName ?
                     preprocessTableBorder(atts) :
                     (
                       THEAD == localName || TFOOT == localName ||
                         TBODY == localName ?
                         preprocessRulesBorder
                         (
                           localName,
                           atts,
                           new String[] {"all", "rows", "groups"},
                           "top"
                         ) : 
                         (
                           COLGROUP == localName ?
                             preprocessRulesBorder
                             (
                               localName,
                               atts,
                               new String[] {"groups"},
                               "left"
                             ) : new AttributesImpl(atts)
                         )
                     )
                )
            )
        );
  }



  private AttributesImpl
  preprocessRulesBorder
  (
    String	localName,
    Attributes	atts,
    String[]	rulesValues,
    String	borderSide
  )
  {
    Element		table = (Element) tableStack.peek();
    String		border = table.atts.getValue("border");
    String		rules = table.atts.getValue("rules");
    AttributesImpl	result = new AttributesImpl(atts);

    if ("0".equals(border) || "none".equals(rules))
    {
      return result;
    }

    String	borderWidth =
      border == null ? defaultBorderThickness : (border + "px");
    Element	parent = (Element) elementStack.peek();

    if
    (
      (
        (
          border != null						&&
          rules == null
        )								||
        Util.inArray(rulesValues, rules)
      )									&&
      equivalentSibling(localName, ((Preceding) parent.extra).element)	&&
      ((Preceding) parent.extra).count > 0
    )
    {
      mergeAttribute
      (
        result,
        "border",
        "border-" + borderSide + "-width",
        borderWidth
      );

      mergeAttribute
      (
        result,
        null, // Removed already.
        "border-" + borderSide + "-style",
        "solid"
      );

      if ("bottom".equals(borderSide))
      {
        mergeAttribute
        (
          result,
          null, // Removed already.
          "border-after-width.conditionality",
          "retain"
        );
      }

      if ("top".equals(borderSide))
      {
        mergeAttribute
        (
          result,
          null, // Removed already.
          "border-before-width.conditionality",
          "retain"
        );
      }
    }

    return result;
  }



  private AttributesImpl
  preprocessTableBorder(Attributes atts)
  {
    Element		table = (Element) tableStack.peek();
    String		border = table.atts.getValue("border");
    String		frame = table.atts.getValue("frame");
    AttributesImpl	result = new AttributesImpl(atts);

    if ("0".equals(border) || "void".equals(frame))
    {
      return result;
    }

    String	borderWidth =
      border == null ? defaultBorderThickness : (border + "px");

    if
    (
      (
        border != null							&&
        frame == null
      )									||
      Util.inArray(new String[] {"above", "hsides", "box", "border"}, frame)
    )
    {
      mergeAttribute(result, "border", "border-top-width", borderWidth);
      mergeAttribute(result, null, "border-top-style", "solid");

      mergeAttribute
      (
        result,
        null, // Removed already
        "border-before-width.conditionality",
        "retain"
      );
    }

    if
    (
      (
        border != null							&&
        frame == null
      )									||
      Util.inArray(new String[] {"below", "hsides", "box", "border"}, frame)
    )
    {
      mergeAttribute(result, "border", "border-bottom-width", borderWidth);
      mergeAttribute(result, null, "border-bottom-style", "solid");

      mergeAttribute
      (
        result,
        null, // Removed already
        "border-after-width.conditionality",
        "retain"
      );
    }

    if
    (
      (
        border != null							&&
        frame == null
      )									||
      Util.inArray(new String[] {"lhs", "vsides", "box", "border"}, frame)
    )
    {
      mergeAttribute(result, "border", "border-left-width", borderWidth);
      mergeAttribute(result, null, "border-left-style", "solid");
    }

    if
    (
      (
        border != null							&&
        frame == null
      )									||
      Util.inArray(new String[] {"rhs", "vsides", "box", "border"}, frame)
    )
    {
      mergeAttribute(result, "border", "border-right-width", borderWidth);
      mergeAttribute(result, null, "border-right-style", "solid");
    }

    return result;
  }



  private AttributesImpl
  preprocessTableCell
  (
    String	localName,
    Attributes	atts,
    String[]	rulesValues,
    String	borderSide
  )
  {
    // If there are columns the normal column propagation can take place. Else
    // we should place the borders directly on the cells.

    return
      preprocessRulesBorder
      (
        localName,
        preprocessTableCellPaddingAndSpacing(atts),
        rulesValues,
        borderSide
      );
  }



  private AttributesImpl
  preprocessTableCellPaddingAndSpacing(Attributes atts)
  {
    Element		table = (Element) tableStack.peek();
    String		padding = table.atts.getValue("cellpadding");
    AttributesImpl	result = new AttributesImpl(atts);
    String		spacing = table.atts.getValue("cellspacing");

    if (padding != null)
    {
      mergeAttribute(result, null, "padding-top", padding);
      mergeAttribute(result, null, "padding-bottom", padding);
      mergeAttribute(result, null, "padding-left", padding);
      mergeAttribute(result, null, "padding-right", padding);
    }

    if (spacing != null)
    {
      mergeAttribute(result, null, "margin-top", spacing);
      mergeAttribute(result, null, "margin-bottom", spacing);
      mergeAttribute(result, null, "margin-left", spacing);
      mergeAttribute(result, null, "margin-right", spacing);
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
    if (Constants.XHTML == namespaceURI)
    {
      Element	element = new Element(namespaceURI, localName, qName, atts);

      if (TABLE == localName)
      {
        element.extra = new Boolean(false); // No columns seen yet.
        tableStack.push(element);
      }

      AttributesImpl	newAtts = prepareTableAttributes(localName, atts);

      for (int i = 0; i < atts.getLength(); ++i)
      {
        if (atts.getURI(i).equals(""))
        {
          Tuple[]	tuples =
            lookup(localName, atts, atts.getLocalName(i), atts.getValue(i));

          for (int j = 0; j < tuples.length; ++j)
          {
            mergeAttribute
            (
              newAtts,
              atts.getLocalName(i),
              tuples[j].property,
              tuples[j].outValue
            );
          }
        }
      }

      Element	parent =
        elementStack.empty() ? null : (Element) elementStack.peek();

      if (parent != null)
      {
        ((Preceding) parent.extra).count =
          equivalentSibling(localName, ((Preceding) parent.extra).element) ?
            (((Preceding) parent.extra).count + 1) :
            1;
        ((Preceding) parent.extra).element = localName;
      }

      element.extra = new Preceding();
      elementStack.push(element);

      super.startElement(namespaceURI, localName, qName, newAtts);
    }
    else
    {
      super.startElement(namespaceURI, localName, qName, atts);
    }
  }



  private static class Preceding

  {

    private int		count = 0;
    private String	element;

  } // Preceding



  private static class Tuple

  {

    private String	inValue;
    private String	outValue;
    private String	property;



    private
    Tuple(String inValue, String property, String outValue)
    {
      this.inValue = inValue;
      this.property = property;
      this.outValue = outValue;
    }

  } // Tuple

} // XHTMLAttributeTranslationFilter
