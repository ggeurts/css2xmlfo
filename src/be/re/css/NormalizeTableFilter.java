package be.re.css;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * This filter propagates table-column-group properties to table-column
 * elements and table-column properties to table-cell elements. It also fills up
 * rows which are too short with empty table cells.
 * @author Werner Donn\u00e9
 */

class NormalizeTableFilter extends XMLFilterImpl

{

  private final Stack<List<Attributes>> columnStack = new Stack<>();
  private final Stack<Element> elementStack = new Stack<>();



  NormalizeTableFilter()
  {
  }



  NormalizeTableFilter(XMLReader parent)
  {
    super(parent);
  }



  private void
  addColumn(Attributes atts)
  {
    List<Attributes> columns = columnStack.peek();
    int	span = getSpan(atts, "span");

    for (int i = 0; i < span; ++i)
    {
        columns.add(atts);
    }
  }


  private void
  addTableCellRowContributions(Element element)
  {
    int	rowSpan = getSpan(element.atts, "rowspan");

    if (rowSpan > 1)
    {
      int	colSpan = getSpan(element.atts, "colspan");
      Element	group = getGroup();
      
      @SuppressWarnings("unchecked")
      List<Integer> rowContributions = (List<Integer>)group.extra;

      for (int i = 1; i < rowSpan; ++i) // For the next rows.
      {
        if (i == rowContributions.size())
        {
            rowContributions.add(colSpan);
        }
        else
        {
            rowContributions.set(i, rowContributions.get(i) + colSpan);
        }
      }
    }
  }



  private void
  bookKeeping(Element element, Element parent)
  {
    if (element.isDisplay(Element.TABLE))
    {
      columnStack.push(new ArrayList<Attributes>());
    }
    else
    {
      if (element.isDisplay(Element.TABLE_COLUMN))
      {
        addColumn(new AttributesImpl(element.atts));
      }
      else
      {
        if (element.isDisplay(Element.TABLE_ROW))
        {
          if (isGroup(parent) && parent.extra == null)
          {
            List<Integer> rowContributions = new ArrayList<>();
            rowContributions.add(0); 
            parent.extra = rowContributions; // Row seen.
              // No contributions because there is no previous row.
          }
        }
        else
        {
          if (element.isDisplay(Element.TABLE_CELL))
          {
            int	currentCellCount = parent.extra == null 
                    ? 0 
                    : ((Integer) parent.extra);

            parent.extra = currentCellCount + getSpan(element.atts, "colspan");

            addTableCellRowContributions(element);
          }
        }
      }
    }
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    Element	element = elementStack.pop();

    if
    (
      element.isDisplay(Element.TABLE_ROW_GROUP)	&&
      Constants.CSS.equals(element.namespaceURI)	&&
      "synthetic".equals(element.localName)
    )
    {
      // Flush pending synthetic group.
      super.endElement(Constants.CSS, "synthetic", "css:synthetic");
      endElement(namespaceURI, localName, qName);

      return;
    }

    Element	parent = elementStack.empty() ? null : elementStack.peek();

    if (element.isDisplay(Element.TABLE_COLUMN_GROUP))
    {
      synthesizeColumns(element);
    }
    else
    {
      if (element.isDisplay(Element.TABLE_ROW))
      {
        synthesizeCells(element, parent);
      }
      else
      {
        if (element.isDisplay(Element.TABLE))
        {
          columnStack.pop();
        }
      }
    }

    if
    (
      !element.isDisplay(Element.TABLE_COLUMN_GROUP)		&&
      (
        !element.isDisplay(Element.TABLE_COLUMN)		||
        parent == null						||
        !parent.isDisplay(Element.TABLE_COLUMN_GROUP)
      )
    )
    {
      super.endElement(namespaceURI, localName, qName);
    }
  }



  /**
   * Searches down the element stack until an element with a display type in
   * <code>oneOf</code> is found, but not beyond <code>upTo</code>.
   */

  private Element
  getAncestor(String[] oneOf, String[] upTo)
  {
    for (int i = elementStack.size() - 1; i >= 0; --i)
    {
      Element	element = elementStack.get(i);

      for (int j = 0; j < oneOf.length; ++j)
      {
        if (element.isDisplay(oneOf[j]))
        {
          return element;
        }
      }

      for (int j = 0; j < upTo.length; ++j)
      {
        if (element.isDisplay(upTo[j]))
        {
          return null;
        }
      }
    }

    return null;
  }



  private Attributes
  getColumn()
  {
    List<Attributes> columns = columnStack.peek();
    Integer position = (Integer)elementStack.peek().extra;

    return position == null && columns.size() > 0 
            ? columns.get(0) 
            : columns.isEmpty() || position >= columns.size() 
                    ? null 
                    : columns.get(position);
  }



  private Element
  getGroup()
  {
    return
      getAncestor
      (
        new String[]
          {
            Element.TABLE_ROW_GROUP, Element.TABLE_HEADER_GROUP,
              Element.TABLE_FOOTER_GROUP
          },
        new String[] {Element.TABLE}
      );
  }



