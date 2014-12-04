package be.re.css;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CharacterDataSelector;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.ProcessingInstructionSelector;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.helpers.ParserFactory;



public class TestSAC

{

  private static final Tuple[]	tuples =
    {
      new Tuple(LexicalUnit.SAC_OPERATOR_COMMA, "SAC_OPERATOR_COMMA"),
      new Tuple(LexicalUnit.SAC_OPERATOR_PLUS, "SAC_OPERATOR_PLUS"),
      new Tuple(LexicalUnit.SAC_OPERATOR_MINUS, "SAC_OPERATOR_MINUS"),
      new Tuple(LexicalUnit.SAC_OPERATOR_MULTIPLY, "SAC_OPERATOR_MULTIPLY"),
      new Tuple(LexicalUnit.SAC_OPERATOR_SLASH, "SAC_OPERATOR_SLASH"),
      new Tuple(LexicalUnit.SAC_OPERATOR_MOD, "SAC_OPERATOR_MOD"),
      new Tuple(LexicalUnit.SAC_OPERATOR_EXP, "SAC_OPERATOR_EXP"),
      new Tuple(LexicalUnit.SAC_OPERATOR_LT, "SAC_OPERATOR_LT"),
      new Tuple(LexicalUnit.SAC_OPERATOR_GT, "SAC_OPERATOR_GT"),
      new Tuple(LexicalUnit.SAC_OPERATOR_LE, "SAC_OPERATOR_LE"),
      new Tuple(LexicalUnit.SAC_OPERATOR_GE, "SAC_OPERATOR_GE"),
      new Tuple(LexicalUnit.SAC_OPERATOR_TILDE, "SAC_OPERATOR_TILDE"),
      new Tuple(LexicalUnit.SAC_INHERIT, "SAC_INHERIT"),
      new Tuple(LexicalUnit.SAC_INTEGER, "SAC_INTEGER"),
      new Tuple(LexicalUnit.SAC_REAL, "SAC_REAL"),
      new Tuple(LexicalUnit.SAC_EM, "SAC_EM"),
      new Tuple(LexicalUnit.SAC_EX, "SAC_EX"),
      new Tuple(LexicalUnit.SAC_PIXEL, "SAC_PIXEL"),
      new Tuple(LexicalUnit.SAC_INCH, "SAC_INCH"),
      new Tuple(LexicalUnit.SAC_CENTIMETER, "SAC_CENTIMETER"),
      new Tuple(LexicalUnit.SAC_MILLIMETER, "SAC_MILLIMETER"),
      new Tuple(LexicalUnit.SAC_POINT, "SAC_POINT"),
      new Tuple(LexicalUnit.SAC_PICA, "SAC_PICA"),
      new Tuple(LexicalUnit.SAC_PERCENTAGE, "SAC_PERCENTAGE"),
      new Tuple(LexicalUnit.SAC_URI, "SAC_URI"),
      new Tuple(LexicalUnit.SAC_COUNTER_FUNCTION, "SAC_COUNTER_FUNCTION"),
      new Tuple(LexicalUnit.SAC_COUNTERS_FUNCTION, "SAC_COUNTERS_FUNCTION"),
      new Tuple(LexicalUnit.SAC_RGBCOLOR, "SAC_RGBCOLOR"),
      new Tuple(LexicalUnit.SAC_DEGREE, "SAC_DEGREE"),
      new Tuple(LexicalUnit.SAC_GRADIAN, "SAC_GRADIAN"),
      new Tuple(LexicalUnit.SAC_RADIAN, "SAC_RADIAN"),
      new Tuple(LexicalUnit.SAC_MILLISECOND, "SAC_MILLISECOND"),
      new Tuple(LexicalUnit.SAC_SECOND, "SAC_SECOND"),
      new Tuple(LexicalUnit.SAC_HERTZ, "SAC_HERTZ"),
      new Tuple(LexicalUnit.SAC_KILOHERTZ, "SAC_KILOHERTZ"),
      new Tuple(LexicalUnit.SAC_IDENT, "SAC_IDENT"),
      new Tuple(LexicalUnit.SAC_STRING_VALUE, "SAC_STRING_VALUE"),
      new Tuple(LexicalUnit.SAC_ATTR, "SAC_ATTR"),
      new Tuple(LexicalUnit.SAC_RECT_FUNCTION, "SAC_RECT_FUNCTION"),
      new Tuple(LexicalUnit.SAC_UNICODERANGE, "SAC_UNICODERANGE"),
      new Tuple(LexicalUnit.SAC_SUB_EXPRESSION, "SAC_SUB_EXPRESSION"),
      new Tuple(LexicalUnit.SAC_FUNCTION, "SAC_FUNCTION"),
      new Tuple(LexicalUnit.SAC_DIMENSION, "SAC_DIMENSION")
    };



