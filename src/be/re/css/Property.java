package be.re.css;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.w3c.css.sac.LexicalUnit;



/**
 * Represents a CSS2 property. It also implements splitting of shorthand
 * properties into basic properties.
 * @author Werner Donn\u00e9
 */

public class Property

{

  private static final String[]	BACKGROUND_ATTACHMENT = {"fixed", "scroll"};
  private static final String[]	BACKGROUND_REPEAT =
    {"repeat", "repeat-x", "repeat-y", "no-repeat"};
  private static final String[]	BORDER_STYLE =
    {
      "none", "hidden", "dotted", "dashed", "solid", "double", "groove",
        "ridge", "inset", "outset"
    };
  private static final String[]	COLORS =
    {
      "aqua", "black", "blue", "fuchsia", "gray", "green", "lime", "maroon",
        "navy", "olive", "purple", "red", "silver", "teal", "white", "yellow",
        "transparent"
    };
  private static final String[]	FONT_SIZE =
    {
      "xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large",
        "larger", "smaller"
    };
  private static final String[]	FONT_STYLE = {"normal", "italic", "oblique"};
  private static final String[]	FONT_VARIANT = {"normal", "small-caps"};
  private static final String[]	FONT_WEIGHT =
    {"normal", "bold", "bolder", "lighter"};
  private static final String[]	LIST_STYLE_POSITION = {"inside", "outside"};
  private static final String[]	LIST_STYLE_TYPE =
    {
      "disc", "circle", "square", "decimal", "decimal-leading-zero",
        "lower-roman", "upper-roman", "lower-greek", "lower-alpha",
        "lower-latin", "upper-alpha", "upper-latin", "hebrew", "armenian",
        "georgian", "cjk-ideographic", "hiragana", "katakana", "hiragana-iroha",
        "katakana-iroha", "none"
    };
  private static final String[]	SYSTEM_FONTS =
    {"caption", "icon", "menu", "message-box", "small-caption", "status-bar"};

  private URL		baseUrl;
  private boolean	important;
  private String	name;
  private Map		prefixMap;
  private LexicalUnit	value;
  private String	valueAsString;



  /**
   * Is not used for shorthand properties.
   */

  Property(String name, String value, boolean important, Map prefixMap)
  {
    this.name = name;
    this.valueAsString = value;
    this.important = important;
    this.prefixMap = prefixMap;
  }



  public
  Property
  (
    String	name,
    LexicalUnit	value,
    boolean	important,
    Map		prefixMap,
    URL		baseUrl
  )
  {
    this.name = name;
    this.value = value;
    this.valueAsString =
      Util.lexicalUnitToString(value, !"font-family".equals(name), baseUrl);
    this.important = important;
    this.prefixMap = prefixMap;
    this.baseUrl = baseUrl;
  }



  private Property
  copy(String name, String value)
  {
    return new Property(name, value, getImportant(), getPrefixMap());
  }



  public boolean
  getImportant()
  {
    return important;
  }



  public String
  getName()
  {
    return name;
  }



  public LexicalUnit
  getLexicalUnit()
  {
    return value;
  }



  public Map
  getPrefixMap()
  {
    return prefixMap;
  }



  public String
  getValue()
  {
    return valueAsString;
  }



  private boolean
  isFontSize(LexicalUnit unit)
  {
    int	type = unit.getLexicalUnitType();

    return
      type == LexicalUnit.SAC_CENTIMETER ||
      type == LexicalUnit.SAC_EM ||
      type == LexicalUnit.SAC_EX ||
      type == LexicalUnit.SAC_INCH ||
      type == LexicalUnit.SAC_MILLIMETER ||
      type == LexicalUnit.SAC_PERCENTAGE ||
      type == LexicalUnit.SAC_PICA ||
      type == LexicalUnit.SAC_PIXEL ||
      type == LexicalUnit.SAC_POINT ||
      Util.inArray(FONT_SIZE, Util.lexicalUnitAtomLower(unit, baseUrl));
  }



