package be.re.css;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Detects centering (right and left margins set to "auto") and wraps tables
 * and blocks in a three column table.
 * @author Werner Donn\u00e9
 */

class CenterFilter extends XMLFilterImpl

{

  private Stack	stack = new Stack();



  CenterFilter()
  {
  }



  CenterFilter(XMLReader parent)
  {
    super(parent);
  }



  private void
  column(String width) throws SAXException
  {
    AttributesImpl	atts = displayType("table-column");

    if (width != null)
    {
      atts.addAttribute(Constants.CSS, "width", "css:width", "CDATA", width);
    }

    super.startElement(Constants.CSS, "table-column", "css:table-column", atts);
    super.endElement(Constants.CSS, "table-column", "css:table-column");
  }



  private static AttributesImpl
  displayType(String type)
  {
    AttributesImpl	atts = new AttributesImpl();

    atts.addAttribute
    (
      Constants.CSS,
      "display",
      "css:display",
      "CDATA",
      type
    );

    return atts;
  }



  private void
  emptyCell() throws SAXException
  {
    super.startElement
    (
      Constants.CSS,
      "table-cell",
      "css:table-cell",
      displayType("table-cell")
    );

    super.endElement(Constants.CSS, "table-cell", "css:table-cell");
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    super.endElement(namespaceURI, localName, qName);

    if (((Boolean) stack.pop()).booleanValue())
    {
      super.endElement(Constants.CSS, "table-cell", "css:table-cell");
      emptyCell();
      super.endElement(Constants.CSS, "table-row", "css:table-row");
      super.endElement(Constants.CSS, "table-row-group", "css:table-row-group");
      super.endElement(Constants.CSS, "table", "css:table");
    }
  }



  private void
  generateTable(Attributes atts) throws SAXException
  {
    AttributesImpl	tableAtts = displayType("table");

    Util.copyAttribute(atts, tableAtts, Constants.CSS, "margin-bottom");
    Util.copyAttribute(atts, tableAtts, Constants.CSS, "margin-top");

    tableAtts.addAttribute
    (
      Constants.CSS,
      "table-layout",
      "css:table-layout",
      "CDATA",
      "fixed"
    );

    super.startElement(Constants.CSS, "table", "css:table", tableAtts);
    column("1*");
    column(atts.getValue(Constants.CSS, "width"));
    column("1*");

    super.startElement
    (
      Constants.CSS,
      "table-row-group",
      "css:table-row-group",
      displayType("table-row-group")
    );

    super.startElement
    (
      Constants.CSS,
      "table-row",
      "css:table-row",
      displayType("table-row")
    );

    emptyCell();

    super.startElement
    (
      Constants.CSS,
      "table-cell",
      "css:table-cell",
      displayType("table-cell")
    );
  }



  private static boolean
  shouldCenter(Attributes atts)
  {
    return
      "auto".equals(atts.getValue(Constants.CSS, "margin-left")) &&
        "auto".equals(atts.getValue(Constants.CSS, "margin-right"));
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
    boolean	extra = false;

    if
    (
      (
        "block".equals(display)	||
        "table".equals(display)
      )				&&
      shouldCenter(atts)
    )
    {
      extra = true;
      generateTable(atts);
      atts = new AttributesImpl(atts);
      Util.
        removeAttribute((AttributesImpl) atts, Constants.CSS, "margin-bottom");
      Util.removeAttribute((AttributesImpl) atts, Constants.CSS, "margin-top");
      Util.removeAttribute((AttributesImpl) atts, Constants.CSS, "margin-left");
      Util.
        removeAttribute((AttributesImpl) atts, Constants.CSS, "margin-right");

      Util.setAttribute
      (
        (AttributesImpl) atts,
        Constants.CSS,
        "width",
        "css:width",
        "100%"
      );
    }

    stack.push(new Boolean(extra));
    super.startElement(namespaceURI, localName, qName, atts);
  }

} // CenterFilter