  private static int
  getSpan(Attributes atts, String name)
  {
    String	span = atts.getValue(Constants.CSS, name);

    return span == null ? 1 : Integer.parseInt(span);
  }



  private Attributes
  getTableCellAttributes(Element element, Element parent)
  {
    Element	table =
      getAncestor(new String[] {Element.TABLE}, new String[0]);

    if (table == null)
    {
      return element.atts;
    }

    Attributes	column = getColumn();

    // While not being general, the XHTML alignment is done here instead of in
    // XHTMLAttributeTranslationFilter because we have to know to which column
    // a cell belongs, in order to possibly inherit from it. This is only
    // possible when the table is normalized.

    Attributes	atts = Constants.XHTML == parent.namespaceURI 
            ? inheritXHTMLAlign(element.atts, column, parent) 
            : element.atts;
    int	index = table.atts.getIndex(Constants.CSS, "border-collapse");

    // The following are redundant and harmless for collapse, but it produces
    // something useful for XSL-FO processors that treat collapse as separate.

    if (index == -1 || "collapse".equals(table.atts.getValue(index)))
    {
      if (parent.isDisplay(Element.TABLE_ROW))
      {
        atts =
          Util.
            mergeAttributes(parent.atts, atts, new String[] {"border*"}, true);
      }

      if (column != null)
      {
        atts =
          Util.mergeAttributes(column, atts, new String[] {"border*"}, true);
      }
    }

    return atts;
  }



  private Attributes
  getTableRowAttributes(Attributes atts, Attributes parentAtts)
  {
    // The following are redundant and harmless for collapse, but it produces
    // something useful for XSL-FO processors that treat collapse as separate.

    return
      Util.mergeAttributes
      (
        parentAtts,
        atts,
        new String[] {"border-before*", "border-top*"},
        true
      );
  }



  private Attributes
  inheritXHTMLAlign(Attributes atts, Attributes columnAtts, Element parent)
  {
    AttributesImpl	result = new AttributesImpl(atts);
    String		textAlign =
      atts.getIndex(Constants.SPECIF, "text-align") != -1 ?
        // Can be overridden by XHTML.
        null : atts.getValue(Constants.CSS, "text-align");
    String		verticalAlign =
      atts.getIndex(Constants.SPECIF, "vertical-align") != -1 ?
        // Can be overridden by XHTML.
        null : atts.getValue(Constants.CSS, "vertical-align");

    if (textAlign == null && columnAtts != null)
    {
      textAlign = columnAtts.getValue(Constants.CSS, "text-align");
    }

    if
    (
      (
        textAlign == null			||
        verticalAlign == null
      )						&&
      parent.isDisplay(Element.TABLE_ROW)
    )
    {
      if (textAlign == null)
      {
        textAlign = parent.atts.getValue(Constants.CSS, "text-align");
      }

      if (verticalAlign == null)
      {
        verticalAlign = parent.atts.getValue(Constants.CSS, "vertical-align");
      }

      if (textAlign == null || verticalAlign == null)
      {
        Element	group = getGroup();

        if (group != null)
        {
          if (textAlign == null)
          {
            textAlign = group.atts.getValue(Constants.CSS, "text-align");
          }

          if (verticalAlign == null)
          {
            verticalAlign =
              group.atts.getValue(Constants.CSS, "vertical-align");

            if (verticalAlign == null && columnAtts != null)
            {
              verticalAlign =
                columnAtts.getValue(Constants.CSS, "vertical-align");
            }
          }

          if (textAlign == null || verticalAlign == null)
          {
            Element	table =
              getAncestor(new String[] {Element.TABLE}, new String[0]);

            if (table != null)
            {
              if (textAlign == null)
              {
                textAlign = table.atts.getValue(Constants.CSS, "text-align");
              }

              if (verticalAlign == null)
              {
                verticalAlign =
                  table.atts.getValue(Constants.CSS, "vertical-align");
              }
            }
          }
        }
      }
    }

    if (textAlign != null)
    {
      Util.setAttribute
      (
        result,
        Constants.CSS,
        "text-align",
        "css:text-align",
        textAlign
      );
    }

    if (verticalAlign != null)
    {
      Util.setAttribute
      (
        result,
        Constants.CSS,
        "vertical-align",
        "css:vertical-align",
        verticalAlign
      );
    }

    return result;
  }



