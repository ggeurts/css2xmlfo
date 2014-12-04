package be.re.css;

import be.re.xml.Accumulator;
import be.re.xml.DOMToContentHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * Moves FO-markers to the next allowed place.
 * @author Werner Donn\u00e9
 */

class FOMarkerFilter extends XMLFilterImpl

{

  private static final Set		allowedPlaces =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "block", "inline", "list-item", "table", "table-cell",
            "table-footer-group", "table-header-group", "table-row-group"
        }
      )
    );

  private List	foMarkers = new ArrayList();
  private Stack	stack = new Stack();



  FOMarkerFilter()
  {
  }



  FOMarkerFilter(XMLReader parent)
  {
    super(parent);
  }



  private void
  accumulateFOMarker
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
          foMarkers.add(element);
        }
      }
    );
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    stack.pop();
    super.endElement(namespaceURI, localName, qName);
  }



  private void
  flushFOMarkers() throws SAXException
  {
    if (foMarkers.size() > 0)
    {
      for (Iterator i = foMarkers.iterator(); i.hasNext();)
      {
        Element	element = (Element) i.next();

        DOMToContentHandler.
          elementToContentHandler(element, getContentHandler());
      }

      foMarkers.clear();
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
    String	parentDisplay = stack.isEmpty() ? null : (String) stack.peek();

    if
    (
      parentDisplay != null			&&
      Constants.CSS == namespaceURI		&&
      "fo-marker".equals(localName)		&&
      !allowedPlaces.contains(parentDisplay)
    )
    {
      accumulateFOMarker(namespaceURI, localName, qName, atts);
    }
    else
    {
      super.startElement(namespaceURI, localName, qName, atts);

      String	display =
        parentDisplay != null && parentDisplay.equals("none") ?
          "none" : atts.getValue(Constants.CSS, "display");

      if
      (
        (
          Constants.CSS != namespaceURI		||
          !"fo-marker".equals(localName)
        )					&&
        allowedPlaces.contains(display)
      )
      {
        flushFOMarkers();
      }

      stack.push(display);
    }
  }

} // FOMarkerFilter
