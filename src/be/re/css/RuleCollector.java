package be.re.css;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;



/**
 * This class collects rules and page rules from the "all" and "print" media.
 * The other media are ignored. Rules without any properties are also ignored.
 * @author Werner Donn\u00e9
 */

class RuleCollector implements DocumentHandler

{

  private URL		baseUrl;
  private RuleEmitter	ruleEmitter;
  private PageRule	currentPageRule = null;
  private Rule[]	currentRules = null;
  private boolean	ignore = false;
  private int		offset;
  private List		pageRules;
  private Map		prefixMap = new HashMap();
  private int		position;



  RuleCollector
  (
    RuleEmitter	ruleEmitter,
    List	pageRules,
    URL		baseUrl,
    int		startPosition,
    int		offset
  )
  {
    this.ruleEmitter = ruleEmitter;
    this.pageRules = pageRules;
    this.baseUrl = baseUrl;
    this.position = startPosition;
    this.offset = offset;
  }



  public void
  comment(String text) throws CSSException
  {
  }



  public void
  endDocument(InputSource source) throws CSSException
  {
  }



  public void
  endFontFace() throws CSSException
  {
  }



  public void
  endMedia(SACMediaList media) throws CSSException
  {
    ignore = false;
  }



  public void
  endPage(String name, String pseudoPage) throws CSSException
  {
    if (currentPageRule.getProperties().length > 0)
    {
      PageRule[]	split = currentPageRule.split();

      for (int i = 0; i < split.length; ++i)
      {
        pageRules.add(split[i]);
      }
    }

    currentPageRule = null;
  }



  public void
  endSelector(SelectorList selectors) throws CSSException
  {
    if (!ignore)
    {
      for (int i = 0; i < currentRules.length; ++i)
      {
        if (currentRules[i].getProperties().length > 0)
        {
          Rule[]	split = currentRules[i].split();

          for (int j = 0; j < split.length; ++j)
          {
            ruleEmitter.addRule(split[j]);
          }
        }
      }

      currentRules = null;
    }
  }



  int
  getCurrentPosition()
  {
    return position;
  }



  private boolean
  hasOneOfMedia(SACMediaList media, String[] choices)
  {
    if (media == null)
    {
      return false;
    }

    for (int i = 0; i < media.getLength(); ++i)
    {
      for (int j = 0; j < choices.length; ++j)
      {
        if (media.item(i).equals(choices[j]))
        {
          return true;
        }
      }
    }

    return false;
  }



  public void
  ignorableAtRule(String atRule) throws CSSException
  {
  }



  public void
  importStyle(String uri, SACMediaList media, String defaultNamespaceURI)
    throws CSSException
  {
    if (!ignore)
    {
      if
      (
        media == null						||
        hasOneOfMedia(media, new String[] {"all", "print"})
      )
      {
        try
        {
          Parser	parser = Util.getSacParser();
          URL		url =
            (baseUrl != null ? new URL(baseUrl, uri) : new URL(uri));

          RuleCollector	importCollector =
            new RuleCollector(ruleEmitter, pageRules, url, position, offset);
          parser.setDocumentHandler(importCollector);
          parser.parseStyleSheet(url.toString());
          position = importCollector.getCurrentPosition();
        }

        catch (Exception e)
        {
          throw new CSSException(e);
        }
      }
    }
  }



  public void
  namespaceDeclaration(String prefix, String uri) throws CSSException
  {
    prefixMap.put(prefix, uri);
  }



  public void
  property(String name, LexicalUnit value, boolean important)
    throws CSSException
  {
    if (!ignore)
    {
      Property[]	properties =
        new Property(name.toLowerCase(), value, important, prefixMap, baseUrl).
          split();

      if (currentRules != null)
      {
        for (int i = 0; i < currentRules.length; ++i)
        {
          for (int j = 0; j < properties.length; ++j)
          {
            currentRules[i].addProperty(properties[j]);
          }
        }
      }
      else
      {
        if (currentPageRule != null)
        {
          for (int i = 0; i < properties.length; ++i)
          {
            LexicalUnit	unit = properties[i].getLexicalUnit();

            if
            (
              "counter-reset".equals(properties[i].getName())		&&
              unit.getLexicalUnitType() == LexicalUnit.SAC_IDENT	&&
              "page".equals(unit.getStringValue())			&&
              (
                unit.getNextLexicalUnit() == null			||
                unit.getNextLexicalUnit().getLexicalUnitType() ==
                  LexicalUnit.SAC_INTEGER
              )
            )
            {
              properties[i] =
                unit.getNextLexicalUnit() == null ?
                  new Property
                  (
                    "initial-page-number",
                    "1",
                    properties[i].getImportant(),
                    prefixMap
                  ) :
                  new Property
                  (
                    "initial-page-number",
                    unit.getNextLexicalUnit(),
                    properties[i].getImportant(),
                    prefixMap,
                    baseUrl
                  );
            }

            currentPageRule.addProperty(properties[i]);
          }
        }
      }
    }
  }



  public void
  startDocument(InputSource source) throws CSSException
  {
  }



  public void
  startFontFace() throws CSSException
  {
  }



  public void
  startMedia(SACMediaList media) throws CSSException
  {
    ignore = !hasOneOfMedia(media, new String[] {"all", "print"});
  }



  public void
  startPage(final String name, final String pseudoPage) throws CSSException
  {
    if (!ignore)
    {
      currentPageRule =
        new PageRule
        (
          name != null && pseudoPage != null ?
            (pseudoPage + "-" + name ) :
            (
              name != null ?
                name : (pseudoPage != null ? pseudoPage : "unnamed")
            ),
          position++
        );
    }
  }



  public void
  startSelector(SelectorList selectors) throws CSSException
  {
    if (ignore || selectors.getLength() == 0)
    {
      currentRules = null;

      return;
    }

    currentRules = new Rule[selectors.getLength()];

    for (int i = 0; i < currentRules.length; ++i)
    {
      currentRules[i] = new Rule(selectors.item(i), position++, offset);
    }
  }



  interface RuleEmitter

  {

    public void	addRule	(Rule rule);

  } // RuleEmitter

} // RuleCollector
