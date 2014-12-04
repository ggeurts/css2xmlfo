package be.re.xml;

import be.re.io.StreamConnector;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/**
 * This entity resolver uses a catalog as defined by SGML Open Technical
 * Resolution TR9401:1997. Only PUBLIC and SYSTEM statements are supported at
 * this time. Relative URLs are resolved using the catalog URL as the base URL.
 * @author Werner Donn\u00e9
 */

public class CatalogResolver implements EntityResolver, XMLResolver

{

  // Alphabet.

  final static int		SINGLE_QUOTE = 0;
  final static int		DOUBLE_QUOTE = 1;
  final static int		OTHER = 2;
  final static int		SPACE = 3;
  final static int		WHITE = 4;
  final static int		EOF = 5;

  // States.

  final static int		TYP = 0;
  final static int		SQ1 = 1;
  final static int		DQ1 = 2;
  final static int		ID1 = 3;
  final static int		SQ2 = 4;
  final static int		DQ2 = 5;
  final static int		ERR = 6;

  final static int[][][]	FSM =
    {
      {{SQ1, 1}, {DQ1, 1}, {TYP, 0}, {TYP, 0}, {TYP, 0}, {TYP, 0}}, // TYP
      {{ID1, 1}, {SQ1, 0}, {SQ1, 0}, {SQ1, 0}, {ERR, 0}, {ERR, 0}}, // SQ1
      {{DQ1, 0}, {ID1, 1}, {DQ1, 0}, {DQ1, 0}, {ERR, 0}, {ERR, 0}}, // DQ1
      {{SQ2, 1}, {DQ2, 1}, {ERR, 0}, {ID1, 0}, {ID1, 0}, {ERR, 0}}, // ID1
      {{TYP, 1}, {SQ2, 0}, {SQ2, 0}, {SQ2, 0}, {ERR, 0}, {ERR, 0}}, // SQ2
      {{DQ2, 0}, {TYP, 1}, {DQ2, 0}, {DQ2, 0}, {ERR, 0}, {ERR, 0}}  // DQ2
    };

  private String	catalogSystemId;
  private Map		publicIdentifiers = new HashMap();
  private Map		systemIdentifiers = new HashMap();



  public
  CatalogResolver(URL catalogUrl) throws IOException
  {
    this(catalogUrl.toString(), null);
  }



  public
  CatalogResolver(String catalogSystemId) throws IOException
  {
    this(catalogSystemId, null);
  }



  public
  CatalogResolver(URL catalogUrl, InputStream in) throws IOException
  {
    this(catalogUrl.toString(), in);
  }



  public
  CatalogResolver(String catalogSystemId, InputStream in) throws IOException
  {
    this.catalogSystemId = catalogSystemId;

    load
    (
      in != null ?
        in :
        (
          isUrl(catalogSystemId) ?
            new URL(catalogSystemId).openStream() :
            new FileInputStream(catalogSystemId)
        )
    );
  }



  private static int
  category(int c)
  {
    return
      c == '\'' ?
        SINGLE_QUOTE :
        (
          c == '\"' ?
            DOUBLE_QUOTE :
            (
              c == ' ' ?
                SPACE :
                (
                  c == '\t' || c == '\n' || c == '\r' ?
                    WHITE : OTHER
                )
            )
        );
  }



  private static void
  error(int in, int line) throws IOException
  {
    if (in == EOF)
    {
      throw
        new IOException
        (
          "Line " + String.valueOf(line) + ": premature end of file"
        );
    }

    if (in == WHITE)
    {
      throw
        new IOException
        (
          "Line " + String.valueOf(line) +
            ": \\t, \\n and \\r are not allowed in an identifier"
        );
    }

    if (in == OTHER)
    {
      throw
        new IOException
        (
          "Line " + String.valueOf(line) + ": white space expected"
        );
    }
  }



  /**
   * Returns a map from the public identifiers to the resolved URLs.
   */

  public Map
  getPublicIdentifierMappings()
  {
    return publicIdentifiers;
  }



  /**
   * Returns a map from the public identifiers to the resolved URLs.
   */

