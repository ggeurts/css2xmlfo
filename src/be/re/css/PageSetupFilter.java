package be.re.css;

import be.re.xml.DOMToContentHandler;
import be.re.xml.sax.GobbleDocumentEvents;
import be.re.xml.sax.FilterOfFilters;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * This filter produces the page setup, taking into account named pages.
 * Named page properties will be considered inside a body region element on
 * blocks and outer tables.
 * @author Werner Donn\u00e9
 */

class PageSetupFilter extends XMLFilterImpl

{

  private static final String		DEFAULT_COLUMN_COUNT = "1";
  private static final String		DEFAULT_REGION_HEIGHT = "10mm";
  private static final String		DEFAULT_REGION_WIDTH = "20mm";

  private static final String[][]	pageInheritanceTable =
    {
      {"first-left-named", "unnamed", "left", "first", "named"},
      {"first-right-named", "unnamed", "right", "first", "named"},
      {"last-left-named", "unnamed", "left", "last", "named"},
      {"last-right-named", "unnamed", "right", "last", "named"},
      {"left-named", "unnamed", "left", "named"},
      {"right-named", "unnamed", "right", "named"},
      {"blank-left-named", "unnamed", "left", "blank", "named"},
      {"blank-right-named", "unnamed", "right", "blank", "named"}
    };
  private static final String[]		prefixes =
      new String[]
      {
        "first-left-", "first-right-", "blank-left-", "blank-right-", "first-",
          "blank-", "left-", "right-", "last-left-", "last-right-", "last-"
      };
  private static final String[][]	regionInheritanceTable =
    {
      {
        "first-left-named", "first-named", "first-left", "first", "left-named",
          "left", "named", "unnamed"
      },
      {
        "first-right-named", "first-named", "first-right", "first",
          "right-named", "right", "named", "unnamed"
      },
      {
        "last-left-named", "last-named", "last-left", "last", "left-named",
          "left", "named", "unnamed"
      },
      {
        "last-right-named", "last-named", "last-right", "last", "right-named",
          "right", "named", "unnamed"
      },
      {"left-named", "left", "named", "unnamed"},
      {"right-named", "right", "named", "unnamed"},
      {
        "blank-left-named", "blank-named", "blank-left", "blank", "left-named",
          "left", "named", "unnamed"
      },
      {
        "blank-right-named", "blank-named", "blank-right", "blank",
          "right-named", "right", "named", "unnamed"
      }
    };

  private URL		baseUrl;
  private Context	context;
  private boolean	debug;
  private Stack		elements = new Stack();
  private PageRule[]	resolvedPageRules;
  private Map		userAgentParameters;



  PageSetupFilter
  (
    Context	context,
    URL		baseUrl,
    Map		userAgentParameters,
    boolean	debug
  )
  {
    this.context = context;
    this.baseUrl = baseUrl;
    this.userAgentParameters = userAgentParameters;
    this.debug = debug;
  }



  PageSetupFilter
  (
    Context	context,
    URL		baseUrl,
    Map		userAgentParameters,
    XMLReader	parent,
    boolean	debug
  )
  {
    super(parent);
    this.context = context;
    this.baseUrl = baseUrl;
    this.userAgentParameters = userAgentParameters;
    this.debug = debug;
  }



  private static List
  addUnnamedPageRule(List pageRules)
  {
    boolean	unnamedPresent = false;

    for (Iterator i = pageRules.iterator(); i.hasNext() && !unnamedPresent;)
    {
      if (((PageRule) i.next()).getName().equals("unnamed"))
      {
        unnamedPresent = true;
      }
    }

    if (unnamedPresent)
    {
      return pageRules;
    }

    PageRule	rule = new PageRule("unnamed", 0);

    rule.addProperty(new Property("size", "portrait", false, null));
    pageRules.add(rule);

    return pageRules;
  }



