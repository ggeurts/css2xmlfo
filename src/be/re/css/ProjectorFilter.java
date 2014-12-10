package be.re.css;

import be.re.xml.Accumulator;
import be.re.xml.DOMToContentHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * This class implements the CSS cascading mechanism. It projects the CSS
 * properties onto the elements in the source document according to the CSS
 * style sheet. They are represented as attributes in a distinct namespace. An
 * external style sheet can be specified with the processing instruction which is
 * proposed in section 2.2 of the CSS2 specification.
 *
 * Only properties from the "all" and "print" media are considered, besides the
 * media-neutral ones.
 *
 * The filter works for any XML vocabulary, but special measures have been taken
 * for the XHTML namespace. The style attribute, for example, is recognized and
 * applied. The XHTML methods of specifying style sheets, such as the link and
 * style elements, are also honored.
 *
 * All shorthand properties are split into their constituting parts. This makes
 * it easier to write an XSLT style sheet which transforms the result of this
 * class into XSL-FO.
 *
 * @author Werner Donn\u00e9
 * @author Gerke Geurts
 */
class ProjectorFilter extends XMLFilterImpl
{
    private static final String DEFAULT_CLOSE_QUOTE = "\"";
    private static final String DEFAULT_OPEN_QUOTE = "\"";

    private static final String AFTER = "after".intern();
    private static final String BASE = "base".intern();
    private static final String BEFORE = "before".intern();
    private static final String BODY = "body".intern();
    private static final String FIRST_LETTER = "first-letter".intern();
    private static final String FIRST_LINE = "first-line".intern();
    private static final String LINK = "link".intern();
    private static final String STYLE = "style".intern();

    private static final String[][] pageFormatTable =
    {
        { "armenian", "&#x0561;" },
        { "decimal", "1" },
        { "decimal-leading-zero", "01" },
        { "georgian", "&#x10D0;" },
        { "hebrew", "&#x05D0;" },
        { "hiragana", "&#x3042;" },
        { "hiragana-iroha", "&#x3044;" },
        { "katakana", "&#x30A2;" },
        { "katakana-iroha", "&#x30A4;" },
        { "lower-alpha", "a" },
        { "lower-greek", "&#x03B1;" },
        { "lower-latin", "a" },
        { "lower-roman", "i" },
        { "upper-alpha", "A" },
        { "upper-latin", "A" },
        { "upper-roman", "I" },
    };


    private final CSSResolver cssResolver;
    private final Context context;
    private URL baseUrl = null;
    private boolean collectStyleSheet = false;
    private final Stack counters = new Stack();
    private final Stack elements = new Stack();
    private String embeddedStyleSheet = "";
    private Matcher matcher = null;
    private final Stack namedStrings = new Stack();
    private int quoteDepth = 0;
    // Filter state because quotes can match across the whole document.
    private Map<String, String> userAgentParameters;
    private URL userAgentStyleSheet = null;

    ProjectorFilter(Context context)
    {
        this(null, null, new HashMap(), context, null);
    }

    ProjectorFilter(URL baseUrl, URL userAgentStyleSheet, Map<String, String> userAgentParameters, Context context, CSSResolver cssResolver)
    {
        this.cssResolver = cssResolver != null
                ? cssResolver
                : new DefaultCSSResolver();
        this.baseUrl = baseUrl;
        this.userAgentStyleSheet = userAgentStyleSheet != null
                ? userAgentStyleSheet 
                : getClass().getResource("style/ua.css");
        this.userAgentParameters = userAgentParameters;
        this.context = context;
    }

    private static void addFOMarker(Node parent, String name, String value)
    {
        org.w3c.dom.Element element = parent.getOwnerDocument().createElementNS(Constants.CSS, "css:fo-marker");
        element.appendChild(parent.getOwnerDocument().createTextNode(value));
        element.setAttributeNS(Constants.CSS, "css:name", name);
        parent.insertBefore(element, parent.getFirstChild());
    }

    @SuppressWarnings("StringEquality")
    private Collection<Rule> appendStyleAttributeRules(Collection<Rule> matchingRules, Attributes atts, String namespaceURI) throws SAXException
    {
        if (Constants.XHTML != namespaceURI)
        {
            return matchingRules;
        }

        String style = atts.getValue("style");

        if (style == null || "".equals(style))
        {
            return matchingRules;
        }

        List<CSSRule> rules = CSSRule.parseStyle(style, baseUrl);
        List<Rule> result = new ArrayList<>(matchingRules.size() + rules.size());
        result.addAll(matchingRules);
        for (CSSRule cssRule : rules)
        {
            for (Property p : cssRule.getProperties())
            {
                result.add(new Rule(cssRule, p, Integer.MAX_VALUE, Integer.MAX_VALUE));
            }
        }
        
        return result;
    }