  private static boolean
  isFontWeight(String atom)
  {
    try
    {
      return
        Util.inArray(FONT_WEIGHT, atom) ||
          (Integer.parseInt(atom) >= 100 && Integer.parseInt(atom) <= 900);
    }

    catch (NumberFormatException e)
    {
      return false;
    }
  }



  private Property[]
  setAtoms(String[] names, String[] atoms)
  {
    Property[]	result = new Property[names.length];

    for (int i = 0; i < names.length; ++i)
    {
      result[i] = copy(names[i], atoms[i]);
    }

    return result;
  }



  public Property[]
  split()
  {
    StringTokenizer	tokenizer = new StringTokenizer(getName(), "-");
    String		name = "split";

    while (tokenizer.hasMoreTokens())
    {
      String	token = tokenizer.nextToken();

      name += token.substring(0, 1).toUpperCase() + token.substring(1);
    }

    try
    {
      return
        (Property[])
          getClass().getDeclaredMethod(name, new Class[0]).
            invoke(this, new Object[0]);
    }

    catch (IllegalAccessException e)
    {
      throw new UndeclaredThrowableException(e);
    }

    catch (InvocationTargetException e)
    {
      throw new UndeclaredThrowableException(e);
    }

    catch (NoSuchMethodException e)
    {
      return new Property[] {this};
    }
  }



  private Property[]
  splitBackground()
  {
    List	result = new ArrayList();
    String	remaining = null;

    for (LexicalUnit i = value; i != null; i = i.getNextLexicalUnit())
    {
      String	atom = Util.lexicalUnitAtomLower(i, baseUrl);

      if (i.getLexicalUnitType() == LexicalUnit.SAC_URI)
      {
        result.add(copy("background-image", atom));
      }
      else
      {
        if
        (
          i.getLexicalUnitType() == LexicalUnit.SAC_RGBCOLOR	||
          Util.inArray(COLORS, atom)
        )
        {
          result.add(copy("background-color", atom));
        }
        else
        {
          if (Util.inArray(BACKGROUND_ATTACHMENT, atom))
          {
            result.add(copy("background-attachment", atom));
          }
          else
          {
            if (Util.inArray(BACKGROUND_REPEAT, atom))
            {
              result.add(copy("background-repeat", atom));
            }
            else
            {
              remaining = remaining == null ? atom : (remaining + " " + atom);
            }
          }
        }
      }
    }

    if (remaining != null)
    {
      result.add(copy("background-position", remaining));
    }

    return (Property[]) result.toArray(new Property[result.size()]);
  }



  private Property[]
  splitBorder()
  {
    List	result = new ArrayList();
    String	remaining = null;

    for (LexicalUnit i = value; i != null; i = i.getNextLexicalUnit())
    {
      String    atom = Util.lexicalUnitAtomLower(i, baseUrl);

      if
      (
        i.getLexicalUnitType() == LexicalUnit.SAC_RGBCOLOR	||
        Util.inArray(COLORS, atom)				||
        atom.equals("transparant")
      )
      {
        result.add(copy("border-top-color", atom));
        result.add(copy("border-right-color", atom));
        result.add(copy("border-bottom-color", atom));
        result.add(copy("border-left-color", atom));
      }
      else
      {
        if (Util.inArray(BORDER_STYLE, atom))
        {
          result.add(copy("border-top-style", atom));
          result.add(copy("border-right-style", atom));
          result.add(copy("border-bottom-style", atom));
          result.add(copy("border-left-style", atom));
        }
        else
        {
          remaining = remaining == null ? atom : (remaining + " " + atom);
        }
      }
    }

    if (remaining != null)
    {
      result.add(copy("border-top-width", remaining));
      result.add(copy("border-bottom-width", remaining));
      result.add(copy("border-left-width", remaining));
      result.add(copy("border-right-width", remaining));
      result.add(copy("border-after-width.conditionality", "retain"));
      result.add(copy("border-before-width.conditionality", "retain"));
    }

    return (Property[]) result.toArray(new Property[result.size()]);
  }