  private PageRule[]
  applyPageRules() throws SAXException
  {
    if (context.pageRules.size() == 0)
    {
      return new PageRule[0];
    }

    PageRule[]	pageRules =
      recomposePageRules
      (
        sortPageRules
        (
          (PageRule[])
            addUnnamedPageRule(context.pageRules).toArray(new PageRule[0])
        )
      );

    super.
      startElement(Constants.CSS, "pages", "css:pages", new AttributesImpl());

    String[]	names = getPageRuleNames(pageRules);

    for (int i = 0; i < names.length; ++i)
    {
      generatePage
      (
        getPageAttributes
        (
          pageRules,
          names[i],
          getInheritanceTableEntry(pageInheritanceTable, names[i])
        )
      );
    }

    super.endElement(Constants.CSS, "pages", "css:pages");

    return pageRules;
  }



  public void
  characters(char[] ch, int start, int length) throws SAXException
  {
    if (shouldEmitContents())
    {
      super.characters(ch, start, length);
    }
  }



  private boolean
  closeElementsInBodyRegion() throws SAXException
  {
    boolean	closed = false;

    for
    (
      int i = elements.size() - 1;
      i >= 0 && ((Element) elements.get(i)).inBodyRegion;
      --i
    )
    {
      Element	element = (Element) elements.get(i);

      super.endElement(element.namespaceURI, element.localName, element.qName);
      closed = true;
    }

    return closed;
  }



  private void
  emitRegion(org.w3c.dom.Element region, String flowName)
    throws SAXException
  {
    AttributesImpl	atts = new AttributesImpl();
    XMLFilterImpl	filter =
      new FilterOfFilters
      (
        new XMLFilter[]
        {
          Util.createPostProjectionFilter
          (
            baseUrl,
            userAgentParameters,
            debug
          ).getFilter(),
          new GobbleDocumentEvents()
            // Give a chance for initialization, but don't interfere
            // with the chain.
        }
      );

    atts.addAttribute("", "flow-name", "flow-name", "CDATA", flowName);
    filter.setContentHandler(getContentHandler());
    filter.startDocument();

    super.startElement
    (
      Constants.XSLFO,
      "static-content",
      "fo:static-content",
      atts
    );

    DOMToContentHandler.
      elementToContentHandler(removeWidthAndHeight(region), filter);
    filter.endDocument();
    super.endElement(Constants.XSLFO, "static-content", "fo:static-content");
  }



  public void
  endDocument() throws SAXException
  {
    endPrefixMapping("fo");
    endPrefixMapping("css");
    endPrefixMapping("xh");
    endPrefixMapping("sp");
    super.endDocument();
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    Element	element = (Element) elements.pop();

    if (element.inBodyRegion)
    {
      super.endElement(namespaceURI, localName, qName);

      if (elements.isEmpty() || !((Element) elements.peek()).inBodyRegion)
      {
        super.startElement
        (
          Constants.CSS,
          "last-page-mark", 
          "css:last-page-mark",
          new AttributesImpl()
        );

        super.endElement(Constants.CSS, "last-page-mark", "css:last-page-mark");
        super.endElement(Constants.CSS, "page-sequence", "css:page-sequence");
      }
      else
      {
        if (element.span && closeElementsInBodyRegion())
        {
          reopenElementsInBodyRegion(false);
        }
      }
    }

    if (elements.isEmpty())
    {
      super.endElement(Constants.CSS, "root", "css:root");
    }
  }



  private static String
  extractPseudoPrefix(String pageName)
  {
    for (int i = 0; i < prefixes.length; ++i)
    {
      if (pageName.startsWith(prefixes[i]))
      {
        return prefixes[i];
      }
    }

    return "";
  }