  public static void
  main(String[] args) throws Exception
  {
    if (args.length != 1)
    {
      System.err.println("Usage: be.re.css.TestSAC url_or_filename");
      return;
    }

    Parser	parser = new ParserFactory().makeParser();
    PrintWriter	out = new PrintWriter(System.out);

    parser.setDocumentHandler(new SACWriter(out, new File(args[0]).toURL()));

    parser.parseStyleSheet
    (
      be.re.net.Util.isUrl(args[0]) ?
        new URL(args[0]).toString() :
        be.re.net.Util.fileToUrl(new File(args[0])).toString()
    );

    out.flush();
  }



  private static class SACWriter implements DocumentHandler

  {

    private URL		baseUrl;
    private PrintWriter	out;



    private
    SACWriter(PrintWriter out, URL baseUrl)
    {
      this.out = out;
      this.baseUrl = baseUrl;
    }



    public void
    comment(String text) throws CSSException
    {
      out.println("comment: " + text);
    }



    public void
    endDocument(InputSource source) throws CSSException
    {
      out.println("end document");
    }



    public void
    endFontFace() throws CSSException
    {
      out.println("end font face");
    }



    public void
    endMedia(SACMediaList media) throws CSSException
    {
      out.println("end media");
    }



    public void
    endPage(String name, String pseudePage) throws CSSException
    {
      out.println("end page");
    }



    public void
    endSelector(SelectorList selectors) throws CSSException
    {
      out.println("end selector");
    }



    private static String
    enumerateUnits(LexicalUnit value)
    {
      return
        lexicalUnitTypeToString(value) +
          (
            value.getNextLexicalUnit() != null ?
              (", " + enumerateUnits(value.getNextLexicalUnit())) : ""
          );
    }



    public void
    ignorableAtRule(String atRule) throws CSSException
    {
      out.println("ignorable at-rule: " + atRule);
    }



    public void
    importStyle(String uri, SACMediaList media, String defaultNamespaceURI)
      throws CSSException
    {
      out.println("import: " + uri);

      try
      {
        Parser	parser = new ParserFactory().makeParser();

        parser.setDocumentHandler(new SACWriter(out, new URL(baseUrl, uri)));
        parser.parseStyleSheet(new URL(baseUrl, uri).toString());
      }

      catch (Exception e)
      {
        throw new CSSException(e);
      }
    }



    private static String
    lexicalUnitTypeToString(LexicalUnit value)
    {
      for (int i = 0; i < tuples.length; ++i)
      {
        if (tuples[i].id == value.getLexicalUnitType())
        {
          return tuples[i].name;
        }
      }

      return "unknown";
    }



    public void
    namespaceDeclaration(String prefix, String uri) throws CSSException
    {
      out.println("namespace declaration: " + prefix + ", " + uri);
    }



    public void
    property(String name, LexicalUnit value, boolean important)
      throws CSSException
    {
      out.println
      (
        "property: " + name + ": " +
          Util.lexicalUnitToString(value, false, null) + "(" +
          enumerateUnits(value) + "), " + String.valueOf(important)
      );
    }