  private static boolean
  isGroup(Element element)
  {
    return
      element.isDisplay(Element.TABLE_HEADER_GROUP) ||
        element.isDisplay(Element.TABLE_FOOTER_GROUP) ||
        element.isDisplay(Element.TABLE_ROW_GROUP);
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
    Element element = new Element(namespaceURI, localName, qName, atts);

    if (!element.isDisplay(Element.TABLE_COLUMN_GROUP))
    {
      Element parent = elementStack.empty() ? null : elementStack.peek();

      if
      (
        element.isDisplay(Element.TABLE_COLUMN)		&&
        parent.isDisplay(Element.TABLE_COLUMN_GROUP)
      )
      {
        parent.addChild(element);
      }
      else
      {
        if
        (
          element.isDisplay(Element.TABLE_ROW)	&&
          parent.isDisplay(Element.TABLE)
        )
        {
          synthesizeGroup();
          parent = elementStack.peek();
        }
        else
        {
          if (isGroup(element) && parent.isDisplay(Element.TABLE_ROW_GROUP))
          {
            // Flush pending synthetic group.
            elementStack.pop();
            super.endElement(Constants.CSS, "synthetic", "css:synthetic");
          }
        }

        element.atts =
          element.isDisplay(Element.TABLE_CELL) ?
            getTableCellAttributes(element, parent) :
            (
              element.isDisplay(Element.TABLE_ROW) && isGroup(parent) &&
                parent.extra == null /* No row seen yet */ ?
                getTableRowAttributes(atts, parent.atts) : atts
            );

        super.startElement(namespaceURI, localName, qName, element.atts);
        bookKeeping(element, parent);
      }
    }

    elementStack.push(element);
  }



  private void
  synthesizeCells(Element element, Element parent) throws SAXException
  {
    if (element.extra != null)
    {
      List<Attributes> columns = columnStack.peek();
      @SuppressWarnings("unchecked")
      List<Integer> rowContributions = (List<Integer>)parent.extra;
      // From group parent.
      int contribution = rowContributions.size() > 0 
              ? rowContributions.get(0) 
              : 0;

      if (rowContributions.size() > 0)
      {
        rowContributions.remove(0);
      }

      if (rowContributions.isEmpty()) // No contributions left for next row.
      {
        rowContributions.add(0);
      }

      if ((Integer)element.extra + contribution < columns.size())
      {
        for(int i = (Integer)element.extra; i < columns.size(); ++i)
        {
          super.startElement
          (
            Constants.CSS,
            "synthetic",
            "css:synthetic",
            synthesizeColumnAttributes
            (
              (Attributes) columns.get(i),
              "table-cell",
              new String[] {"border*"}
            )
          );

          super.endElement(Constants.CSS, "synthetic", "css:synthetic");
        }
      }
    }
  }



  private static Attributes
  synthesizeColumnAttributes
  (
    Attributes	source,
    String	display,
    String[]	include
  )
  {
    AttributesImpl	atts = new AttributesImpl();

    for (int i = 0; i < source.getLength(); ++i)
    {
      if (Util.inArray(include, source.getLocalName(i)))
      {
        atts.addAttribute
        (
          source.getURI(i),
          source.getLocalName(i),
          source.getQName(i),
          source.getType(i),
          source.getValue(i)
        );
      }
    }

    atts.
      addAttribute(Constants.CSS, "display", "css:display", "CDATA", display);

    return atts;
  }



  private void
  synthesizeColumns(Element element) throws SAXException
  {
    if (element.children == null)
    {
      int	span = getSpan(element.atts, "span");

      for (int i = 0; i < span; ++i)
      {
        Attributes	atts =
          synthesizeColumnAttributes
          (
            element.atts,
            "table-column",
            i == 0 ?
              // Take over the group border if needed.
              new String[] {"border-left*", "text-align", "vertical-align"} :
              (
                i == span - 1 ?
                  new String[]
                    {"border-right*", "text-align", "vertical-align"} :
                  new String[] {"text-align", "vertical-align"}
              )
          );

        addColumn(atts);
        super.startElement(Constants.CSS, "synthetic", "css:synthetic", atts);
        super.endElement(Constants.CSS, "synthetic", "css:synthetic");
      }
    }
    else
    {
      for (int i = 0; i < element.children.size(); ++i)
      {
        Element		child = element.children.get(i);
        Attributes	atts =
          Util.mergeAttributes
          (
            element.atts,
            child.atts,
            i == 0 ?
              // Take over the group border if needed.
              new String[] {"border-left*", "text-align", "vertical-align"} :
              (
                i == element.children.size() - 1 ?
                  new String[]
                    {"border-right*", "text-align", "vertical-align"} :
                  new String[] {"text-align", "vertical-align"}
              ),
            true
          );

        addColumn(atts);

        super.startElement
        (
          child.namespaceURI,
          child.localName,
          child.qName,
          atts
        );

        super.endElement(child.namespaceURI, child.localName, child.qName);
      }
    }
  }



  private void
  synthesizeGroup() throws SAXException
  {
    AttributesImpl	atts = new AttributesImpl();

    atts.addAttribute
    (
      Constants.CSS,
      "display",
      "css:display",
      "CDATA",
      "table-row-group"
    );

    super.startElement(Constants.CSS, "synthetic", "css:synthetic", atts);

    elementStack.push
    (
      new Element(Constants.CSS, "synthetic", "css:synthetic", atts)
    );
  }

} // NormalizeTableFilter