  private void
  generateBodyRegionExtent(Attributes pageAtts, AttributesImpl regionAtts)
    throws SAXException
  {
    String	columnCount = pageAtts.getValue(Constants.CSS, "column-count");
    String	columnGap = pageAtts.getValue(Constants.CSS, "column-gap");

    if (columnCount == null)
    {
      columnCount = (String) userAgentParameters.get("column-count");
    }

    if (columnCount != null)
    {
      regionAtts.
        addAttribute("", "column-count", "columnt-count", "CDATA", columnCount);
    }

    if (columnGap != null)
    {
      regionAtts.
        addAttribute("", "column-gap", "columnt-gap", "CDATA", columnGap);
    }

    super.startElement
    (
      Constants.XSLFO,
      "region-body",
      "fo:region-body",
      regionAtts
    );

    super.endElement(Constants.XSLFO, "region-body", "fo:region-body");
  }



  private void
  generatePage(Attributes attributes) throws SAXException
  {
    AttributesImpl	atts = new AttributesImpl(attributes);
    AttributesImpl	bodyRegionAttributes = new AttributesImpl();

    moveBodyProperties(atts, bodyRegionAttributes);
    super.startElement(Constants.CSS, "page", "css:page", atts);

    List		extents = new ArrayList();
    Set			generated = new HashSet();
    String		name = atts.getValue(Constants.CSS, "name");
    String[]		inheritedPages =
      getInheritanceTableEntry(regionInheritanceTable, name);
    String[]		pages = new String[inheritedPages.length + 1];
    String[]		regionNames =
      new String[]{"top", "bottom", "left", "right"};

    System.arraycopy(inheritedPages, 0, pages, 1, inheritedPages.length);
    pages[0] = name;

    for (int i = 0; i < pages.length; ++i)
    {
      Map	regions = (Map) context.regions.get(pages[i]);

      if (regions != null)
      {
        for (int j = 0; j < regionNames.length; ++j)
        {
          if (!generated.contains(regionNames[j]))
          {
            org.w3c.dom.Element	region =
              (org.w3c.dom.Element) regions.get(regionNames[j]);

            if (region != null)
            {
              generated.add(regionNames[j]);

              String	extent;

              if
              (
                "top".equals(regionNames[j])	||
                "bottom".equals(regionNames[j])
              )
              {
                extent = region.getAttributeNS(Constants.CSS, "height");

                if (extent.equals(""))
                {
                  extent = DEFAULT_REGION_HEIGHT;
                }
              }
              else
              {
                extent = region.getAttributeNS(Constants.CSS, "width");

                if (extent.equals(""))
                {
                  extent = DEFAULT_REGION_WIDTH;
                }
              }

              bodyRegionAttributes.addAttribute
              (
                "",
                "margin-" + regionNames[j],
                "margin-" + regionNames[j],
                "CDATA",
                extent
              );

              extents.add
              (
                generateRegionExtent(region, name, regionNames[j], extent)
              );
            }
          }
        }
      }
    }

    // The region order matters.

    generateBodyRegionExtent(atts, bodyRegionAttributes);

    for (Iterator j = sortExtents(extents).iterator(); j.hasNext();)
    {
      DOMToContentHandler.elementToContentHandler
      (
        (org.w3c.dom.Element) j.next(),
        getContentHandler()
      );
    }

    super.endElement(Constants.CSS, "page", "css:page");
  }



  private org.w3c.dom.Element
  generateRegionExtent
  (
    org.w3c.dom.Element	region,
    String		page,
    String		name,
    String		extent
  ) throws SAXException
  {
    String		element =
      "top".equals(name) ?
        "region-before" :
        (
          "bottom".equals(name) ?
            "region-after" :
            ("left".equals(name) ? "region-start" : "region-end")
        );
    String		precedence =
      region.getAttributeNS(Constants.CSS, "precedence");
    org.w3c.dom.Element	result =
      region.getOwnerDocument().
        createElementNS(Constants.XSLFO, "fo:" + element);

    if (!precedence.equals(""))
    {
      result.setAttribute("precedence", precedence);
    }

    result.setAttribute("region-name", page + "-" + name);
    result.setAttribute("extent", extent);

    return result;
  }