    private void applyContentProperty(Element element, List<Rule> pseudoRules) throws SAXException
    {
        // From most to least specific.
        for (int i = pseudoRules.size() - 1; i >= 0; --i)
        {
            Property property = pseudoRules.get(i).getProperty();

            if ("content".equals(property.getName()) && property.getLexicalUnit().getLexicalUnitType() != LexicalUnit.SAC_INHERIT)
            {
                for (LexicalUnit k = property.getLexicalUnit(); k != null; k = k.getNextLexicalUnit())
                {
                    switch (k.getLexicalUnitType())
                    {
                        case LexicalUnit.SAC_ATTR:
                            serializeAttrFunction(k, element, property.getPrefixMap());
                            break;

                        case LexicalUnit.SAC_COUNTER_FUNCTION:
                            serializeCounterFunction(k);
                            break;

                        case LexicalUnit.SAC_COUNTERS_FUNCTION:
                            serializeCountersFunction(k);
                            break;

                        case LexicalUnit.SAC_FUNCTION:
                            serializeFunction(k, element, property.getPrefixMap());
                            break;

                        case LexicalUnit.SAC_IDENT:
                            serializeQuote(k, element);
                            break;

                        case LexicalUnit.SAC_STRING_VALUE:
                            serializeString(k.getStringValue());
                            break;

                        case LexicalUnit.SAC_URI:
                            serializeUriFunction(k);
                            break;

                        default:
                            break;
                    }
                }

                return;
            }
        }
    }

    private void addFirstLetterMarker(Element element)
    {
        List<Rule> pseudoRules = selectPseudoRules(element.matchingPseudoRules, FIRST_LETTER);

        if (!pseudoRules.isEmpty())
        {
            element.appliedAttributes.addAttribute(Constants.CSS, "has-first-letter", "css:has-first-letter", "CDATA", "1");
        }
    }

