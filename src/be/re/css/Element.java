package be.re.css;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;



class Element

{

  static final String	BLOCK = "block".intern();
  static final String	COMPACT = "compact".intern();
  static final String	GRAPHIC = "graphic".intern();
  static final String	INLINE = "inline".intern();
  static final String	INLINE_TABLE = "inline-table".intern();
  static final String	LEADER = "leader".intern();
  static final String	LIST_ITEM = "list-item".intern();
  static final String	MARKER = "marker".intern();
  static final String	NONE = "none".intern();
  static final String	RUN_IN = "run-in".intern();
  static final String	TABLE = "table".intern();
  static final String	TABLE_CELL = "table-cell".intern();
  static final String	TABLE_CAPTION = "table-caption".intern();
  static final String	TABLE_COLUMN = "table-column".intern();
  static final String	TABLE_COLUMN_GROUP = "table-column-group".intern();
  static final String	TABLE_FOOTER_GROUP = "table-footer-group".intern();
  static final String	TABLE_HEADER_GROUP = "table-header-group".intern();
  static final String	TABLE_ROW = "table-row".intern();
  static final String	TABLE_ROW_GROUP = "table-row-group".intern();

  Attributes	atts;
  List		children;
  String	display; // Interned.
  Object	extra;
  String	localName;
  String	namespaceURI;
  String	qName;



  Element(String namespaceURI, String localName, String qName, Attributes atts)
  {
    this.namespaceURI = namespaceURI;
    this.localName = localName;
    this.qName = qName;
    this.atts = new AttributesImpl(atts); // Copy because parser reuses them.
    this.display = atts.getValue(Constants.CSS, "display");

    if (display != null)
    {
      display = display.intern();
    }
  }



  void
  addChild(Element child)
  {
    if (children == null)
    {
      children = new ArrayList(10);
    }

    children.add(child);
  }



  boolean
  isDisplay(String knownDisplay)
  {
    return knownDisplay == display;
  }

} // Element