  private void
  generateRegions(String page) throws SAXException
  {
    super.startElement
    (
      Constants.CSS,
      "regions",
      "css:regions",
      new AttributesImpl()
    );

    if (context.regions.size() > 0)
    {
      Set	generated = new HashSet();
      String[]	regionNames = new String[]{"top", "bottom", "left", "right"};

      for (int i = 0; i < regionInheritanceTable.length; ++i)
      {
        for (int j = 0; j < regionInheritanceTable[i].length; ++j)
        {
          String	specificPage =
            getSpecificPageName(regionInheritanceTable[i][j], page);
          Map		regions = (Map) context.regions.get(specificPage);

          if (regions != null)
          {
            for (int k = 0; k < regionNames.length; ++k)
            {
              String	flowName =
                getSpecificPageName(regionInheritanceTable[i][0], page) + "-" +
                  regionNames[k];

              if (!generated.contains(flowName))
              {
                org.w3c.dom.Element	region =
                  (org.w3c.dom.Element) regions.get(regionNames[k]);
  
                if (region != null)
                {
                  generated.add(flowName);
                  emitRegion(region, flowName);
                }
              }
            }
          }
        }
      }
    }

    super.endElement(Constants.CSS, "regions", "css:regions");
  }



  private static String[]
  getInheritanceTableEntry(String[][] table, String name)
  {
    String	symbolic = extractPseudoPrefix(name) + "named";
    String	unprefixed = stripPseudoPrefix(name);

    for (int i = 0; i < table.length; ++i)
    {
      if (table[i][0].equals(symbolic))
      {
        List	result = new ArrayList();

        for (int j = 0; j < table[i].length - 1; ++j)
        {
          int	index =
            table[i][j + 1].equals("unnamed") ?
              -1 : table[i][j + 1].indexOf("named");

          if (!unprefixed.equals("unnamed") || !table[i][j + 1].equals("named"))
            // The named page "unnamed" doesn't exist.
          {
            result.add
            (
              index != -1 ?
                (table[i][j + 1].substring(0, index) + unprefixed) :
                table[i][j + 1]
            );
          }
        }

        return (String[]) result.toArray(new String[0]);
      }
    }

    return new String[0];
  }



  /**
   * The page properties are determined in the order of <code>names</code>.
   * A successor overrides the values of its predecessors. This implements the
   * cascade.
   */

  private Attributes
  getPageAttributes(PageRule[] pageRules, String pageName, String[] names)
  {
    AttributesImpl	result = new AttributesImpl();

    result.addAttribute(Constants.CSS, "name", "css:name", "CDATA", pageName);

    for (int i = 0; i < names.length; ++i)
    {
      for (int j = 0; j < pageRules.length; ++j)
      {
        if (names[i].equals(pageRules[j].getName()))
        {
          Property[]	properties = pageRules[j].getProperties();

          for (int k = 0; k < properties.length; ++k)
          {
            Util.setCSSAttribute(result, properties[k], -1);
          }
        }
      }
    }

    return result;
  }



  /**
   * The method expands all the style sheet specified page names into first,
   * last, left, right, blank and any variants, which go in the repeatable page
   * masters. Only the most precise page names remain.
   */

  private static String[]
  getPageRuleNames(PageRule[] rules)
  {
    Set	result = new HashSet();

    for (int i = 0; i < rules.length; ++i)
    {
      String	stripped = stripPseudoPrefix(rules[i].getName());

      if (!isPseudoPageName(rules[i].getName()))
      {
        result.add("first-left-" + stripped);
        result.add("first-right-" + stripped);
        result.add("last-left-" + stripped);
        result.add("last-right-" + stripped);
        result.add("blank-left-" + stripped);
        result.add("blank-right-" + stripped);
        result.add("left-" + stripped);
        result.add("right-" + stripped);
      }
    }

    result.remove("first");
    result.remove("last");
    result.remove("blank");
    result.remove("left");
    result.remove("right");
    result.remove("first-left");
    result.remove("first-right");
    result.remove("last-left");
    result.remove("last-right");
    result.remove("blank-left");
    result.remove("blank-right");
    result.remove("unnamed");

    return (String[]) result.toArray(new String[result.size()]);
  }