    @SuppressWarnings("StringEquality")
    private void applyPseudoRules(Element element, String name) throws SAXException
    {
        List<Rule> pseudoRules = selectPseudoRules(element.matchingPseudoRules, name);

        if (!pseudoRules.isEmpty())
        {
            AttributesImpl extra = new AttributesImpl();

            if (AFTER == name || BEFORE == name)
            {
                extra.addAttribute(Constants.CSS, "display", "css:display", "CDATA", "inline");
            }

            AttributesImpl attributes = setCSSAttributes(pseudoRules, extra);
            AttributesImpl changeBarAttributes = splitChangeBarAttributes(attributes);

            if (BEFORE == name)
            {
                serializeChangeBarBegin(changeBarAttributes);
            }

            super.startElement(Constants.CSS, name, "css:" + name, attributes);

            if (AFTER == name || BEFORE == name)
            {
                serializeFOMarkers(pseudoRules);
                applyContentProperty(element, pseudoRules);
            }

            super.endElement(Constants.CSS, name, "css:" + name);

            if (AFTER == name)
            {
                serializeChangeBarEnd(changeBarAttributes);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (collectStyleSheet)
        {
            embeddedStyleSheet += new String(ch, start, length);
        }

        super.characters(ch, start, length);
    }

    private static String convertPageFormat(String listStyle)
    {
        for (int i = 0; i < pageFormatTable.length; ++i)
        {
            if (listStyle.equals(pageFormatTable[i][0]))
            {
                return pageFormatTable[i][1];
            }
        }

        return "1"; // Decimal is the default.
    }

    @SuppressWarnings("StringEquality")
    private static void detectMarkers(Element element)
    {
        for (Rule rule : element.matchingPseudoRules)
        {
            if ((rule.getPseudoElementName() == BEFORE
                    || rule.getPseudoElementName() == AFTER)
                    && "display".equals(rule.getProperty().getName())
                    && "marker".equals(rule.getProperty().getValue()))
            {
                // Marker found
                element.appliedAttributes.addAttribute(Constants.CSS, "has-markers", "css:has-markers", "CDATA", "1");
                return;
            }
        }
    }

    @Override
    public void endDocument() throws SAXException
    {
        endPrefixMapping("css");
        endPrefixMapping("sp");
        super.endDocument();
        matcher.endDocument();
        reset();
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        counters.pop();
        namedStrings.pop();
        matcher.endElement(namespaceURI, localName, qName);

        if (collectStyleSheet)
        {
            collectStyleSheet = false;
            addStyleSheet(CSSRuleSet.parse(baseUrl, embeddedStyleSheet, cssResolver), 0, true);
            embeddedStyleSheet = "";
        }

        Element element = (Element) elements.pop();

        applyPseudoRules(element, AFTER);
        super.endElement(element.namespaceURI, element.localName, element.qName);
        element.matchingElementRules = null;
        element.matchingPseudoRules = null;
        element.appliedAttributes = null;

        if (element.floating)
        {
            super.endElement(Constants.CSS, "float", "css:float");
        }
    }

    private static String evaluateAttrFunction(LexicalUnit function, Attributes attributes, Map prefixMap)
    {
        if (function.getStringValue() == null)
        {
            return "";
        }

        String attribute = function.getStringValue();
        int index = attribute.indexOf('|');
        String value = index == -1
                ? attributes.getValue(attribute)
                : attributes.getValue(
                        (String) prefixMap.get(attribute.substring(0, index)),
                        attribute.substring(index + 1));

        return value != null ? value : "";
    }

    private String evaluateCounterFunction(LexicalUnit function)
    {
        if (function.getParameters() == null)
        {
            return "";
        }

        String counter = function.getParameters().getStringValue();
        if (counter == null)
        {
            return "";
        }

        Integer value = (Integer) findCounterScope(counter).get(counter.toLowerCase());
        String listStyle = getCounterListStyle(function);

        return value != null && !"none".equalsIgnoreCase(listStyle)
                && !"inherit".equalsIgnoreCase(listStyle)
                        ? getCounterString(value, listStyle) 
                        : "";
    }

    private String evaluateCountersFunction(LexicalUnit function) throws SAXException
    {
        if (function.getParameters() == null)
        {
            return "";
        }

        String counter = function.getParameters().getStringValue();
        if (counter == null)
        {
            return "";
        }

        LexicalUnit parameter = function.getParameters().getNextLexicalUnit().getNextLexicalUnit();
        if (parameter == null)
        {
            return "";
        }

        String separator = parameter.getStringValue();
        if (separator == null)
        {
            return "";
        }

        String listStyle = getCountersListStyle(parameter);

        return !"none".equalsIgnoreCase(listStyle)
                && !"inherit".equalsIgnoreCase(listStyle)
                        ? getCountersString(counter.toLowerCase(), separator, listStyle) 
                        : "";
    }

    private static String evaluateQuote(LexicalUnit quote, Element element, int[] quoteDepth)
    {
        if (quote.getStringValue().equals("open-quote"))
        {
            String s = selectOpenQuote(element, quoteDepth[0]);
            ++quoteDepth[0];
            return s;
        }

        if (quote.getStringValue().equals("close-quote"))
        {
            --quoteDepth[0];
            String s = selectCloseQuote(element, quoteDepth[0]);
            return s;
        }

        switch (quote.getStringValue())
        {
            case "no-open-quote":
                ++quoteDepth[0];
                break;
            case "no-close-quote":
                --quoteDepth[0];
                break;
        }

        return "";
    }

    private String evaluateStringFunction(LexicalUnit function) throws SAXException
    {
        if (function.getParameters() == null)
        {
            return "";
        }

        String name = function.getParameters().getStringValue().toLowerCase();
        String value = (String) findNamedStringScope(name).get(name);

        return value != null ? value : "";
    }

    private Map findCounterScope(String counter)
    {
        return findScope(counters, counter, 0);
    }

    private Map findNamedStringScope(String namedString)
    {
        return findScope(namedStrings, namedString, "");
    }

    private static Map findScope(Stack scopes, String item, Object defaultValue)
    {
        for (int i = scopes.size() - 1; i >= 0; --i)
        {
            if (((Map) scopes.get(i)).containsKey(item))
            {
                return (Map) scopes.get(i);
            }
        }

        // The highest scope is the default scope.
        ((Map) scopes.get(0)).put(item, defaultValue);

        return (Map) scopes.get(0);
    }

    public URL getBaseUrl()
    {
        return baseUrl;
    }

    private static String getCounterListStyle(LexicalUnit function)
    {
        return function.getParameters().getNextLexicalUnit() != null
                && function.getParameters().getNextLexicalUnit().getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA
                        ? function.getParameters().getNextLexicalUnit().getNextLexicalUnit().getStringValue()
                        : "decimal";
    }

    private static String getCountersListStyle(LexicalUnit function)
    {
        return function.getNextLexicalUnit() != null
                && function.getNextLexicalUnit().getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA
                        ? function.getNextLexicalUnit().getNextLexicalUnit().getStringValue()
                        : "decimal";
    }

    private static String getCounterString(int value, String listStyle)
    {
        if (listStyle.equals("circle"))
        {
            return "\u25cb";
        }

        if (listStyle.equals("disc"))
        {
            return "\u2022";
        }

        if (listStyle.equals("square"))
        {
            return "\u25a0";
        }

        if (listStyle.equals("decimal-leading-zero"))
        {
            return (value < 10 ? "0" : "") + String.valueOf(value);
        }

        if (listStyle.equals("lower-alpha") || listStyle.equals("lower-latin"))
        {
            return String.valueOf((char) (value + 96));
        }

        if (listStyle.equals("upper-alpha") || listStyle.equals("upper-latin"))
        {
            return String.valueOf((char) (value + 64));
        }

        if (listStyle.equals("lower-greek"))
        {
            return String.valueOf((char) (value + 944));
        }

        if (listStyle.equals("lower-roman"))
        {
            return Util.toRoman(value).toLowerCase();
        }

        if (listStyle.equals("upper-roman"))
        {
            return Util.toRoman(value);
        }

        if (listStyle.equals("footnote"))
        {
            return Util.toFootnote(value);
        }

        return String.valueOf(value); // decimal
    }

    private String getCountersString(String counter, String separator, String listStyle)
    {
        String result = "";

        for (Iterator i = counters.iterator(); i.hasNext();)
        {
            Map scope = (Map) i.next();
            Integer value = (Integer) scope.get(counter);

            if (value != null)
            {
                result += (result.equals("") ? "" : separator) + getCounterString(value, listStyle);
            }
        }

        return result;
    }

    private static String getElementContents(Node node)
    {
        return node == null
                ? ""
                : ((node instanceof Text
                        ? ((Text) node).getData()
                        : (node instanceof org.w3c.dom.Element
                        && (!Constants.CSS.equals(node.getNamespaceURI())
                        || !(BEFORE.equals(node.getLocalName())
                        || AFTER.equals(node.getLocalName()))) 
                                ? getElementContents(node.getFirstChild()) 
                                : "")) + getElementContents(node.getNextSibling()));
    }

    private static LexicalUnit getQuotePair(LexicalUnit unit, int quoteDepth)
    {
        for (int i = 0; i < quoteDepth && unit.getNextLexicalUnit().getNextLexicalUnit() != null; ++i)
        {
            unit = unit.getNextLexicalUnit().getNextLexicalUnit();
        }

        return unit;
    }

    private static String[] getSetNamedStringNames(Iterable<Rule> rules)
    {
        Set result = new HashSet();

        for (Rule rule : rules)
        {
            Property property = rule.getProperty();

            if ("string-set".equals(property.getName())
                    && property.getLexicalUnit() != null
                    && property.getLexicalUnit().getLexicalUnitType() == LexicalUnit.SAC_IDENT
                    && !"none".equalsIgnoreCase(property.getLexicalUnit().getStringValue()))
            {
                if (!hasContentsIdentifier // Those are serialized differently.
                        (
                                property.getLexicalUnit().getNextLexicalUnit()
                        ))
                {
                    result.add(property.getLexicalUnit().getStringValue().toLowerCase());
                } 
                else
                {
                    // In case there is a rule earlier in the cascade that doesn't
                    // have "contents" as the value for the named string.

                    result.remove(property.getLexicalUnit().getStringValue().toLowerCase());
                }
            }
        }

        return (String[]) result.toArray(new String[result.size()]);
    }

    public URL getUserAgentStyleSheet()
    {
        return userAgentStyleSheet;
    }

    @SuppressWarnings("StringEquality")
    private void handleControlInformation(String namespaceURI, String localName, AttributesImpl atts) throws SAXException
    {
        try
        {
            if (atts.getValue("xml:base") != null)
            {
                ((Element) elements.peek()).baseUrl
                        = new URL(atts.getValue("xml:base"));
            }

            setXMLIDType(atts);

            if (Constants.XHTML == namespaceURI)
            {
                if (BASE == localName)
                {
                    if (atts.getValue("href") != null)
                    {
                        URL base = new URL(atts.getValue("href"));

                        for (int i = 0; i < elements.size(); ++i)
                        {
                            ((Element) elements.get(i)).baseUrl = base;
                        }
                    }
                } 
                else if (BODY == localName)
                {
                    URL base = ((Element) elements.peek()).baseUrl;

                    if (base != null)
                    // Make sure the BASE is translated for the rest of the chain.
                    {
                        Util.setAttribute(atts, "", "xml:base", "xml:base", base.toString());
                    }
                } 
                else if (LINK == localName)
                {
                    if (isMatchingStyleSheet(atts) && atts.getValue("href") != null)
                    {
                        URL cssUrl = baseUrl == null
                                ? new URL(atts.getValue("href"))
                                : new URL(baseUrl, atts.getValue("href"));
                        addStyleSheet(cssResolver.getRuleSet(cssUrl), 0, true);
                    }
                } 
                else if (STYLE == localName && isMatchingStyleSheet(atts))
                {
                    collectStyleSheet = true;
                }
            }
        } 
        catch (MalformedURLException | CSSException | SAXException e)
        {
            throw new SAXException(e);
        }
    }

    private void handleFloats(Element element) throws SAXException
    {
        int floating = element.appliedAttributes.getIndex(Constants.CSS, "float");

        if (floating == -1)
        {
            return;
        }

        if (!"none".equals(element.appliedAttributes.getValue(floating)))
        {
            AttributesImpl floatAttributes = new AttributesImpl();

            floatAttributes.addAttribute(Constants.CSS, "float", "css:float", "CDATA", element.appliedAttributes.getValue(floating));

            super.startElement(Constants.CSS, "float", "css:float", floatAttributes);
            element.floating = true;
        }

        element.appliedAttributes.removeAttribute(floating);
    }

    private void handleGraphics(Element element) throws SAXException
    {
        if ("graphic".equals(element.appliedAttributes.getValue(Constants.CSS, "display")))
        {
            String url = Util.getIndirectValue(element.appliedAttributes, "src");

            if (url != null)
            {
                try
                {
                    element.appliedAttributes.setValue(
                            element.appliedAttributes.getIndex(Constants.CSS, "src"),
                            element.baseUrl != null
                                    ? new URL(element.baseUrl, url).toString() 
                                    : url
                    );
                } 
                catch (MalformedURLException e)
                {
                    throw new SAXException(e);
                }
            }
        }
    }

    private static boolean hasContentsIdentifier(LexicalUnit unit)
    {
        return unit == null
                ? false
                : (unit.getLexicalUnitType() == LexicalUnit.SAC_IDENT && "contents".equalsIgnoreCase(unit.getStringValue()) 
                        ? true
                        : hasContentsIdentifier(unit.getNextLexicalUnit()));
    }

    private void incrementCounter(Property counterIncrement, boolean display)
    {
        for (LexicalUnit i = counterIncrement.getLexicalUnit(); i != null; i = i.getNextLexicalUnit())
        {
            if (i.getLexicalUnitType() == LexicalUnit.SAC_IDENT)
            {
                String counter = i.getStringValue().toLowerCase();

                if (display || "page".equals(counter))
                {
                    Map scope = findCounterScope(i.getStringValue());

                    scope.put(counter, ((Integer) scope.get(i.getStringValue())) +   
                            (i.getNextLexicalUnit() != null && i.getNextLexicalUnit().getLexicalUnitType() == LexicalUnit.SAC_INTEGER
                                    ? i.getNextLexicalUnit().getIntegerValue()
                                    : 1));
                }
            }
        }
    }

    /**
     * The installed accumulator catches region elements processed by this
     * filter and saves them.
     */
    private void installRegionAccumulator() throws SAXException
    {
        Accumulator.postAccumulate(this, new Accumulator.ProcessElement()
        {
            @Override
            public void process(org.w3c.dom.Element element, XMLFilter filter)
            {
                String pageName = element.getAttributeNS(Constants.CSS, "page");
                if (pageName.equals("") || pageName.equals("auto"))
                {
                    pageName = "unnamed";
                }
                
                element.setAttributeNS(Constants.CSS, "css:page", pageName);
                
                Map<String, org.w3c.dom.Element> regionsForPage = context.regions.get(pageName);
                if (regionsForPage == null)
                {
                    regionsForPage = new HashMap();
                    context.regions.put(pageName, regionsForPage);
                }
                
                regionsForPage.put(element.getAttributeNS(Constants.CSS, "region"), element);
            }
        });
    }

    private void installStringSetAccumulator(final String name, final String value, final Map scope) throws SAXException
    {
        Accumulator.postAccumulate(this, new Accumulator.ProcessElement()
        {
            @Override
            public void process(org.w3c.dom.Element element, XMLFilter filter) throws SAXException
            {
                String result = MessageFormat.format(
                        value,
                        new Object[] { getElementContents(element.getFirstChild()) });
                
                scope.put(name, result);
                addFOMarker(element, name, result);
                
                DOMToContentHandler.elementToContentHandler(element, filter.getContentHandler());
            }
        });
    }

    private static boolean isMatchingStyleSheet(Attributes atts)
    {
        if (!"text/css".equals(atts.getValue("type"))
                || (atts.getValue("rel") != null && !"stylesheet".equalsIgnoreCase(atts.getValue("rel").trim())))
        {
            return false;
        }

        if (atts.getValue("media") == null)
        {
            return true;
        }

        StringTokenizer tokenizer
                = new StringTokenizer(atts.getValue("media"), ",");

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken().trim().toLowerCase();

            if ("all".equals(token) || "print".equals(token))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isStaticRegion(Attributes atts)
    {
        String region = atts.getValue(Constants.CSS, "region");

        return region != null 
                && !"body".equalsIgnoreCase(region)
                && !"none".equalsIgnoreCase(region);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        if ("xml-stylesheet".equalsIgnoreCase(target))
        {
            StringTokenizer tokenizer = new StringTokenizer(data, " ");
            String type = null;
            String uri = null;

            while (tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken();

                if (token.startsWith("type="))
                {
                    type = token.substring(token.indexOf('=') + 1);
                } 
                else if (token.startsWith("href="))
                {
                    uri = token.substring(token.indexOf('=') + 1);
                }
            }

            if (uri != null
                    && uri.length() > 2
                    && type != null
                    && type.length() > 2
                    && type.substring(1, type.length() - 1).equals("text/css"))
            {
                try
                {
                    String cssUri = uri.substring(1, uri.length() - 1);
                    URL cssUrl = baseUrl == null
                            ? new URL(cssUri)
                            : new URL(baseUrl, cssUri);
                    addStyleSheet(cssResolver.getRuleSet(cssUrl), 0, true);
                } 
                catch (MalformedURLException | CSSException | SAXException e)
                {
                    throw new SAXException(e);
                }
            }
        }

        super.processingInstruction(target, data);
    }

    private void repositionMatcher() throws SAXException
    {
        matcher.startDocument();

        // The "/" element is of no concern.
        for (int i = 1; i < elements.size() - 1; ++i)
        {
            Element element = (Element) elements.get(i);
            matcher.startElement(element.namespaceURI, element.localName, element.qName, element.attributes);
        }
    }

    private void reset()
    {
        context.clear();
        matcher = null;
        collectStyleSheet = false;
        embeddedStyleSheet = "";
        elements.clear();
        counters.clear();
        namedStrings.clear();
    }

    private void resetCounter(Property counterReset, boolean display)
    {
        for (LexicalUnit i = counterReset.getLexicalUnit();
                i != null;
                i = i.getNextLexicalUnit())
        {
            if (i.getLexicalUnitType() == LexicalUnit.SAC_IDENT)
            {
                String counter = i.getStringValue().toLowerCase();

                if (display || "page".equals(counter))
                {
                    ((Map) counters.peek()).put(counter, i.getNextLexicalUnit() != null
                            && i.getNextLexicalUnit().getLexicalUnitType() == LexicalUnit.SAC_INTEGER
                            ? i.getNextLexicalUnit().getIntegerValue() :
                            0);
                }
            }
        }
    }

    private static String
            selectCloseQuote(Element element, int quoteDepth)
    {
        if (element.quotes == null)
        {
            return DEFAULT_CLOSE_QUOTE;
        }

        return getQuotePair(element.quotes, quoteDepth).getNextLexicalUnit().getStringValue();
    }

    private static String selectOpenQuote(Element element, int quoteDepth)
    {
        if (element.quotes == null)
        {
            return DEFAULT_OPEN_QUOTE;
        }

        return getQuotePair(element.quotes, quoteDepth).getStringValue();
    }

    /**
     * Maintains the order.
     */
    @SuppressWarnings("StringEquality")
    private static List<Rule> selectPseudoRules(Iterable<Rule> rules, String pseudoElementName)
    {
        List<Rule> result = new ArrayList<>();

        for (Rule rule : rules)
        {
            if (pseudoElementName == rule.getPseudoElementName())
            {
                result.add(rule);
            }
        }

        return result;
    }

    private void serializeAttrFunction(LexicalUnit unit, Element element, Map prefixMap) throws SAXException
    {
        String value = evaluateAttrFunction(unit, element.attributes, prefixMap);
        super.characters(value.toCharArray(), 0, value.length());
    }

    private void serializeChangeBarBegin(Attributes attributes) throws SAXException
    {
        if (attributes.getIndex("change-bar-class") != -1)
        {
            super.startElement(Constants.CSS, "change-bar-begin", "css:change-bar-begin", attributes);
            super.endElement(Constants.CSS, "change-bar-begin", "css:change-bar-begin");
        }
    }

    private void serializeChangeBarEnd(Attributes attributes) throws SAXException
    {
        int index = attributes.getIndex("change-bar-class");

        if (index != -1)
        {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute(
                    attributes.getURI(index), 
                    attributes.getLocalName(index), 
                    attributes.getQName(index), 
                    attributes.getType(index), 
                    attributes.getValue(index));

            super.startElement(Constants.CSS, "change-bar-end", "css:change-bar-end", atts);
            super.endElement(Constants.CSS, "change-bar-end", "css:change-bar-end");
        }
    }

    private void serializeCounterFunction(LexicalUnit unit) throws SAXException
    {
        if (unit.getParameters() == null) return;

        String counter = unit.getParameters().getStringValue().toLowerCase();
        if (counter == null) return;
        
        switch (counter)
        {
            // Special synthetic counter.
            case "page":
                serializePageNumber(getCounterListStyle(unit));
                break;
             // Special synthetic counter.
            case "pages":
                serializePagesTotal();
                break;
            default:
                String value = evaluateCounterFunction(unit);
                super.characters(value.toCharArray(), 0, value.length());
                break;
        }
    }

    private void serializeCountersFunction(LexicalUnit unit) throws SAXException
    {
        String value = evaluateCountersFunction(unit);
        super.characters(value.toCharArray(), 0, value.length());
    }

    private void serializeFOMarkers(Iterable<Rule> rules) throws SAXException
    {
        String[] names = getSetNamedStringNames(rules);

        for (int i = 0; i < names.length; ++i)
        {
            String value = (String) findNamedStringScope(names[i]).get(names[i]);
            if (value != null)
            {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute(Constants.CSS, "name", "css:name", "CDATA", names[i]);

                super.startElement(Constants.CSS, "fo-marker", "css:fo-marker", atts);
                super.characters(value.toCharArray(), 0, value.length());
                super.endElement(Constants.CSS, "fo-marker", "css:fo-marker");
            }
        }
    }

    private void serializeFunction(LexicalUnit unit, Element element, Map prefixMap) throws SAXException
    {
        if ("string".equalsIgnoreCase(unit.getFunctionName()))
        {
            serializeStringFunction(unit);
        } 
        else if ("page-ref".equalsIgnoreCase(unit.getFunctionName()))
        {
            serializePageRefFunction(unit, element, prefixMap);
        }
    }

    private void serializeQuote(LexicalUnit unit, Element element) throws SAXException
    {
        int[] quoteDepthReference = new int[]
        {
            quoteDepth
        };
        String value = evaluateQuote(unit, element, quoteDepthReference);

        quoteDepth = quoteDepthReference[0];
        super.characters(value.toCharArray(), 0, value.length());
    }

    private void serializePageNumber(String listStyle) throws SAXException
    {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(Constants.CSS, "format", "css:format", "CDATA", convertPageFormat(listStyle));

        if (listStyle.equals("armenian")
                || listStyle.equals("georgian")
                || listStyle.equals("hebrew"))
        {
            attributes.addAttribute(Constants.CSS, "letter-value", "css:letter-value", "CDATA", "traditional");
        }

        super.startElement(Constants.CSS, "page-number", "css:page-number", attributes);
        super.endElement(Constants.CSS, "page-number", "css:page-number");
    }

    private void serializePageRefFunction(LexicalUnit unit, Element element, Map prefixMap) throws SAXException
    {
        if (unit.getParameters() == null)
        {
            return;
        }

        String value = unit.getParameters().getLexicalUnitType() == LexicalUnit.SAC_ATTR
                ? evaluateAttrFunction(unit.getParameters(), element.attributes, prefixMap) 
                : element.attributes.getValue(unit.getParameters().getStringValue());
        if (value == null)
        {
            return;
        }

        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(Constants.CSS, "ref-id", "css:ref-id", "CDATA", value);

        super.startElement(Constants.CSS, "page-ref", "css:page-ref", attributes);
        super.endElement(Constants.CSS, "page-ref", "css:page-ref");
    }

    private void serializePagesTotal() throws SAXException
    {
        super.startElement(Constants.CSS, "pages-total", "css:pages-total", new AttributesImpl());
        super.endElement(Constants.CSS, "pages-total", "css:pages-total");
    }

    private void serializeString(String s) throws SAXException
    {
        int position = 0;

        for (int i = s.indexOf('\n'); i != -1; i = s.indexOf(position, '\n'))
        {
            super.characters(s.substring(position, i).toCharArray(), 0, i - position);
            super.startElement(Constants.CSS, "newline", "css:newline", new AttributesImpl());
            super.endElement(Constants.CSS, "newline", "css:newline");
            position = i + 1;
        }

        if (position < s.length())
        {
            super.characters(s.substring(position).toCharArray(), 0, s.length() - position);
        }
    }

    private void serializeStringFunction(LexicalUnit unit) throws SAXException
    {
        if (unit.getParameters() == null)
        {
            return;
        }

        AttributesImpl atts = new AttributesImpl();
        String name = unit.getParameters().getStringValue();

        if (name == null)
        {
            return;
        }

        atts.addAttribute(Constants.CSS, "name", "css:name", "CDATA", name);

        super.startElement(
                Constants.CSS,
                "retrieve-fo-marker",
                "css:retrieve-fo-marker",
                atts
        );

        super.endElement(Constants.CSS, "retrieve-fo-marker", "css:retrieve-fo-marker");
    }

    private void serializeUriFunction(LexicalUnit unit) throws SAXException
    {
        if (unit.getStringValue() == null)
        {
            return;
        }

        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute(
                Constants.CSS,
                "href",
                "css:href",
                "CDATA",
                "url(" + unit.getStringValue() + ")"
        );

        super.startElement(Constants.CSS, "external", "css:external", atts);
        super.endElement(Constants.CSS, "external", "css:external");
    }

    public void
            setBaseUrl(URL baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * This method produces CSS attributes according to the matching rules. It
     * also has a side effect in that it adjusts the counters and named strings.
     * This was done in order to scan the matching rules only once.
     */
    private AttributesImpl setCSSAttributes(Iterable<Rule> matchingRules, Attributes attributes) throws SAXException
    {
        Property counterIncrement = null;
        Property counterReset = null;
        boolean displayNone = false;
        AttributesImpl result = new AttributesImpl(attributes);
        Property stringSet = null;

        // From least to most specific.
        for (Rule rule : matchingRules)
        {
            Property property = rule.getProperty();
            String propertyName = property.getName();

            switch (propertyName)
            {
                case "counter-increment":
                    counterIncrement = property;
                    break;
                case "counter-reset":
                    counterReset = property;
                    break;
                case "string-set":
                    if (rule.getPseudoElementName() == null)
                    {
                        stringSet = property;
                    }   break;
                default:
                    Util.setCSSAttribute(result, property, rule.getSpecificity());
                    if (propertyName.equals("display") && "none".equalsIgnoreCase(property.getValue()))
                    {
                        displayNone = true;
                    }   break;
            }
        }

        if (counterReset != null)
        {
            resetCounter(counterReset, !displayNone);
        }

        if (counterIncrement != null)
        {
            incrementCounter(counterIncrement, !displayNone);
        }

        if (stringSet != null && !displayNone)
        {
            setNamedString(stringSet);
        }

        return result;
    }

    private void addStyleSheet(CSSRuleSet cssRuleSet, int offset, boolean resetMatcher) throws SAXException
    {
        context.ruleSet.addRuleSet(cssRuleSet, offset);
        if (resetMatcher)
        {
            matcher = new Matcher(context.ruleSet.getCompiledRules());
            repositionMatcher();
        }
    }
    
    private void setNamedString(Property stringSet) throws SAXException
    {
        if (stringSet.getLexicalUnit() == null
                || stringSet.getLexicalUnit().getLexicalUnitType() != LexicalUnit.SAC_IDENT
                || "none".equalsIgnoreCase(stringSet.getLexicalUnit().getStringValue()))
        {
            return;
        }

        boolean needContents = false;
        String name
                = stringSet.getLexicalUnit().getStringValue().toLowerCase();
        String result = "";
        Map scope = findNamedStringScope(name);

        for (LexicalUnit i = stringSet.getLexicalUnit().getNextLexicalUnit(); i != null; i = i.getNextLexicalUnit())
        {
            switch (i.getLexicalUnitType())
            {
                case LexicalUnit.SAC_ATTR:
                    result += evaluateAttrFunction(i, ((Element) elements.peek()).attributes, stringSet.getPrefixMap());
                    break;

                case LexicalUnit.SAC_COUNTER_FUNCTION:
                    result += evaluateCounterFunction(i);
                    break;

                case LexicalUnit.SAC_COUNTERS_FUNCTION:
                    result += evaluateCountersFunction(i);
                    break;

                case LexicalUnit.SAC_FUNCTION:
                    if ("string".equalsIgnoreCase(i.getFunctionName()))
                    {
                        result += evaluateStringFunction(i);
                    }
                    break;

                case LexicalUnit.SAC_IDENT:
                    if ("contents".equalsIgnoreCase(i.getStringValue()))
                    {
                        result += "{0}";
                        needContents = true;
                    } else
                    {
                        result += evaluateQuote(i, (Element) elements.peek(), new int[1]);
                        // Local evaluation.
                    }
                    break;

                case LexicalUnit.SAC_STRING_VALUE:
                    result += i.getStringValue();
                    break;

                default:
                    break;
            }
        }

        if (!needContents)
        {
            scope.put(name, result);
        } 
        else
        {
            installStringSetAccumulator(name, result, scope);
        }
    }

    private void setQuotes()
    {
        Element element = (Element) elements.peek();

        for (Rule rule : element.matchingElementRules)
        {
            if (rule.getProperty().getName().equals("quotes"))
            {
                element.quotes = rule.getProperty().getLexicalUnit();
            }
        }

        if (element.quotes == null || element.quotes.getLexicalUnitType() == LexicalUnit.SAC_INHERIT)
        {
            element.quotes = ((Element) elements.get(elements.size() - 2)).quotes;
        }
    }

    public void setUserAgentStyleSheet(URL userAgentStyleSheet)
    {
        this.userAgentStyleSheet = userAgentStyleSheet;
    }

    private static void setXMLIDType(AttributesImpl atts)
    {
        int index = atts.getIndex("xml:id");

        if (index != -1 && !"ID".equals(atts.getType(index)))
        {
            atts.setType(index, "ID");
        }
    }

    /**
     * Removes the change-bar attributes from <code>attributes</code> and
     * returns those that are in the CSS namespace. They are returned without a
     * namespace however.
     */
    private static AttributesImpl splitChangeBarAttributes(AttributesImpl attributes)
    {
        AttributesImpl result = new AttributesImpl();

        for (int i = 0; i < attributes.getLength(); ++i)
        {
            if (attributes.getLocalName(i).startsWith("change-bar-"))
            {
                if (Constants.CSS.equals(attributes.getURI(i)))
                {
                    result.addAttribute("", attributes.getLocalName(i), attributes.getLocalName(i), attributes.getType(i), attributes.getValue(i));
                }

                attributes.removeAttribute(i--);
            }
        }

        return result;
    }

    @Override
    public void startDocument() throws SAXException
    {
        reset();

        try
        {
            addStyleSheet(CSSRuleSet.parse("*{display: inline}"), -2, false);

            String htmlHeaderMark = userAgentParameters.get("html-header-mark");
            if (htmlHeaderMark != null)
            {
                addStyleSheet(CSSRuleSet.parse(htmlHeaderMark + "{string-set: component contents}"), -2, false);
            }

            addStyleSheet(cssResolver.getRuleSet(userAgentStyleSheet), -1, true);
        } 
        catch (CSSException | SAXException e)
        {
            throw new SAXException(e);
        }

        Element root = new Element("", "/", "/");

        root.baseUrl = baseUrl;
        elements.push(root);
        counters.push(new HashMap());
        namedStrings.push(new HashMap());
        super.startDocument();
        startPrefixMapping("css", Constants.CSS);
        startPrefixMapping("sp", Constants.SPECIF);
    }

    /**
     * The string arguments are interned.
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        if (namespaceURI != null)
        {
            namespaceURI = namespaceURI.intern();
        }

        if (localName != null)
        {
            localName = localName.intern();
        }

        if (qName != null)
        {
            qName = qName.intern();
        }

        Element element = new Element(namespaceURI, localName, qName);

        element.baseUrl = ((Element) elements.peek()).baseUrl;
        elements.push(element);
        element.attributes = new AttributesImpl(atts);
        // Must be copied because atts might be recuperated by the parser.
        handleControlInformation(namespaceURI, localName, element.attributes);
        matcher.startElement(namespaceURI, localName, qName, element.attributes);

        element.matchingElementRules = appendStyleAttributeRules(matcher.matchingRules(), atts, namespaceURI);
        element.matchingPseudoRules = matcher.matchingPseudoRules();
        setQuotes();

        element.appliedAttributes = setCSSAttributes(element.matchingElementRules, element.attributes);
        handleFloats(element);
        handleGraphics(element);
        detectMarkers(element);

        if (isStaticRegion(element.appliedAttributes))
        {
            installRegionAccumulator();
        }

        addFirstLetterMarker(element);
        translateId(element.appliedAttributes);
        super.startElement(namespaceURI, localName, qName, element.appliedAttributes);
        serializeFOMarkers(element.matchingElementRules);
        applyPseudoRules(element, FIRST_LETTER);
        applyPseudoRules(element, BEFORE);
        applyPseudoRules(element, FIRST_LINE);
        counters.push(new HashMap());
        namedStrings.push(new HashMap());
    }

    private static void translateId(AttributesImpl atts)
    {
        for (int i = 0; i < atts.getLength(); ++i)
        {
            if ("ID".equals(atts.getType(i)))
            {
                atts.setAttribute(i, Constants.XML, "id", "xml:id", "ID", atts.getValue(i));
            }
        }
    }

    private static class Element
    {
        private AttributesImpl appliedAttributes;
        private AttributesImpl attributes = new AttributesImpl();
        private URL baseUrl;
        private boolean floating = false;
        private String localName;
        private Collection<Rule> matchingElementRules = null;
        private Collection<Rule> matchingPseudoRules = null;
        private String namespaceURI;
        private String qName;
        private LexicalUnit quotes = null;

        private Element(String namespaceURI, String localName, String qName)
        {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
        }
    } // Element

} // ProjectorFilter
