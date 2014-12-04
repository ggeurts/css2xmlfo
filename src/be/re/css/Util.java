package be.re.css;

import be.re.xml.sax.FilterOfFilters;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.ContentCondition;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.NegativeCondition;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.PositionalCondition;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;
import org.xml.sax.Attributes;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * A collection of functions.
 * @author Werner Donn\u00e9
 */

class Util

{

  private static final String[]	FOOTNOTE_NUMBERS =
    new String[]
    {
      "*",
      "\u2020",
      "\u2021",
      "\u00a7",
      "\u007c\u007c",
      "\u00b6",
      "#",
      "**",
      "\u2020\u2020",
      "\u2021\u2021",
      "\u00a7\u00a7"
    };
  private static final Set	INHERITED =
    new HashSet
    (
      Arrays.asList
      (
        new String[]
        {
          "azimuth",
          "border-collapse",
          "border-spacing",
          "caption-size",
          "color",
          "cursor",
          "direction",
          "elevation",
          "empty-cells",
          "font",
          "font-family",
          "font-size",
          "font-stretch",
          "font-style",
          "font-variant",
          "font-weight",
          "hyphenate",
          "leader-alignment",
          "leader-length",
          "leader-pattern",
          "leader-pattern-width",
          "letter-spacing",
          "line-height",
          "list-style",
          "list-style-image",
          "list-style-position",
          "list-style-type",
          "orientation",
          "orphans",
          "page",
          "page-break-inside",
          "pitch",
          "pitch-range",
          "quotes",
          "richness",
          "rule-style",
          "rule-thickness",
          "speak",
          "speak-header",
          "speak-numeral",
          "speak-punctuation",
          "speech-rate",
          "stress",
          "text-align",
          "text-align-last",
          "text-indent",
          "text-transform",
          "voice-family",
          "volume",
          "white-space",
          "widows",
          "word-spacing"
        }
      )
    );

  private static Class		sacParserClass = null;



  static String
  conditionText(Condition condition)
  {
    switch (condition.getConditionType())
    {
      case Condition.SAC_AND_CONDITION:
        return
          "(and: " +
            conditionText
            (
              ((CombinatorCondition) condition).getFirstCondition()
            ) + " " +
            conditionText
            (
              ((CombinatorCondition) condition).getSecondCondition()
            ) + ")";

      case Condition.SAC_ATTRIBUTE_CONDITION:
        return
          "(attribute: (" +
            ((AttributeCondition) condition).getNamespaceURI() + ") (" +
            ((AttributeCondition) condition).getLocalName() + ") (" +
            ((AttributeCondition) condition).getSpecified() + ") (" +
            ((AttributeCondition) condition).getValue() + "))";

      case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
        return
          "(hyphen: (" +
            ((AttributeCondition) condition).getNamespaceURI() + ") (" +
            ((AttributeCondition) condition).getLocalName() + ") (" +
            ((AttributeCondition) condition).getSpecified() + ") (" +
            ((AttributeCondition) condition).getValue() + "))";

      case Condition.SAC_CLASS_CONDITION:
        return
          "(class: (" +
            ((AttributeCondition) condition).getNamespaceURI() + ") (" +
            ((AttributeCondition) condition).getLocalName() + ") (" +
            ((AttributeCondition) condition).getSpecified() + ") (" +
            ((AttributeCondition) condition).getValue() + "))";

      case Condition.SAC_CONTENT_CONDITION:
        return "(content: " + ((ContentCondition) condition).getData() + ")";

      case Condition.SAC_ID_CONDITION:
        return
          "(id: (" +
            ((AttributeCondition) condition).getNamespaceURI() + ") (" +
            ((AttributeCondition) condition).getLocalName() + ") (" +
            ((AttributeCondition) condition).getSpecified() + ") (" +
            ((AttributeCondition) condition).getValue() + "))";

      case Condition.SAC_LANG_CONDITION:
        return "(lang: " + ((LangCondition) condition).getLang() + ")";

      case Condition.SAC_NEGATIVE_CONDITION:
        return
          "(negative: " +
            conditionText(((NegativeCondition) condition).getCondition()) +
            ")";

      case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
        return
          "(one of: (" +
            ((AttributeCondition) condition).getNamespaceURI() + ") (" +
            ((AttributeCondition) condition).getLocalName() + ") (" +
            ((AttributeCondition) condition).getSpecified() + ") (" +
            ((AttributeCondition) condition).getValue() + "))";

      case Condition.SAC_ONLY_CHILD_CONDITION: return "(only child)";

      case Condition.SAC_ONLY_TYPE_CONDITION: return "(only type)";

      case Condition.SAC_OR_CONDITION:
        return
          "(or: " +
            conditionText
            (
              ((CombinatorCondition) condition).getFirstCondition()
            ) + " " +
            conditionText
            (
              ((CombinatorCondition) condition).getSecondCondition()
            ) + ")";

      case Condition.SAC_POSITIONAL_CONDITION:
        return
          "(positional: (" + ((PositionalCondition) condition).getPosition() +
            ") (" + ((PositionalCondition) condition).getType() + ") (" +
            ((PositionalCondition) condition).getTypeNode() + "))";

      case Condition.SAC_PSEUDO_CLASS_CONDITION:
        return
          "(pseudo class: (" +
            ((AttributeCondition) condition).getNamespaceURI() + ") (" +
            ((AttributeCondition) condition).getLocalName() + ") (" +
            ((AttributeCondition) condition).getSpecified() + ") (" +
            ((AttributeCondition) condition).getValue() + "))";

      default: return "(unknown)";
    }
  }



