package be.re.css;

import be.re.xml.Accumulator;
import be.re.xml.DOMToContentHandler;
import be.re.xml.sax.FilterOfFilters;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Creates table structures to implement the marker display type.
 * @author Werner Donn\u00e9
 */

class MarkerFilter extends XMLFilterImpl

{

  private static final String	DEFAULT_WIDTH = "2em";



  MarkerFilter()
  {
  }



  MarkerFilter(XMLReader parent)
  {
    super(parent);
  }



  private void
  accumulate
  (
    String	namespaceURI,
    String	localName,
    String	qName,
    Attributes	atts
  ) throws SAXException
  {
    Accumulator.preAccumulate
    (
      namespaceURI,
      localName,
      qName,
      atts,
      this,
      new Accumulator.ProcessElement()
      {
        public void
        process(Element element, XMLFilter filter) throws SAXException
        {
          DOMToContentHandler.elementToContentHandler
          (
            transform(element),
            filter.getContentHandler()
          );
        }
      }
    );
  }



  private static void
  addBody(Element table, Element element, Element before, Element after)
  {
    Element	body =
      table.getOwnerDocument().
        createElementNS(Constants.CSS, "table-row-group");

    body.setAttributeNS(Constants.CSS, "css:display", "table-row-group");
    table.appendChild(body);

    Element	row =
      table.getOwnerDocument().createElementNS(Constants.CSS, "table-row");

    row.setAttributeNS(Constants.CSS, "css:display", "table-row");
    body.appendChild(row);

    Element	mainCell =
      table.getOwnerDocument().createElementNS(Constants.CSS, "table-cell");

    mainCell.setAttributeNS(Constants.CSS, "css:display", "table-cell");
    row.appendChild(mainCell);
    mainCell.appendChild(element);

    if (before != null)
    {
      Element	cell =
        table.getOwnerDocument().createElementNS(Constants.CSS, "table-cell");

      cell.setAttributeNS(Constants.CSS, "css:display", "table-cell");
      cell.setAttributeNS(Constants.CSS, "css:vertical-align", "top");
      row.insertBefore(cell, mainCell);
      addMarker(cell, before, "right");
    }

    if (after != null)
    {
      Element	cell =
        table.getOwnerDocument().createElementNS(Constants.CSS, "table-cell");

      cell.setAttributeNS(Constants.CSS, "css:display", "table-cell");
      cell.setAttributeNS(Constants.CSS, "css:vertical-align", "bottom");
      row.appendChild(cell);
      addMarker(cell, after, "left");
    }
  }



  private static void
  addColumn(Element table, String width)
  {
    Element	column =
      table.getOwnerDocument().createElementNS(Constants.CSS, "table-column");

    column.setAttributeNS(Constants.CSS, "css:display", "table-column");

    column.setAttributeNS
    (
      Constants.CSS,
      "css:width",
      width.equals("") ? "1*" : width
    );
    table.appendChild(column);
  }



  private static void
  addMarker(Element cell, Element marker, String side)
  {
    cell.appendChild(marker);
    marker.removeAttributeNS(Constants.CSS, "width");
    marker.setAttributeNS(Constants.CSS, "css:display", "block");

    String	markerOffset =
      marker.getAttributeNS(Constants.CSS, "marker-offset");

    if (!markerOffset.equals(""))
    {
      cell.setAttributeNS(Constants.CSS, "css:padding-" + side, markerOffset);
      marker.removeAttributeNS(Constants.CSS, "marker-offset");
    }
  }



  private static Element
  getAfterPseudoElement(Node node)
  {
    return
      node == null ?
        null :
        (
          node instanceof Element &&
            Constants.CSS.equals(node.getNamespaceURI()) &&
            "after".equals(node.getLocalName()) &&
            "marker".equals
            (
              ((Element) node).getAttributeNS(Constants.CSS, "display")
            ) ?
            (Element) node :
            getAfterPseudoElement(node.getPreviousSibling())
        );
  }



  private static Element
  getBeforePseudoElement(Node node)
  {
    return
      node == null ?
        null :
        (
          node instanceof Element &&
            Constants.CSS.equals(node.getNamespaceURI()) &&
            "before".equals(node.getLocalName()) &&
            "marker".equals
            (
              ((Element) node).getAttributeNS(Constants.CSS, "display")
            ) ?
            (Element) node :
            getBeforePseudoElement(node.getNextSibling())
        );
  }



