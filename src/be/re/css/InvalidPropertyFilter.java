package be.re.css;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Removes invalid properties after projection, where shorthand properties have
 * already been split.
 * @author Werner Donn\u00e9
 */

class InvalidPropertyFilter extends XMLFilterImpl

{

  private final static Set	after =
    new HashSet(Arrays.asList(new String[]{"change-bar-class", "content"}));
  private final static Set	alwaysValid =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "background-attachment",
          "background-color",
          "background-image",
          "background-repeat",
          "border-top-color",
          "border-right-color",
          "border-bottom-color",
          "border-left-color",
          "border-top-style",
          "border-right-style",
          "border-bottom-style",
          "border-left-style",
          "border-top-width",
          "border-right-width",
          "border-bottom-width",
          "border-left-width",
          "color",
          "counter-increment",
          "counter-reset",
          "direction",
          "display",
          "font",
          "font-family",
          "font-size",
          "font-size-adjust",
          "font-stretch",
          "font-style",
          "font-variant",
          "font-weight",
          "letter-spacing",
          "line-height",
          "margin-top",
          "margin-right",
          "margin-bottom",
          "margin-left",
          "padding-top",
          "padding-right",
          "padding-bottom",
          "padding-left",
          "position",
          "quotes",
          "region",
          "string-set",
          "text-decoration",
          "text-shadow",
          "text-transform",
          "unicode-bidi",
          "visibility",
          "word-spacing",
          // Internal attributes.
          "has-first-letter",
          "has-markers",
          "list-label-width"
        }
      )
    );
  private final static Set	before =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "change-bar-class", "change-bar-color", "change-bar-offset",
            "change-bar-placement", "change-bar-style", "change-bar-width",
            "content"
        }
      )
    );
  private final static Set	blockLevel =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "anchor", "background-position", "clear", "clip", "hyphenate",
            "link", "orphans", "overflow", "page", "page-break-after",
            "page-break-before", "page-break-inside", "text-align",
            "text-align-last", "text-indent", "white-space", "widows"
        }
      )
    );
  private final static Set	blockLevelNotTable =
    new HashSet(Arrays.asList(new String[]{"column-span"}));
  private final static Set	blockOrTableOrTableCell =
    new HashSet(Arrays.asList(new String[]{"orientation"}));
  private final static Set	graphic =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "background-position", "content-height", "content-type",
            "content-width", "height", "max-height", "max-width", "min-height",
            "min-width", "overflow", "scaling", "scaling-method", "src", "width"
        }
      )
    );
  private final static Set	inline =
    new HashSet
    (
      Arrays.
        asList(new String[]{"anchor", "hyphenate", "link", "vertical-align"})
    );
  private final static Set	leader =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "leader-alignment", "leader-length", "leader-pattern",
            "leader-pattern-width", "rule-thickness"
        }
      )
    );
  private final static Set	listItem =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "list-style", "list-style-image", "list-style-position",
            "list-style-type"
        }
      )
    );
  private final static Set	marker =
    new HashSet(Arrays.asList(new String[]{"marker-offset"}));
  private final static Set	notInlineOrTable =
    new HashSet
    (
      Arrays.asList
      (
        new String[]{"max-height", "max-width", "min-height", "min-width"}
      )
    );
  private final static Set	notInlineOrTableColumnOrColumnGroup =
    new HashSet(Arrays.asList(new String[]{"height"}));
  private final static Set	notInlineOrTableRowOrRowGroup =
    new HashSet(Arrays.asList(new String[]{"width"}));
  private final static Set	notPositioned =
    new HashSet(Arrays.asList(new String[]{"float"}));
  private final static Set	positioned =
    new HashSet
    (
      Arrays.asList(new String[]{"bottom", "left", "right", "top", "z-index"})
    );
  private final static Set	table =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "border-collapse", "border-spacing", "caption-side", "empty-cells",
            "table-layout", "table-omit-footer-at-break",
            "table-omit-header-at-break"
        }
      )
    );
  private final static Set	tableCaption =
    new HashSet(Arrays.asList(new String[]{"caption-side"}));
  private final static Set	tableCell =
    new HashSet
    (
      Arrays.asList
      (
        new String[]{"colspan", "empty-cells", "rowspan", "vertical-align"}
      )
    );



  InvalidPropertyFilter()
  {
  }



  InvalidPropertyFilter(XMLReader parent)
  {
    super(parent);
  }



  private static boolean
  isBlockLevel(String display)
  {
    return
      display == Element.BLOCK || display == Element.COMPACT ||
        display == Element.LIST_ITEM || display == Element.RUN_IN ||
        display == Element.TABLE || display == Element.TABLE_CELL ||
        display == Element.TABLE_ROW || display == Element.MARKER;
  }



  private static boolean
  isInline(String display)
  {
    return
      display == Element.INLINE || display == Element.GRAPHIC ||
        display == Element.LEADER;
  }



  private static boolean
  isPositioned(Attributes atts)
  {
    String	value = atts.getValue(Constants.CSS, "position");

    return value != null && !"static".equals(value);
  }



  private static boolean
  isValid
  (
    Attributes	atts,
    int		index,
    String	display,
    boolean	isBefore,
    boolean	isAfter,
    boolean	isPositioned
  )
  {
    String	localName = atts.getLocalName(index);

    return
      !Constants.CSS.equals(atts.getURI(index)) ||
        (
          alwaysValid.contains(localName) ||
            Util.isInherited(localName) ||
            (isBlockLevel(display) && blockLevel.contains(localName)) ||
            (display == Element.LIST_ITEM && listItem.contains(localName)) ||
            (
              (display == Element.TABLE || display == Element.INLINE_TABLE) &&
                table.contains(localName)
            ) ||
            (
              display == Element.TABLE_CAPTION &&
                tableCaption.contains(localName)
            ) ||
            (display == Element.TABLE_CELL && tableCell.contains(localName)) ||
            (isInline(display) && inline.contains(localName)) ||
            (
              !isInline(display) && display != Element.TABLE &&
                notInlineOrTable.contains(localName)
            ) ||
            (
              !isInline(display) && display != Element.TABLE_COLUMN &&
                display != Element.TABLE_COLUMN_GROUP &&
                notInlineOrTableColumnOrColumnGroup.contains(localName)
            ) ||
            (
              !isInline(display) && display != Element.TABLE_ROW &&
                display != Element.TABLE_ROW_GROUP &&
                notInlineOrTableRowOrRowGroup.contains(localName)
            ) ||
            (
              display != Element.TABLE && isBlockLevel(display) &&
                blockLevelNotTable.contains(localName)
            ) ||
            (display == Element.MARKER && marker.contains(localName)) ||
            (display == Element.GRAPHIC && graphic.contains(localName)) ||
            (display == Element.LEADER && leader.contains(localName)) ||
            (
              (
                display == Element.BLOCK || display == Element.TABLE ||
                  display == Element.TABLE_CELL
              ) && blockOrTableOrTableCell.contains(localName)
            ) ||
            (isAfter && after.contains(localName)) ||
            (isBefore && before.contains(localName)) ||
            (isPositioned && positioned.contains(localName)) ||
            (!isPositioned && notPositioned.contains(localName))
        );
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

    if
    (
      display == null				||
      (
        Constants.CSS.equals(namespaceURI)	&&
        !"after".equals(localName)		&&
        !"before".equals(localName)
      )
    )
    {
      super.startElement(namespaceURI, localName, qName, atts);

      return;
    }

    display = display.intern();

    boolean		after =
      Constants.CSS.equals(namespaceURI) && "after".equals(localName);
    boolean		before =
      Constants.CSS.equals(namespaceURI) && "before".equals(localName);
    AttributesImpl	newAtts = new AttributesImpl();
    boolean		positioned = isPositioned(atts);

    for (int i = 0; i < atts.getLength(); ++i)
    {
      if (isValid(atts, i, display, before, after, positioned))
      {
        newAtts.addAttribute
        (
          atts.getURI(i),
          atts.getLocalName(i),
          atts.getQName(i),
          atts.getType(i),
          atts.getValue(i)
        );
      }
    }

    super.startElement(namespaceURI, localName, qName, newAtts);
  }

} // InvalidPropertyFilter