  public Map
  getSystemIdentifierMappings()
  {
    return systemIdentifiers;
  }



  private static String
  getTypeToken(char[] c, int off, int len, int line) throws IOException
  {
    StringTokenizer	tokenizer =
      new StringTokenizer(new String(c, off, len), " \t\n\r");

    if (!tokenizer.hasMoreTokens())
    {
      throw
        new IOException
        (
          "Line " + String.valueOf(line) + ": PUBLIC or SYSTEM expected"
        );
    }

    String	token = tokenizer.nextToken();

    if (!token.equals("PUBLIC") && !token.equals("SYSTEM"))
    {
      throw
        new IOException
        (
          "Line " + String.valueOf(line) + ": PUBLIC or SYSTEM expected"
        );
    }

    return token;
  }



  private static boolean
  isUrl(String s)
  {
    try
    {
      return s != null && new URL(s) != null;
    }

    catch (MalformedURLException e)
    {
      return false;
    }
  }



  private void
  load(InputStream in) throws IOException
  {
    ByteArrayOutputStream	out = new ByteArrayOutputStream();

    StreamConnector.copy(in, out);

    char[]	c = new String(out.toByteArray(), "ASCII").toCharArray();
    String	from = null;
    int		line = 1;
    int		position = 0;
    int		state = TYP;
    String	type = null;

    for (int i = 0; i < c.length; ++i)
    {
      int[]	next = FSM[state][category(c[i])];

      if (next[0] == ERR)
      {
        error(category(c[i]), line);
      }

      if (next[1] == 1)
      {
        Map	map;

        switch (state)
        {
          case TYP:
            type = getTypeToken(c, position, i - position, line);
            break;

          case SQ1: case DQ1:
            from = new String(c, position, i - position);
            break;

          case SQ2: case DQ2:
            map = type.equals("PUBLIC") ? publicIdentifiers : systemIdentifiers;

            map.put
            (
              from,
              resolveSystemId
              (
                catalogSystemId,
                new String(c, position, i - position)
              )
            );

            break;
        }

        position = i + 1;
      }

      state = next[0];

      if (c[i] == '\n')
      {
        ++line;
      }
    }

    if (FSM[state][EOF][0] == ERR)
    {
      error(EOF, line);
    }
  }



  public InputSource
  resolveEntity(String publicId, String systemId)
    throws IOException, SAXException
  {
    InputSource	result =
      publicId != null && publicIdentifiers.get(publicId) != null ?
        new InputSource(publicIdentifiers.get(publicId).toString()) :
        (
          systemId != null && systemIdentifiers.get(systemId) != null ?
            new InputSource(systemIdentifiers.get(systemId).toString()) : null
        );

    if (result != null)
    {
      result.setPublicId(publicId);
    }

    return result;
  }



  public Object
  resolveEntity
  (
    String	publicId,
    String	systemId,
    String	baseURI,
    String	namespace
  ) throws XMLStreamException
  {
    try
    {
      StreamSource	result =
        new StreamSource
        (
          publicId != null && publicIdentifiers.get(publicId) != null ?
            publicIdentifiers.get(publicId).toString() :
            (
              systemId != null && systemIdentifiers.get(systemId) != null ?
                systemIdentifiers.get(systemId).toString() :
                (
                  baseURI != null && systemId != null ?
                    resolveSystemId(baseURI, systemId) : null
                )
            )
          );

      result.setPublicId(publicId);

      return result;
    }

    catch (IOException e)
    {
      throw new XMLStreamException(e);
    }
  }



  private static String
  resolveSystemId(String baseURI, String systemId) throws IOException
  {
    return
      isUrl(baseURI) ?
        new URL(new URL(baseURI), systemId).toString() :
        (
          systemId.charAt(0) == '/' ?
            systemId :
            (
              baseURI.charAt(baseURI.length() - 1) == '/' ?
                (baseURI + systemId) :
                (baseURI.substring(0, baseURI.lastIndexOf('/') + 1) + systemId)
            )
        );
  }

} // CatalogResolver