  private static Element
  handleNestedMarkers(Element element) throws SAXException
  {
    Accumulator		result = new Accumulator();
    FilterOfFilters	filter =
      new FilterOfFilters
      (
        // The MarkerFilter needs a parent to insert its own accumulator.
        new XMLFilter[]{new XMLFilterImpl(), new MarkerFilter(), result}
      );

    filter.startDocument();
    DOMToContentHandler.elementToContentHandler(element, filter);
    filter.endDocument();

    return
      (Element)
        element.getOwnerDocument().
          importNode(result.getDocument().getDocumentElement(), true);
  }



  private static void
  moveInheritedProperties(Element element, Element table)
  {
    NamedNodeMap	attributes = element.getAttributes();

    for (int i = 0; i < attributes.getLength(); ++i)
    {
      Attr	attribute = (Attr) attributes.item(i);

      if
      (
        Constants.CSS.equals(attribute.getNamespaceURI())	&&
        Util.isInherited(attribute.getLocalName())
      )
      {
        element.removeAttributeNode(attribute);
        table.setAttributeNodeNS(attribute);
        --i;
      }
    }
  }



  private static void
  moveMargin(Element element, Element table, String side)
  {
    Attr	margin =
      element.getAttributeNodeNS(Constants.CSS, "margin-" + side);

    if (margin != null && !margin.getValue().equals(""))
    {
      element.removeAttributeNode(margin);
      table.setAttributeNodeNS(margin);
    }
  }



  private static void
  moveMargins(Element element, Element table, String beforeWidth)
  {
    String	margin = element.getAttributeNS(Constants.CSS, "margin-left");

    if (Util.isZeroLength(margin))
    {
      margin = "";
    }

    if (beforeWidth != null || !margin.equals(""))
    {
      table.setAttributeNS
      (
        Constants.CSS,
        "css:margin-left",
        (beforeWidth != null ? ("-" + beforeWidth) : "") +
          (!margin.equals("") ? ("+" + margin) : "")
      );
    }

    if (!margin.equals(""))
    {
      element.removeAttributeNS(Constants.CSS, "margin-left");
    }

    moveMargin(element, table, "right");
    moveMargin(element, table, "top");
    moveMargin(element, table, "bottom");
  }



  private static void
  removeMargins(Element marker)
  {
    marker.removeAttributeNS(Constants.CSS, "margin-left");
    marker.removeAttributeNS(Constants.CSS, "margin-right");
    marker.removeAttributeNS(Constants.CSS, "margin-top");
    marker.removeAttributeNS(Constants.CSS, "margin-bottom");
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
    if ("1".equals(atts.getValue(Constants.CSS, "has-markers")))
    {
      accumulate(namespaceURI, localName, qName, atts);
    }
    else
    {
      super.startElement(namespaceURI, localName, qName, atts);
    }
  }



  private static Element
  transform(Element element) throws SAXException
  {
    Element	after = getAfterPseudoElement(element.getLastChild());
    Element	before = getBeforePseudoElement(element.getFirstChild());
    String	beforeWidth = null;
    Element	table =
      element.getOwnerDocument().createElementNS(Constants.CSS, "table");

    table.setAttributeNS(Constants.CSS, "css:display", "table");
    table.setAttributeNS(Constants.CSS, "css:table-layout", "fixed");

    if (before != null)
    {
      beforeWidth = before.getAttributeNS(Constants.CSS, "width");

      if (beforeWidth.equals("") || beforeWidth.equals("auto"))
      {
        beforeWidth = DEFAULT_WIDTH;
      }

      addColumn(table, beforeWidth);

      if ("list-item".equals(element.getAttributeNS(Constants.CSS, "display")))
      {
        element.setAttributeNS(Constants.CSS, "css:display", "block");
      }

      element.removeChild(before);
      removeMargins(before);
    }

    addColumn(table, element.getAttributeNS(Constants.CSS, "width"));
    element.setAttributeNS(Constants.CSS, "css:width", "100%");

    if (after != null)
    {
      String	width = before.getAttributeNS(Constants.CSS, "width");

      addColumn
      (
        table,
        !width.equals("") && !width.equals("auto") ? width : DEFAULT_WIDTH
      );

      element.removeChild(after);
      removeMargins(after);
    }

    moveMargins(element, table, beforeWidth);
    moveInheritedProperties(element, table);
    element.removeAttributeNS(Constants.CSS, "has-markers");
    addBody(table, handleNestedMarkers(element), before, after);

    return table;
  }

} // MarkerFilter