  private Property[]
  splitBorderBottom()
  {
    return splitBorderSide("bottom");
  }



  private Property[]
  splitBorderBottomWidth()
  {
    Property[]	result = new Property[2];

    result[0] = copy(getName(), getValue());
    result[1] = copy("border-after-width.conditionality", "retain");

    return result;
  }



  private Property[]
  splitBorderColor()
  {
    return splitFourWays(getName(), getImportant());
  }



  private Property[]
  splitBorderLeft()
  {
    return splitBorderSide("left");
  }



  private Property[]
  splitBorderRight()
  {
    return splitBorderSide("right");
  }



  private Property[]
  splitBorderStyle()
  {
    return splitFourWays(getName(), getImportant());
  }



  private Property[]
  splitBorderTop()
  {
    return splitBorderSide("top");
  }



  private Property[]
  splitBorderSide(String side)
  {
    List	result = new ArrayList();
    String	remaining = null;

    for (LexicalUnit i = value; i != null; i = i.getNextLexicalUnit())
    {
      String    atom = Util.lexicalUnitAtomLower(i, baseUrl);

      if
      (
        i.getLexicalUnitType() == LexicalUnit.SAC_RGBCOLOR	||
        Util.inArray(COLORS, atom)				||
        atom.equals("transparant")
      )
      {
        result.add(copy("border-" + side + "-color", atom));
      }
      else
      {
        if (Util.inArray(BORDER_STYLE, atom))
        {
          result.add(copy("border-" + side + "-style", atom));
        }
        else
        {
          remaining = remaining == null ? atom : (remaining + " " + atom);
        }
      }
    }

    if (remaining != null)
    {
      result.add(copy("border-" + side + "-width", remaining));

      if ("top".equals(side))
      {
        result.add(copy("border-before-width.conditionality", "retain"));
      }
      else
      {
        if ("bottom".equals(side))
        {
          result.add(copy("border-after-width.conditionality", "retain"));
        }
      }
    }

    return (Property[]) result.toArray(new Property[result.size()]);
  }



  private Property[]
  splitBorderTopWidth()
  {
    Property[]	result = new Property[2];

    result[0] = copy(getName(), getValue());
    result[1] = copy("border-before-width.conditionality", "retain");

    return result;
  }



  private Property[]
  splitBorderWidth()
  {
    Property[]	values = splitFourWays(getName(), getImportant());
    Property[]	result = new Property[values.length + 2];

    System.arraycopy(values, 0, result, 0, values.length);
    result[values.length] = copy("border-after-width.conditionality", "retain");
    result[values.length + 1] =
      copy("border-before-width.conditionality", "retain");

    return result;
  }



  private Property[]
  splitFont()
  {
    List	result = new ArrayList();
    String	remaining = null;

    for (LexicalUnit i = value; i != null; i = i.getNextLexicalUnit())
    {
      String	originalAtom = Util.lexicalUnitAtom(i, baseUrl);
      String	atom = originalAtom.toLowerCase();

      if (Util.inArray(SYSTEM_FONTS, atom))
      {
        result.add(copy("font", atom));
      }
      else
      {
        if (Util.inArray(FONT_STYLE, atom))
        {
          result.add(copy("font-style", atom));
        }
        else
        {
          if (Util.inArray(FONT_VARIANT, atom))
          {
            result.add(copy("font-variant", atom));
          }
          else
          {
            if (isFontWeight(atom))
            {
              result.add(copy("font-weight", atom));
            }
            else
            {
              if
              (
                (
                  i.getPreviousLexicalUnit() == null			||
                  i.getPreviousLexicalUnit().getLexicalUnitType() !=
                    LexicalUnit.SAC_OPERATOR_SLASH
                )							&&
                isFontSize(i)
              )
              {
                result.add(copy("font-size", atom));
              }
              else
              {
                if (i.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_SLASH)
                {
                  i = i.getNextLexicalUnit();

                  result.add
                  (
                    copy("line-height", Util.lexicalUnitAtomLower(i, baseUrl))
                  );
                }
                else
                {
                  remaining =
                    remaining == null ?
                      originalAtom : (remaining + " " + originalAtom);
                }
              }
            }
          }
        }
      }
    }

    if (remaining != null)
    {
      result.add(copy("font-family", remaining));
    }

    return (Property[]) result.toArray(new Property[result.size()]);
  }