  private static String
  convertFloat(float value)
  {
    return
      new DecimalFormat
      (
        "####0.0####",
        new DecimalFormatSymbols(Locale.ENGLISH)
      ).format(value);
  }



  static void
  copyAttribute
  (
    Attributes		from,
    AttributesImpl	to,
    String		namespaceURI,
    String		localName
  )
  {
    int	index = from.getIndex(namespaceURI, localName);

    if (index != -1)
    {
      to.addAttribute
      (
        namespaceURI,
        localName,
        from.getQName(index),
        from.getType(index),
        from.getValue(index)
      );
    }
  }



  static PostProjectionFilter
  createPostProjectionFilter
  (
    URL		baseUrl,
    Map		userAgentParameters,
    boolean	debug
  )
  {
    final LinkFilter	linkFilter = new LinkFilter(baseUrl);
    final XMLFilterImpl	filter =
      new FilterOfFilters
      (
        new XMLFilter[]
        {
          new InvalidPropertyFilter(),
          new WrapperFilter(),
          new DisplayNonePropagator(),
          new ForeignFilter(),
          new FirstLetterFilter(),
          userAgentParameters != null &&
            userAgentParameters.get("rule-thickness") != null ?
            new XHTMLAttributeTranslationFilter
            (
              (String) userAgentParameters.get("rule-thickness")
            ) : new XHTMLAttributeTranslationFilter(),
          new NormalizeTableFilter(),
          new CenterFilter(),
          new LengthAdjustFilter(),
          new WidthAndMarginsFilter(),
          new MarkerFilter(),
          linkFilter,
          new FootnoteFilter(),
          new BlockContainerFilter(),
          new ListImageLabelFilter()
        },
        debug
      );

    return
      new PostProjectionFilter()
      {
        public XMLFilterImpl
        getFilter()
        {
          return filter;
        }

        public void
        setBaseUrl(URL url)
        {
          linkFilter.setBaseUrl(url);
        }
      };
  }



  static XMLFilter
  createPreprocessorFilter(URL[] preprocessors, XMLFilter parent)
    throws TransformerConfigurationException
  {
    SAXTransformerFactory	factory =
      be.re.xml.sax.Util.newSAXTransformerFactory();
    XMLFilter			result = parent;

    for (int i = 0; i < preprocessors.length; ++i)
    {
      XMLFilter	transformer =
        factory.newXMLFilter(new StreamSource(preprocessors[i].toString()));

      transformer.setParent(result);
      result = transformer;
    }

    return result;
  }



  static URL
  createUrl(String s) throws MalformedURLException
  {
    return
      be.re.net.Util.isUrl(s) ?
        new URL(s) : be.re.net.Util.fileToUrl(new File(s));
  }



  static URL[]
  createUrls(String s) throws MalformedURLException
  {
    List		result = new ArrayList();
    StringTokenizer	tokenizer = new StringTokenizer(s, ",");

    while (tokenizer.hasMoreTokens())
    {
      result.add(createUrl(tokenizer.nextToken()));
    }

    return (URL[]) result.toArray(new URL[0]);
  }