  private static String
  getSpecificPageName(String symbolicName, String page)
  {
    return
      symbolicName.indexOf("-named") != -1 ?
        symbolicName.substring(0, symbolicName.indexOf("-named")) + "-" + page :
        ("named".equals(symbolicName) ? page : symbolicName);
  }



  private static boolean
  isPseudoPageName(String name)
  {
    return
      Util.
        inArray(new String[]{"first", "last", "left", "right", "blank"}, name);
  }



  private static void
  moveBodyProperties(AttributesImpl pageAtts, AttributesImpl regionAtts)
  {
    for (int i = 0; i < pageAtts.getLength(); ++i)
    {
      if
      (
        Constants.CSS.equals(pageAtts.getURI(i))		&&
        (
          pageAtts.getLocalName(i).startsWith("background-")	||
          pageAtts.getLocalName(i).startsWith("border-")	||
          pageAtts.getLocalName(i).startsWith("padding-")
        )
      )
      {
        if (!pageAtts.getLocalName(i).endsWith(".conditionality"))
        {
          regionAtts.addAttribute
          (
            pageAtts.getURI(i),
            pageAtts.getLocalName(i),
            pageAtts.getQName(i),
            pageAtts.getType(i),
            pageAtts.getValue(i)
          );
        }

        pageAtts.removeAttribute(i--);
      }
    }
  }



  public void
  processingInstruction(String target, String data) throws SAXException
  {
    if (shouldEmitContents())
    {
      super.processingInstruction(target, data);
    }
  }



  /**
   * This performs the cascade.
   */

  private static PageRule[]
  recomposePageRules(PageRule[] pageRules)
  {
    Map	result = new HashMap();

    for (int i = 0; i < pageRules.length; ++i)
    {
      PageRule	pageRule = (PageRule) result.get(pageRules[i].getName());

      if (pageRule == null)
      {
        pageRule = new PageRule(pageRules[i].getName(), 0);
        result.put(pageRule.getName(), pageRule);
      }

      pageRule.setProperty(pageRules[i].getProperties()[0]);
    }

    return (PageRule[]) result.values().toArray(new PageRule[0]);
  }



  private static AttributesImpl
  removeId(AttributesImpl atts)
  {
    for (int i = 0; i < atts.getLength(); ++i)
    {
      if ("ID".equals(atts.getType(i)))
      {
        atts.removeAttribute(i);

        return atts;
      }
    }

    return atts;
  }



  private static org.w3c.dom.Element
  removeWidthAndHeight(org.w3c.dom.Element region)
  {
    org.w3c.dom.Element	result = (org.w3c.dom.Element) region.cloneNode(true);

    result.removeAttributeNS(Constants.CSS, "width");
    result.removeAttributeNS(Constants.CSS, "height");

    return result;
  }



  private void
  reopenElementsInBodyRegion(boolean span) throws SAXException
  {
    int	i;

    for
    (
      i = elements.size() - 1;
      i >= 0 && ((Element) elements.get(i)).inBodyRegion;
      --i
    );

    if (((Element) elements.get(i + 1)).inBodyRegion)
    {
      for (int j = i + 1; j < elements.size(); ++j)
      {
        Element		element = (Element) elements.get(j);
        AttributesImpl	atts = removeId(element.atts); // Avoid duplicate IDs.

        if (span && j == i + 1)
        {
          atts = new AttributesImpl(atts);

          atts.addAttribute
          (
            Constants.CSS,
            "column-span",
            "css:column-span",
            "CDATA",
            "all"
          );
        }

        super.startElement
        (
          element.namespaceURI,
          element.localName,
          element.qName,
          atts
        );
      }
    }
  }