  private Property[]
  splitFourWays(String name, boolean important)
  {
    String[]		names = splitFourWaysNames(name);
    LexicalUnit[]	units = Util.lexicalUnitArray(value);

    if (units.length == 1)
    {
      String	atom = Util.lexicalUnitAtomLower(units[0], baseUrl);

      return setAtoms(names, new String[] {atom, atom, atom, atom});
    }

    if (units.length == 2)
    {
      String	atom1 = Util.lexicalUnitAtomLower(units[0], baseUrl);
      String	atom2 = Util.lexicalUnitAtomLower(units[1], baseUrl);

      return setAtoms(names, new String[] {atom1, atom2, atom1, atom2});
    }

    if (units.length == 3)
    {
      String	atom1 = Util.lexicalUnitAtomLower(units[0], baseUrl);
      String	atom2 = Util.lexicalUnitAtomLower(units[1], baseUrl);
      String	atom3 = Util.lexicalUnitAtomLower(units[2], baseUrl);

      return setAtoms(names, new String[] {atom1, atom2, atom3, atom2});
    }

    if (units.length == 4)
    {
      String	atom1 = Util.lexicalUnitAtomLower(units[0], baseUrl);
      String	atom2 = Util.lexicalUnitAtomLower(units[1], baseUrl);
      String	atom3 = Util.lexicalUnitAtomLower(units[2], baseUrl);
      String	atom4 = Util.lexicalUnitAtomLower(units[3], baseUrl);

      return setAtoms(names, new String[] {atom1, atom2, atom3, atom4});
    }

    return new Property[] {this};
  }



  private static String[]
  splitFourWaysNames(String name)
  {
    String[]		result = new String[4];
    StringTokenizer	tokenizer = new StringTokenizer(name, "-");

    while (tokenizer.hasMoreTokens())
    {
      String	token = tokenizer.nextToken();

      if (result[0] == null)
      {
        result[0] = token + "-top";
        result[1] = token + "-right";
        result[2] = token + "-bottom";
        result[3] = token + "-left";
      }
      else
      {
        for (int i = 0; i < result.length; ++i)
        {
          result[i] += "-" + token;
        }
      }
    }

    return result;
  }



  private Property[]
  splitListStyle()
  {
    List	result = new ArrayList();

    for (LexicalUnit i = value; i != null; i = i.getNextLexicalUnit())
    {
      String    atom = Util.lexicalUnitAtomLower(i, baseUrl);

      if (atom.equals("none"))
      {
        result.add(copy("list-style-type", atom));
        result.add(copy("list-style-image", atom));
      }
      else
      {
        if (i.getLexicalUnitType() == LexicalUnit.SAC_URI)
        {
          result.add(copy("list-style-image", atom));
        }
        else
        {
          if (Util.inArray(LIST_STYLE_POSITION, atom))
          {
            result.add(copy("list-style-position", atom));
          }
          else
          {
            if (Util.inArray(LIST_STYLE_TYPE, atom))
            {
              result.add(copy("list-style-type", atom));
            }
          }
        }
      }
    }

    return (Property[]) result.toArray(new Property[result.size()]);
  }



  private Property[]
  splitMargin()
  {
    return splitFourWays(getName(), getImportant());
  }



  private Property[]
  splitPadding()
  {
    return splitFourWays(getName(), getImportant());
  }

} // Property