    private String
    selectorText(Selector selector)
    {
      switch (selector.getSelectorType())
      {
        case Selector.SAC_ANY_NODE_SELECTOR: return "(any)";

        case Selector.SAC_CDATA_SECTION_NODE_SELECTOR:
          return
            "(cdata: " + ((CharacterDataSelector) selector).getData() + ")";

        case Selector.SAC_CHILD_SELECTOR:
          return
            "(child: " +
              selectorText
              (
                ((DescendantSelector) selector).getAncestorSelector()
              ) + " " +
              selectorText(((DescendantSelector) selector).getSimpleSelector())
              + ")";

        case Selector.SAC_COMMENT_NODE_SELECTOR:
          return
            "(comment: " + ((CharacterDataSelector) selector).getData() + ")";

        case Selector.SAC_CONDITIONAL_SELECTOR:
          return
            "(conditional: " +
              Util.conditionText(((ConditionalSelector) selector).
                getCondition()) +
              " " +
              selectorText(((ConditionalSelector) selector).getSimpleSelector())
              + ")";

        case Selector.SAC_DESCENDANT_SELECTOR:
          return
            "(descendant: " +
              selectorText
              (
                ((DescendantSelector) selector).getAncestorSelector()
              ) + " " +
              selectorText(((DescendantSelector) selector).getSimpleSelector())
              + ")";

        case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
          return
            "(sibling: " +
              selectorText(((SiblingSelector) selector).getSelector()) + " " +
              selectorText(((SiblingSelector) selector).getSiblingSelector())
              + ")";

        case Selector.SAC_ELEMENT_NODE_SELECTOR:
          return
            "(element: " +
              (
                ((ElementSelector) selector).getNamespaceURI() != null ?
                  (((ElementSelector) selector).getNamespaceURI() + "#") : ""
              ) + ((ElementSelector) selector).getLocalName() + ")";

        case Selector.SAC_NEGATIVE_SELECTOR:
          return
            "(negative: " +
              selectorText(((NegativeSelector) selector).getSimpleSelector()) +
              ")";

        case Selector.SAC_PROCESSING_INSTRUCTION_NODE_SELECTOR:
          return
            "(pi: (" + ((ProcessingInstructionSelector) selector).getData() +
            ") (" + ((ProcessingInstructionSelector) selector).getTarget() +
            "))";

        case Selector.SAC_PSEUDO_ELEMENT_SELECTOR:
          return
            "(pseudo: " +
              (
                ((ElementSelector) selector).getNamespaceURI() != null ?
                  (((ElementSelector) selector).getNamespaceURI() + "#") : ""
              ) + ((ElementSelector) selector).getLocalName() + ")";

        case Selector.SAC_ROOT_NODE_SELECTOR: return "(root)";

        case Selector.SAC_TEXT_NODE_SELECTOR:
          return
            "(text: " + ((CharacterDataSelector) selector).getData() + ")";

        default: return "(unknown)";
      }
    }



    public void
    startDocument(InputSource source) throws CSSException
    {
      out.println("start document");
    }



    public void
    startFontFace() throws CSSException
    {
      out.println("start font face");
    }



    public void
    startMedia(SACMediaList media) throws CSSException
    {
      out.print("start media:");

      for (int i = 0; i < media.getLength(); ++i)
      {
        out.print(" " + media.item(i));
      }

      out.println();
    }



    public void
    startPage(final String name, final String pseudoPage) throws CSSException
    {
      out.println("start page: " + name + ", " + pseudoPage);
    }



    public void
    startSelector(SelectorList selectors) throws CSSException
    {
      out.print("start selector:");

      for (int i = 0; i < selectors.getLength(); ++i)
      {
        out.print(" " + selectorText(selectors.item(i)));
      }

      out.println();
    }

  } // SACWriter



  private static class Tuple

  {

    private int		id;
    private String	name;



    private
    Tuple(int id, String name)
    {
      this.id = id;
      this.name = name;
    }

  } // Tuple

} // TestSAC