  void
  setBaseUrl(URL baseUrl)
  {
    this.baseUrl = baseUrl;
  }



  private boolean
  shouldEmitContents()
  {
    return ((Element) elements.peek()).inBodyRegion;
  }



  private static Collection
  sortExtents(Collection extents)
  {
    Collection	result =
      new TreeSet
      (
        new Comparator()
        {
          public int
          compare(Object o1, Object o2)
          {
            String[]	ordered =
              new String[]
              {"region-before", "region-after", "region-start", "region-end"};

            return
              Util.indexOf(ordered, ((Node) o1).getLocalName()) -
                Util.indexOf(ordered, ((Node) o2).getLocalName());
          }
        }
      );

    result.addAll(extents);

    return result;
  }



  private static PageRule[]
  sortPageRules(PageRule[] pageRules)
  {
    Arrays.sort
    (
      pageRules,
      new Comparator()
      {
        public int
        compare(Object object1, Object object2)
        {
          PageRule	rule1 = (PageRule) object1;
          PageRule	rule2 = (PageRule) object2;
          String	name1 = rule1.getName();
          String	name2 = rule2.getName();
          int		result =
            !"unnamed".equals(name1) && "unnamed".equals(name2) ?
              1 :
              ("unnamed".equals(name1) && !"unnamed".equals(name2) ? -1 : 0);

          if (result == 0)
          {
            result =
              "first".equals(name1) && !"first".equals(name2) ?
                1 : (!"first".equals(name1) && "first".equals(name2) ? -1 : 0);
          }

          if (result == 0)
          {
            result =
              "last".equals(name1) && !"last".equals(name2) ?
                1 : (!"last".equals(name1) && "last".equals(name2) ? -1 : 0);
          }

          if (result == 0)
          {
            result =
              "left".equals(name1) && !"left".equals(name2) ?
                1 : (!"left".equals(name1) && "left".equals(name2) ? -1 : 0);
          }

          if (result == 0)
          {
            result =
              "right".equals(name1) && !"right".equals(name2) ?
                1 : (!"right".equals(name1) && "right".equals(name2) ? -1 : 0);
          }

          if (result == 0)
          {
            result = rule1.getPosition() - rule2.getPosition();
          }

          return result;
        }
      }
    );

    return pageRules;
  }



  public void
  startDocument() throws SAXException
  {
    super.startDocument();
    startPrefixMapping("css", Constants.CSS);
    startPrefixMapping("xh", Constants.XHTML);
    startPrefixMapping("sp", Constants.SPECIF);
    startPrefixMapping("fo", Constants.XSLFO);
    getParent().setContentHandler(new Recorder());
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
    Element	element = new Element(namespaceURI, localName, qName, atts);
    Element	parent = elements.isEmpty() ? null : (Element) elements.peek();

    if (parent == null)
    {
      super.startElement(Constants.CSS, "root", "css:root", atts);
    }

    element.inBodyRegion =
      (parent != null && parent.inBodyRegion) ||
        "body".equals(atts.getValue(Constants.CSS, "region"));
    element.inTable =
      (parent != null && parent.inTable) || "table".equals(display);

    if
    (
      element.inBodyRegion		&&
      (
        parent == null			||
        (
          !parent.inTable		&&
          (
            element.inTable		||
            "block".equals(display)
          )
        )
      )
    )
    {
      int	span = element.atts.getIndex(Constants.CSS, "column-span");

      if (span != -1)
      {
        element.span = "all".equals(element.atts.getValue(span));
        element.atts.removeAttribute(span);
      }

      element.pageName = atts.getValue(Constants.CSS, "page");

      if ("auto".equals(element.pageName))
      {
        element.pageName = null;
      }

      if (element.pageName == null && parent != null)
      {
        element.pageName = parent.pageName;
      }

      if (element.pageName == null)
      {
        element.pageName = "unnamed";
      }

      boolean	newPage =
        parent == null || !element.pageName.equals(parent.pageName);

      if (newPage || element.span)
      {
        boolean		closed = closeElementsInBodyRegion();

        if (newPage)
        {
          if (parent != null && parent.pageName != null)
            // There is an open page sequence.
          {
            super.
              endElement(Constants.CSS, "page-sequence", "css:page-sequence");
          }
          else
          {
            // Pages go before the first page sequence.
            resolvedPageRules = applyPageRules();
          }

          AttributesImpl	attributes = new AttributesImpl();

          attributes.addAttribute
          (
            Constants.CSS,
            "page",
            "css:page",
            "CDATA",
            element.pageName
          );

          super.startElement
          (
            Constants.CSS,
            "page-sequence",
            "css:page-sequence",
            attributes
          );

          generateRegions(element.pageName);
        }

        if (closed)
        {
          reopenElementsInBodyRegion(element.span);
        }

        if (parent != null)
        {
          parent.pageName = element.pageName;
        }
      }
    }
    else
    {
      if (parent != null)
      {
        element.pageName = parent.pageName;
      }
    }

    if (element.inBodyRegion)
    {
      super.startElement(namespaceURI, localName, qName, element.atts);
    }

    elements.push(element);
  }