  static String
  getIndirectType(Attributes attributes, String property)
  {
    String	value = attributes.getValue(Constants.CSS, property);
    int		index = value != null ? value.lastIndexOf('|') : -1;

    return
      value != null ?
        (
          index != -1 ?
            attributes.
              getType(value.substring(0, index), value.substring(index + 1)) :
            attributes.getType(value)
        ) : null;
  }



  static String
  getIndirectValue(Attributes attributes, String property)
  {
    String	value = attributes.getValue(Constants.CSS, property);
    int		index = value != null ? value.lastIndexOf('|') : -1;

    return
      value != null ?
        (
          index != -1 ?
            attributes.
              getValue(value.substring(0, index), value.substring(index + 1)) :
            attributes.getValue(value)
        ) : null;
  }



  static Selector
  getLastSelector(Selector selector)
  {
    Selector[]	chain = Util.getSelectorChain(selector);

    return chain[chain.length - 1];
  }



  static Parser
  getSacParser() throws CSSException
  {
    try
    {
      return (Parser) getSacParserClass().newInstance();
    }

    catch (Exception e)
    {
      if (e instanceof CSSException)
      {
        throw (CSSException) e;
      }

      throw new CSSException(e);
    }
  }



  private static Class
  getSacParserClass() throws Exception
  {
    if (sacParserClass == null)
    {
      String	cls =
        be.re.util.Util.getSystemProperty("org.w3c.css.sac.parser");

      if (cls == null)
      {
        throw new CSSException("No value for org.w3c.css.sac.parser");
      }

      sacParserClass = Class.forName(cls);
    }

    return sacParserClass;
  }



  /**
   * Flattens the selector expression tree in infix order.
   */

  static Selector[]
  getSelectorChain(Selector selector)
  {
    List	result = getSelectorChainList(selector);

    return (Selector[]) result.toArray(new Selector[result.size()]);
  }



  private static List
  getSelectorChainList(Selector selector)
  {
    List	result;

    switch (selector.getSelectorType())
    {
      case Selector.SAC_CHILD_SELECTOR:
      case Selector.SAC_DESCENDANT_SELECTOR:
        result =
          getSelectorChainList
          (
            ((DescendantSelector) selector).getAncestorSelector()
          );

        result.add(selector);

        result.addAll
        (
          getSelectorChainList
          (
            ((DescendantSelector) selector).getSimpleSelector()
          )
        );

        break;

      case Selector.SAC_CONDITIONAL_SELECTOR:
        result = new ArrayList();
        result.add(selector);

        result.addAll
        (
          getSelectorChainList
          (
            ((ConditionalSelector) selector).getSimpleSelector()
          )
        );

        break;

      case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
        result =
          getSelectorChainList(((SiblingSelector) selector).getSelector());
        result.add(selector);

        result.addAll
        (
          getSelectorChainList
          (
            ((SiblingSelector) selector).getSiblingSelector()
          )
        );

        break;

      case Selector.SAC_NEGATIVE_SELECTOR:
        result = new ArrayList();
        result.add(selector);

        result.addAll
        (
          getSelectorChainList
          (
            ((NegativeSelector) selector).getSimpleSelector()
          )
        );

        break;

      default:
        result = new ArrayList();
        result.add(selector);
    }

    return result;
  }