  private static String
  stripPseudoPrefix(String pageName)
  {
    for (int i = 0; i < prefixes.length; ++i)
    {
      if (pageName.startsWith(prefixes[i]))
      {
        return pageName.substring(prefixes[i].length());
      }
    }

    return pageName;
  }



  private static class Element

  {

    private AttributesImpl	atts;
    private boolean		inBodyRegion;
    private boolean		inTable;
    private String		localName;
    private String		namespaceURI;
    private String		pageName;
    private String		qName;
    private boolean		span;


    private
    Element
    (
      String		namespaceURI,
      String		localName,
      String		qName,
      Attributes	atts
    )
    {
      this.namespaceURI = namespaceURI;
      this.localName = localName;
      this.qName = qName;
      this.atts = new AttributesImpl(atts);
    }

  } // Element



  private class Recorder extends XMLFilterImpl

  {

    private List	events = new ArrayList();
    private Stack	elements = new Stack();



    public void
    endElement(String namespaceURI, String localName, String qName)
      throws SAXException
    {
      elements.pop();
      events.add(new Event(namespaceURI, localName, qName, null));
    }



    private void
    replayEvents() throws SAXException
    {
      for (int i = 0; i < events.size(); ++i)
      {
        Event	event = (Event) events.get(i);

        if (event.atts != null)
        {
          PageSetupFilter.this.startElement
          (
            event.namespaceURI,
            event.localName,
            event.qName,
            event.atts
          );
        }
        else
        {
          PageSetupFilter.this.
            endElement(event.namespaceURI, event.localName, event.qName);
        }
      }
    }



    public void
    startElement
    (
      String		namespaceURI,
      String		localName,
      String		qName,
      Attributes	atts
    ) throws SAXException
    {
      events.add(new Event(namespaceURI, localName, qName, atts));

      if (!elements.isEmpty() && ((Element) elements.peek()).inBodyRegion)
      {
        replayEvents();
        PageSetupFilter.this.getParent().
          setContentHandler(PageSetupFilter.this);
      }
      else
      {
        Element	element = new Element(namespaceURI, localName, qName, atts);

        element.inBodyRegion =
          "body".equals(atts.getValue(Constants.CSS, "region"));
        elements.push(element);
      }
    }



    private class Event

    {

      private Attributes	atts;
      private String		localName;
      private String		namespaceURI;
      private String		qName;



      private
      Event
      (
        String		namespaceURI,
        String		localName,
        String		qName,
        Attributes	atts
      )
      {
        this.namespaceURI = namespaceURI;
        this.localName = localName;
        this.qName = qName;
        this.atts = (atts == null ? null : new AttributesImpl(atts));
      }

    } // Event

  } // Recorder

} // PageSetupFilter