  static boolean
  inArray(String[] array, String object)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if
      (
        (
          array[i].charAt(array[i].length() - 1) == '*'			  &&
          object.startsWith(array[i].substring(0, array[i].length() - 1))
        )								  ||
        array[i].equals(object)
      )
      {
        return true;
      }
    }

    return false;
  }



  static int
  indexOf(Object[] array, Object object)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if (array[i].equals(object))
      {
        return i;
      }
    }

    return -1;
  }



  static boolean
  isInherited(String property)
  {
    return INHERITED.contains(property);
  }



  static boolean
  isWhitespace(char[] ch, int start, int length)
  {
    for (int i = start; i < ch.length && i < start + length; ++i)
    {
      if (!Character.isWhitespace(ch[i]))
      {
        return false;
      }
    }

    return true;
  }



  static boolean
  isZeroLength(String value)
  {
    return
      Util.inArray
      (
        new String[] {"0", "0pt", "0px", "0pc", "0mm", "0cm", "0in", "0em"},
        value
      );
  }



  static LexicalUnit[]
  lexicalUnitArray(LexicalUnit unit)
  {
    List	result = new ArrayList();

    for (LexicalUnit i = unit; i != null; i = i.getNextLexicalUnit())
    {
      result.add(i);
    }

    return (LexicalUnit[]) result.toArray(new LexicalUnit[result.size()]);
  }



  static String
  lexicalUnitAtom(LexicalUnit unit, URL baseUrl)
  {
    return lexicalUnitAtom(unit, false, baseUrl);
  }



  private static String
  lexicalUnitAtom(LexicalUnit unit, boolean identifiersToLower, URL baseUrl)
  {
    switch (unit.getLexicalUnitType())
    {
      case LexicalUnit.SAC_ATTR:
        return "attr(" + unit.getStringValue().toLowerCase() + ")";

      case LexicalUnit.SAC_CENTIMETER:
      case LexicalUnit.SAC_DEGREE:
      case LexicalUnit.SAC_DIMENSION:
      case LexicalUnit.SAC_EM:
      case LexicalUnit.SAC_EX:
      case LexicalUnit.SAC_GRADIAN:
      case LexicalUnit.SAC_HERTZ:
      case LexicalUnit.SAC_INCH:
      case LexicalUnit.SAC_KILOHERTZ:
      case LexicalUnit.SAC_MILLIMETER:
      case LexicalUnit.SAC_MILLISECOND:
      case LexicalUnit.SAC_PERCENTAGE:
      case LexicalUnit.SAC_PICA:
      case LexicalUnit.SAC_PIXEL:
      case LexicalUnit.SAC_POINT:
      case LexicalUnit.SAC_RADIAN:
        return
          (convertFloat(unit.getFloatValue()) + unit.getDimensionUnitText()).
            toLowerCase();

      // Flute 1.3 work-around, should be in previous list.
      case LexicalUnit.SAC_REAL:
        return convertFloat(unit.getFloatValue());

      case LexicalUnit.SAC_COUNTER_FUNCTION:
      case LexicalUnit.SAC_COUNTERS_FUNCTION:
      case LexicalUnit.SAC_FUNCTION:
      case LexicalUnit.SAC_RECT_FUNCTION:
        return
          unit.getFunctionName().toLowerCase() + "(" +
            (
              unit.getParameters() != null ?
                lexicalUnitChain
                (
                  unit.getParameters(),
                  identifiersToLower,
                  baseUrl
                ) : ""
            ) + ")";

      case LexicalUnit.SAC_IDENT:
        return
          identifiersToLower ?
            unit.getStringValue().toLowerCase() : unit.getStringValue();

      case LexicalUnit.SAC_INHERIT: return "inherit";

      case LexicalUnit.SAC_INTEGER:
        return String.valueOf(unit.getIntegerValue());

      case LexicalUnit.SAC_OPERATOR_COMMA: return ",";

      case LexicalUnit.SAC_OPERATOR_EXP: return "^";

      case LexicalUnit.SAC_OPERATOR_GE: return ">=";

      case LexicalUnit.SAC_OPERATOR_GT: return ">";

      case LexicalUnit.SAC_OPERATOR_LE: return "<=";

      case LexicalUnit.SAC_OPERATOR_LT: return "<";

      case LexicalUnit.SAC_OPERATOR_MINUS: return "-";

      case LexicalUnit.SAC_OPERATOR_MOD: return "%";

      case LexicalUnit.SAC_OPERATOR_MULTIPLY: return "*";

      case LexicalUnit.SAC_OPERATOR_PLUS: return "+";

      case LexicalUnit.SAC_OPERATOR_SLASH: return "/";

      case LexicalUnit.SAC_OPERATOR_TILDE: return "~";

      case LexicalUnit.SAC_RGBCOLOR:
        return
          "rgb(" +
            lexicalUnitChain(unit.getParameters(), identifiersToLower, baseUrl)
            + ")";

      case LexicalUnit.SAC_STRING_VALUE: return unit.getStringValue();

      case LexicalUnit.SAC_URI:
        try
        {
          return
            "url(" +
              (
                baseUrl != null ?
                  new URL(baseUrl, unit.getStringValue()).toString() :
                  unit.getStringValue()
              ) + ")";
        }

        catch (MalformedURLException e)
        {
          throw new RuntimeException(e);
        }

      default: return "";
    }
  }



  static String
  lexicalUnitAtomLower(LexicalUnit unit, URL baseUrl)
  {
    return
      unit.getLexicalUnitType() == LexicalUnit.SAC_URI ?
        lexicalUnitAtom(unit, baseUrl) :
        lexicalUnitAtom(unit, baseUrl).toLowerCase();
  }



  static String[]
  lexicalUnitAtoms(LexicalUnit unit, URL baseUrl)
  {
    return lexicalUnitAtoms(unit, false, baseUrl);
  }



  private static String[]
  lexicalUnitAtoms(LexicalUnit unit, boolean lower, URL baseUrl)
  {
    LexicalUnit[]	values = lexicalUnitArray(unit);
    String[]		result = new String[values.length];

    for (int i = 0; i < values.length; ++i)
    {
      result[i] =
        lower ?
          lexicalUnitAtomLower(values[i], baseUrl) :
          lexicalUnitAtom(values[i], baseUrl);
    }

    return result;
  }



  static String[]
  lexicalUnitAtomsLower(LexicalUnit unit, URL baseUrl)
  {
    return lexicalUnitAtoms(unit, true, baseUrl);
  }



  private static String
  lexicalUnitChain(LexicalUnit unit, boolean identifiersToLower, URL baseUrl)
  {
    return
      lexicalUnitAtom(unit, identifiersToLower, baseUrl) +
	(
	  unit.getNextLexicalUnit() != null ?
	    (
              " " +
                lexicalUnitChain
                (
                  unit.getNextLexicalUnit(),
                  identifiersToLower,
                  baseUrl
                )
            ) : ""
	);
  }



  static String
  lexicalUnitToString(LexicalUnit unit, boolean identifiersToLower, URL baseUrl)
  {
    return lexicalUnitChain(unit, identifiersToLower, baseUrl);
  }



  /**
   * Adds <code>from</code> attributes to <code>into</code> giving precedence
   * to the latter.
   */

  static Attributes
  mergeAttributes(Attributes from, Attributes into)
  {
    return mergeAttributes(from, into, new String[0], false);
  }



  /**
   * Adds <code>from</code> attributes to <code>into</code> giving precedence
   * to the latter. If <code>include</code> is <code>true</code>, the attribute
   * in <code>from</code> must be in <code>subset</code> in order for it to be
   * included. If <code>include</code> is <code>false</code>, the attribute
   * in <code>from</code> must not be in <code>subset</code> in order for it to
   * be included.
   */

  static Attributes
  mergeAttributes
  (
    Attributes	from,
    Attributes	into,
    String[]	subset,
    boolean	include
  )
  {
    AttributesImpl	result = new AttributesImpl(into);

    for (int i = 0; i < from.getLength(); ++i)
    {
      if
      (
        into.getIndex(from.getURI(i), from.getLocalName(i)) == -1	&&
        (
          (
            !include							&&
            !inArray(subset, from.getLocalName(i))
          )								||
          (
            include							&&
            inArray(subset, from.getLocalName(i))
          )
        )
      )
      {
        result.addAttribute
        (
          from.getURI(i),
          from.getLocalName(i),
          from.getQName(i),
          from.getType(i),
          from.getValue(i)
        );
      }
    }

    return result;
  }



  static void
  printUserAgentParameters(PrintStream out)
  {
    System.err.println("User Agent parameters:");
    System.err.println("  column-count (default: 1)");
    System.err.println("  country (default: GB)");
    System.err.
      println("  font-size (default: 10pt for a5 and b5, otherwise 11pt)");
    System.err.println("  html-header-mark: an HTML element (default: none)");
    System.err.println("  language (default: en)");
    System.err.println("  odd-even-shift (default: 10mm)");
    System.err.
      println("  orientation (default: portrait; other: landscape)");
    System.err.println("  paper-margin-bottom (default: 0mm)");
    System.err.println("  paper-margin-left (default: 25mm)");
    System.err.println("  paper-margin-right (default: 25mm)");
    System.err.println("  paper-margin-top (default: 10mm)");
    System.err.println("  paper-mode (default: onesided; other: twosided)");

    System.err.println
    (
      "  paper-size (default: a4; others: a0, a1, a2, a3, a5, b5, " +
        "executive, letter and legal)"
    );

    System.err.println("  rule-thickness (default: 0.2pt)");
    System.err.println("  writing-mode (default: lr-tb)");
  }



  private static String
  processFontFamily(String value)
  {
    value = value.trim();

    if
    (
      value.indexOf(' ') == -1				||
      (
        value.charAt(0) == '\''				&&
        value.charAt(value.length() - 1) == '\''
      )							||
      (
        value.charAt(0) == '"'				&&
        value.charAt(value.length() - 1) == '"'
      )
    )
    {
      return value;
    }

    String		result = "";
    StringTokenizer	tokenizer = new StringTokenizer(value, ",");

    while (tokenizer.hasMoreTokens())
    {
      String	token = tokenizer.nextToken().trim();

      result +=
        (result.equals("") ? "" : ", ") +
          (token.indexOf(' ') != -1 ? ("'" + token + "'") : token);
    }

    return result;
  }



  static void
  removeAttribute(AttributesImpl atts, String namespaceURI, String localName)
  {
    int	index = atts.getIndex(namespaceURI, localName);

    if (index != -1)
    {
      atts.removeAttribute(index);
    }
  }



  static void
  setAttribute
  (
    AttributesImpl	attributes,
    String		namespaceURI,
    String		localName,
    String		qName,
    String		value
  )
  {
    int	index = attributes.getIndex(namespaceURI, localName);

    if (index == -1)
    {
      attributes.addAttribute(namespaceURI, localName, qName, "CDATA", value);
    }
    else
    {
      attributes.setAttribute
      (
        index,
        namespaceURI,
        localName,
        qName,
        "CDATA",
        value
      );
    }
  }



  /**
   * If the value of a property is a call to the "attr" function and if the
   * property is not "content", the call is replaced by the expanded attribute
   * name in which the URI is separated from the local name by a |.
   */

  static void
  setCSSAttribute(AttributesImpl attributes, Property property, int specificity)
  {
    String	propertyName = property.getName();
    String	value;

    if
    (
      !"content".equals(propertyName)			&&
      property.getLexicalUnit() != null			&&
      property.getLexicalUnit().getLexicalUnitType() ==
        LexicalUnit.SAC_ATTR
    )
    {
      value = property.getLexicalUnit().getStringValue();

      int	index = value.lastIndexOf('|');

      if (index != -1)
      {
        value =
          (String) property.getPrefixMap().get(value.substring(0, index)) +
            "|" + value.substring(index + 1);
      }
    }
    else
    {
      value = property.getValue();
    }

    Util.setAttribute
    (
      attributes,
      Constants.CSS,
      propertyName,
      "css:" + propertyName,
      "font-family".equals(propertyName) ? processFontFamily(value) : value
    );

    // XHTML attributes are translated to CSS properties further down the
    // filter chain. They get a specificity of 0 and a position before the
    // other rules in the style sheet. Therefore, they can only overwrite
    // property values selected by the universal selector or comming from the
    // UA style sheet.

    if (specificity <= 0) // Universal selector is 0, UA rules are < 0.
    {
      // Marked as eligible for replacement.

      Util.setAttribute
      (
        attributes,
        Constants.SPECIF,
        propertyName,
        "sp:" + propertyName,
        "1"
      );
    }
  }



  static String
  toFootnote(int v)
  {
    return v > FOOTNOTE_NUMBERS.length ? "*" : FOOTNOTE_NUMBERS[v - 1];
  }



  static String
  toRoman(int v)
  {
    return
      v < 1 ?
        "" :
        (
          v < 4 ?
            ("I" + toRoman(v - 1)) :
            (
              v < 5 ?
                "IV" :
                 (
                   v < 9 ?
                     ("V" + toRoman(v - 5)) :
                     (
                       v < 10 ?
                         "IX" :
                         (
                           v < 40 ?
                             ("X" + toRoman(v - 10)) :
                             (
                               v < 50 ?
                                 ("XL" + toRoman(v - 40)) :
                                 (
                                   v < 90 ?
                                     ("L" + toRoman(v - 50)) :
                                     (
                                       v < 100 ?
                                         ("XC" + toRoman(v - 90)) :
                                         (
                                           v < 400 ?
                                             ("C" + toRoman(v - 100)) :
                                             (
                                               v < 500 ?
                                                 ("CD" + toRoman(v - 400)) :
                                                 (
                                                   v < 900 ?
                                                     (
                                                       "D" +
                                                         toRoman(v - 500)
                                                     ) :
                                                     (
                                                       v < 1000 ?
                                                         (
                                                           "CM" +
                                                             toRoman(v - 900)
                                                         ) :
                                                         (
                                                           "M" +
                                                             toRoman(v - 1000)
                                                         )
                                                     )
                                                 )
                                             )
                                         )
                                     )
                                 )
                             )
                         )
                     )
                 )
            )
        );
  }



  interface PostProjectionFilter

  {

    public XMLFilterImpl	getFilter	();
    public void			setBaseUrl	(URL baseUrl);

  } // BaseUrl

} // Util
