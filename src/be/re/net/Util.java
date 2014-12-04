package be.re.net;

import be.re.io.ReadLineInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;



public class Util

{

  private final static char[]	MARK =
    {'-', '_', '.', '!', '~', '*', '\'', '(', ')'};
  private final static char[]	PCHAR_SPECIALS =
    {':', '@', '&', '=', '+', '$', ','}; // Less than uric_no_slash.
  private final static char[]	RESERVED =
    {';', '/', '?', ':', '@', '&', '=', '+', '$', ','};

  private final static String[]	archiveExtensions =
    {"ear", "jar", "rar", "war", "zip"};
  private static ResourceBundle	bundle = null;
  private static Map		bundles = new HashMap();



  private static boolean
  compareBytes(byte[] b1, byte[] b2, int length)
  {
    for (int i = 0; i < length; ++i)
    {
      if (b1[i] != b2[i])
      {
        return false;
      }
    }

    return true;
  }



  private static String
  escapeUriPart(String s, TestChar t)
  {
    byte[]	bytes = null;
    String	result = "";

    try
    {
      bytes = s.getBytes("UTF-8");
    }

    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e); // Would be a bug.
    }

    char[]	c = new char[bytes.length];

    for (int i = 0; i < bytes.length; ++i)
    {
      c[i] = (char) (0xff & bytes[i]);
    }

    for (int i = 0; i < c.length; ++i)
    {
      result +=
        !t.test(c, i) ?
          ("%" + (c[i] < 0x10 ? "0" : "") + Integer.toHexString(c[i])) :
          new String(c, i, 1);
    }

    return result;
  }



  public static String
  escapeUriPathSegment(String segment)
  {
    return escapeUriPathSegment(segment, false);
  }



  public static String
  escapeUriPathSegment(String segment, final boolean noParameters)
  {
    return
      escapeUriPart
      (
        segment,
        new TestChar()
        {
          public boolean
          test(char[] c, int i)
          {
            return
              isPChar(c[i]) || (!noParameters && c[i] == ';') ||
                isStartOfEscape(c, i);
          }
        }
      );
  }



  public static String
  escapeUriPathSegments(String path)
  {
    return escapeUriPathSegments(path, false);
  }



  public static String
  escapeUriPathSegments(String path, boolean noParameters)
  {
    String		result = "";
    StringTokenizer	tokenizer = new StringTokenizer(path, "/");

    while (tokenizer.hasMoreTokens())
    {
      result +=
        (result.equals("") ? "" : "/") +
          escapeUriPathSegment(tokenizer.nextToken(), noParameters);
    }

    return
      (path.length() > 0 && path.charAt(0) == '/' ? "/" : "") + result +
        (path.length() > 1 && path.charAt(path.length() - 1) == '/' ? "/" : "");
  }



  public static String
  escapeUriQueryString(String queryString)
  {
    return
      escapeUriPart
      (
        queryString,
        new TestChar()
        {
          public boolean
          test(char[] c, int i)
          {
            return (!isReserved(c[i]) || c[i] == '=') && isUriChar(c, i);
          }
        }
      );
  }



  public static String
  escapeUriQueryStrings(String path)
  {
    String		result = "";
    StringTokenizer	tokenizer = new StringTokenizer(path, "&");

    while (tokenizer.hasMoreTokens())
    {
      result +=
        (result.equals("") ? "" : "&") +
          escapeUriQueryString(tokenizer.nextToken());
    }

    return result;
  }



  public static String
  escapeUriReference(String reference)
  {
    return
      escapeUriPart
      (
        reference,
        new TestChar()
        {
          public boolean
          test(char[] c, int i)
          {
            return !isReserved(c[i]) && isUriChar(c, i);
          }
        }
      );
  }



  private static URL
  escapedComposedUrl(String url) throws MalformedURLException
  {
    return
      new URL
      (
        url.substring(0, url.indexOf(':')) + ":" +
          escapedUrl(extractSubUrl(url).toString()).toString() + "!/" +
          escapeUriPathSegments(extractComposedUrlEntry(url)).toString()
      );
  }



  private static File
  escapedFile(File file)
  {
    return
      new File
      (
        escapeUriPathSegments
        (
          file.getAbsolutePath().replace(File.separatorChar, '/')
        )
      );
  }



  public static String
  escapedRelativeUrl(String url, String protocol)
  {
    return escapedRelativeUrl(url, protocol, false);
  }



  public static String
  escapedRelativeUrl(String url, String protocol, boolean noParameters)
  {
    int	queryStart = -1;
    int	referenceStart = -1;

    if ("http".equals(protocol) || "https".equals(protocol))
    {
      queryStart = url.indexOf('?');
      referenceStart = url.indexOf('#', queryStart != -1 ? queryStart : 0);
    }

    return
      escapeUriPathSegments
      (
        url.substring
        (
          0,
          queryStart != -1 ?
            queryStart : (referenceStart != -1 ?  referenceStart : url.length())
        ),
        noParameters
      ) +
      (
        queryStart != -1 ?
          (
            "?" +
              escapeUriQueryStrings
              (
                url.substring
                (
                  queryStart + 1,
                  referenceStart != -1 ? referenceStart : url.length()
                )
              )
          ) : ""
      ) +
      (
        referenceStart != -1 ?
          ("#" + escapeUriReference(url.substring(referenceStart + 1))) : ""
      );
  }



  public static URL
  escapedUrl(String url) throws MalformedURLException
  {
    return escapedUrl(url, false);
  }



  public static URL
  escapedUrl(String url, boolean noParameters) throws MalformedURLException
  {
    int	colon = url.indexOf(':');

    if (colon != -1 && isComposedUrl(url))
    {
      return escapedComposedUrl(url);
    }

    int	pathStart =
      colon != -1 ?
        url.indexOf
        (
          '/',
          colon +
            (
              url.length() > colon + 2 &&
                url.substring(colon + 1, colon + 3).equals("//") ? 3 : 1
            )
        ) : 0;

    return
      pathStart == -1 ?
        new URL(url) :
        new URL
        (
          url.substring(0, pathStart) +
            escapedRelativeUrl
            (
              url.substring(pathStart),
              colon != -1 ? url.substring(0, colon) : null,
              noParameters
            )
        );
  }



  public static String
  extractComposedUrlEntry(URL url)
  {
    return extractComposedUrlEntry(url.getFile());
  }



  public static String
  extractComposedUrlEntry(String url)
  {
    int	index = url.lastIndexOf("!/");

    return
      index == -1 || index + 2 == url.length() ? "" : url.substring(index + 2);
  }



  public static URL
  extractSubUrl(URL url) throws MalformedURLException
  {
    if (!isComposedUrl(url))
    {
      throw new MalformedURLException(url.toString() + ": no sub-URL");
    }

    return
      new URL
      (
        url.toString().substring
        (
          url.getProtocol().length() + 1, url.toString().lastIndexOf("!/")
        )
      );
  }



  public static URL
  extractSubUrl(String url) throws MalformedURLException
  {
    return extractSubUrl(new URL(url));
  }



  public static URL
  fileToUrl(File file)
  {
    try
    {
      return
        escapedFile
        (
          new File
          (
            file.getAbsolutePath().replace(File.separatorChar, '/')
          )
        ).toURL();
    }

    catch (MalformedURLException e)
    {
      throw new RuntimeException(e);
        // We made sure the path can be parsed as an URL by escaping it.
    }
  }



  private static String
  getExtension(URL url)
  {
    return url.toString().substring(url.toString().lastIndexOf(".") + 1);
  }



  public static String
  getFTPFilename(URL url)
  {
    // According to RFC 959 the file system conventions of the involved
    // server must be followed. The applicable information is, however, not
    // available anymore in the URL, because there the URI rules are followed.
    // Therefore, we assure to have forward slashes, which are widely accepted.

    return urlToFile(url).getPath().replace('\\', '/');
  }



  public static String
  getHttpReasonPhrase(int statusCode)
  {
    return getHttpReasonPhrase(statusCode, new String[0]);
  }



  public static String
  getHttpReasonPhrase(int statusCode, String languageTag)
  {
    return getHttpReasonPhrase(statusCode, new String[]{languageTag});
  }



  public static String
  getHttpReasonPhrase(int statusCode, String[] languageTags)
  {
    return getResource("http_" + String.valueOf(statusCode), languageTags);
  }



  public static String
  getJarEntry(URL url)
  {
    return unescapeUriSpecials(extractComposedUrlEntry(url));
  }



  public static String
  getLastPathSegment(URL url)
  {
    return
      getLastPathSegment
      (
        isComposedUrl(url) ?
          url.getFile().substring(url.getFile().lastIndexOf("!/") + 1) :
          url.getFile()
      );
  }



  /**
   * A trailing slash is ignored to determine the last segment, but it is part
   * of it if it is there.
   */

  public static String
  getLastPathSegment(String path)
  {
    // We're not interested in the last character if it is a slash.

    path = path.replace('\\', '/');

    return path.substring(path.lastIndexOf('/', path.length() - 2) + 1);
  }



  public static Properties
  getParameters(String queryString)
  {
    if (queryString == null)
    {
      return new Properties();
    }

    Properties		result = new Properties();
    StringTokenizer	tokenizer = new StringTokenizer(queryString, "&");

    while (tokenizer.hasMoreTokens())
    {
      String	token = tokenizer.nextToken();
      int	index = token.indexOf('=');

      if (index != -1)
      {
        result.
          setProperty(token.substring(0, index), token.substring(index + 1));
      }
    }

    return result;
  }



  public static String[]
  getPathSegments(String path)
  {
    List		result = new ArrayList();
    StringTokenizer	tokenizer = new StringTokenizer(path, "/");

    while (tokenizer.hasMoreTokens())
    {
      String	token = tokenizer.nextToken();

      if (!"".equals(token))
      {
        result.add(token);
      }
    }

    return (String[]) result.toArray(new String[0]);
  }



  public static String
  getResource(String key)
  {
    if (bundle == null)
    {
      bundle = ResourceBundle.getBundle("be.re.net.res.Res");
    }

    return bundle.getString(key);
  }



  public static String
  getResource(String key, String languageTag)
  {
    return getResource(key, new String[]{languageTag});
  }



  /**
   * Try <code>languageTags</code> until a match is found. Return the default
   * otherwise.
   */

  public static String
  getResource(String key, String[] languageTags)
  {
    for (int i = 0; i < languageTags.length; ++i)
    {
      ResourceBundle	bundle =
        (ResourceBundle)
          bundles.get(be.re.util.Util.getLocale(languageTags[i]));

      if (bundle == null)
      {
        bundle = getResourceBundle(languageTags[i]);

        if (bundle != null)
        {
          bundles.put(be.re.util.Util.getLocale(languageTags[i]), bundle);
        }
      }

      if (bundle != null)
      {
        return bundle.getString(key);
      }
    }

    return getResource(key);
  }



  private static ResourceBundle
  getResourceBundle(String language)
  {
    try
    {
      Locale		locale = be.re.util.Util.getLocale(language);
      ResourceBundle	result =
        ResourceBundle.getBundle("be.re.net.res.Res", locale);

      return result.getLocale().equals(locale) ? result : null;
    }

    catch (MissingResourceException e)
    {
      return null;
    }
  }



  public static boolean
  hasBody(Headers headers)
  {
    return
      (
        headers.get("Content-Length").length > 0 &&
          Integer.parseInt(headers.get("Content-Length")[0]) > 0
      ) || headers.get("Transfer-Encoding").length > 0;
  }



  public static long
  httpDate(String s)
  {
    String[]	patterns =
      new String[]
      {
        "EEE, dd MMM yyyy HH:mm:ss Z", // RFC1123-date
        "EEEE, dd-MMM-yy HH:mm:ss Z", // RFC850-date
        "EEE MMM dd HH:mm:ss yyyy", // ASCTIME-date
        "EEE MMM  d HH:mm:ss yyyy" // ASCTIME-date
      };

    for (int i = 0; i < patterns.length; ++i)
    {
      try
      {
        Date	date =
          new SimpleDateFormat(patterns[i], new Locale("en", "US")).parse(s);

        if (date != null)
        {
          return date.getTime();
        }
      }

      catch (ParseException e)
      {
      }
    }

    return -1;
  }



  public static String
  httpDate(long date)
  {
    return
      new SimpleDateFormat
      (
        "EEE, dd MMM yyyy HH:mm:ss Z",
        new Locale("en", "US")
      ).format(new Date(date));
  }



  /**
   * Extract the status code from the status line or <code>-1</code> if that is
   * not possible.
   */

  public static int
  httpStatusCode(String statusLine)
  {
    StringTokenizer	tokenizer = new StringTokenizer(statusLine);

    if
    (
      tokenizer.countTokens() < 2			||
      !tokenizer.nextToken().startsWith("HTTP/")
    )
    {
      return -1;
    }

    try
    {
      return Integer.parseInt(tokenizer.nextToken());
    }

    catch (NumberFormatException e)
    {
      return -1;
    }
  }



  public static boolean
  isAncestor(URL ancestor, URL child)
  {
    try
    {
      if (isArchive(ancestor) && "jar".equals(child.getProtocol()))
      {
        return isAncestor(new URL("jar:" + ancestor.toString() + "!/"), child);
      }

      if
      (
        "file".equals(ancestor.getProtocol())	&&
        "jar".equals(child.getProtocol())
      )
      {
        return isAncestor(ancestor, Util.extractSubUrl(child));
      }
    }

    catch (MalformedURLException e)
    {
      throw new RuntimeException(e); // Would be a bug.
    }

    if
    (
      ancestor.getProtocol() == null				||
      child.getProtocol() == null				||
      !ancestor.getProtocol().equals(child.getProtocol())	||
      !ancestor.getHost().equals(child.getHost())		||
      ancestor.getPort() != child.getPort()
    )
    {
      return false;
    }

    return
      isAncestor(ancestor.getFile(), child.getFile()) ||
        (
          "file".equals(ancestor.getProtocol()) &&
            "file".equals(child.getProtocol()) &&
            isAncestor(urlToFile(ancestor), urlToFile(child))
        );
  }



  private static boolean
  isAncestor(String path1, String path2)
  {
    StringTokenizer	t1 = new StringTokenizer(path1, "/");
    StringTokenizer	t2 = new StringTokenizer(path2, "/");

    while (t1.hasMoreTokens())
    {
      if (!t2.hasMoreTokens() || !t1.nextToken().equals(t2.nextToken()))
      {
        return false;
      }
    }

    return t2.hasMoreTokens();
  }



  public static boolean
  isAncestor(File file1, File file2)
  {
    return
      file1 != null && file2 != null &&
        (
          file1.equals(file2.getParentFile()) ||
            isAncestor(file1, file2.getParentFile())
        );
  }



  public static boolean
  isArchive(URL url)
  {
    return
      url.getProtocol() != null && url.getProtocol().equals("file") &&
        isArchiveExtension(getExtension(url));
  }



  private static boolean
  isArchiveExtension(String s)
  {
    for (int i = 0; i < archiveExtensions.length; ++i)
    {
      if (archiveExtensions[i].equalsIgnoreCase(s))
      {
        return true;
      }
    }

    return false;
  }



  public static boolean
  isComposedUrl(URL url)
  {
    return
      "jar".equals(url.getProtocol()) && url.toString().lastIndexOf("!/") != -1;
  }



  public static boolean
  isComposedUrl(String url) throws MalformedURLException
  {
    return isComposedUrl(new URL(url));
  }



  private static boolean
  isPChar(char c)
  {
    return isUnreserved(c) || be.re.util.Array.inArray(PCHAR_SPECIALS, c);
  }



  private static boolean
  isReserved(char c)
  {
    return be.re.util.Array.inArray(RESERVED, c);
  }



  private static boolean
  isStartOfEscape(char[] c, int i)
  {
    return
      c[i] == '%' && i < c.length - 2 &&
        be.re.util.Util.isInteger(new String(c, i + 1, 2), 16);
  }



  private static boolean
  isUnreserved(char c)
  {
    return
      (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
        (c >= '0' && c <= '9') || be.re.util.Array.inArray(MARK, c);
  }



  private static boolean
  isUriChar(char[] c, int i)
  {
    return isReserved(c[i]) || isUnreserved(c[i]) || isStartOfEscape(c, i);
  }



  public static boolean
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



  public static Headers
  readHeaders(ReadLineInputStream in) throws IOException
  {
    Headers	headers = new Headers();
    byte[]	line;

    while ((line = in. readLine()) != null && line.length > 0)
    {
      String	s = new String(line);

      if (s.indexOf(':') != -1)
      {
        headers.add
        (
          s.substring(0, s.indexOf(':')).trim(),
          s.substring(s.indexOf(':') + 1).trim()
        );
      }
    }

    return headers;
  }



  public static String
  resolveFullPath(String basePath, String relativePath)
    throws MalformedURLException
  {
    return
      resolveFullPath
      (
        relativePath.startsWith("/") ?
          relativePath :
          (basePath.substring(0, basePath.lastIndexOf('/') + 1) + relativePath)
      );
  }



  /**
   * Resolves ".." segements and multiple slashes.
   */

  public static String
  resolveFullPath(String path) throws MalformedURLException
  {
    List		segments = new ArrayList();
    StringTokenizer	tokenizer = new StringTokenizer(path, "/");

    while (tokenizer.hasMoreTokens())
    {
      String	token = tokenizer.nextToken();

      if (!token.equals("."))
      {
        if (token.equals(".."))
        {
          if (segments.size() == 0)
          {
            throw
              new MalformedURLException
              (
                "URL specification goes outside of its root"
              );
          }

          segments.remove(segments.size() - 1);
        }
        else
        {
          segments.add(token);
        }
      }
    }

    String	result = "";

    for
    (
      Iterator i = segments.iterator();
      i.hasNext();
      result += "/" + (String) i.next()
    );

    return result.equals("") ? "/" : (result + (path.endsWith("/") ? "/" : ""));
  }



  public static URL
  resolvePath(URL url) throws MalformedURLException
  {
    if (isComposedUrl(url))
    {
      try
      {
        String	path = resolveFullPath(extractComposedUrlEntry(url));

        return
          new URL
          (
            url.getProtocol() + ":" +
              resolvePath(extractSubUrl(url)).toString() + "!" + path
          );
      }

      catch (MalformedURLException e)
      {
        // This considers a jar URL to be an ordinary directory.

        return
          resolvePath
          (
            new URL
            (
              extractSubUrl(url).toString() + "/" + extractComposedUrlEntry(url)
            )
          );
      }
    }

    String	path = resolveFullPath(url.getFile());

    return
      new URL
      (
        new URL
        (
          url.getProtocol() + ":" +
            (url.getAuthority() != null ? "//" + url.getAuthority() : "")
        ),
        path + (url.getRef() != null ? ("#" + url.getRef()) : "")
      );
  }



  public static URL
  setScheme(URL url, String scheme) throws MalformedURLException
  {
    try
    {
      return
        escapedUrl
        (
          new URI
          (
            scheme,
            unescapeUriSpecials(url.getUserInfo()),
              // Avoid escaping escapes by URI.
            unescapeUriSpecials(url.getHost()),
              // Avoid escaping escapes by URI.
            url.getPort(),
            unescapeUriSpecials(url.getPath()),
              // Avoid escaping escapes by URI.
            unescapeUriSpecials(url.getQuery()),
              // Avoid escaping escapes by URI.
            unescapeUriSpecials(url.getRef())
              // Avoid escaping escapes by URI.
          ).toString()
        );
    }

    catch (URISyntaxException e)
    {
      throw new MalformedURLException(e.getMessage());
    }
  }



  public static URL
  setUserInfo(URL url, String userInfo) throws MalformedURLException
  {
    try
    {
      return
        escapedUrl
        (
          new URI
          (
            url.getProtocol(),
            unescapeUriSpecials(userInfo), // Avoid escaping escapes by URI.
            unescapeUriSpecials(url.getHost()),
              // Avoid escaping escapes by URI.
            url.getPort(),
            unescapeUriSpecials(url.getPath()),
              // Avoid escaping escapes by URI.
            unescapeUriSpecials(url.getQuery()),
              // Avoid escaping escapes by URI.
            unescapeUriSpecials(url.getRef())
              // Avoid escaping escapes by URI.
          ).toString()
        );
    }

    catch (URISyntaxException e)
    {
      throw new MalformedURLException(e.getMessage());
    }
  }



  public static String
  stripQuery(String url)
  {
    int	query = url.lastIndexOf('?');
    int	reference = -1;

    return
      query != -1 ?
        (
          url.substring(0, query) +
            (
              (reference = url.lastIndexOf('#')) != -1 ?
                url.substring(reference) : ""
            )
        ) : url;
  }



  public static URL
  stripQuery(URL url) throws MalformedURLException
  {
    return new URL(stripQuery(url.toString()));
  }



  public static URL
  stripUserInfo(URL url)
  {
    try
    {
      return
        new URL
        (
          url.getProtocol() + ":" +
            (url.getHost() != null ? "//" + url.getHost() : "") +
            (url.getPort() != -1 ?  (":" + String.valueOf(url.getPort())) : "")
            + url.getFile() + (url.getRef() != null ? ("#" + url.getRef()) : "")
        );
    }

    catch (MalformedURLException e)
    {
      throw new RuntimeException(e); // Would be a bug.
    }
  }



  public static void
  testUrl(URL url) throws IOException
  {
    try
    {
      url.openStream().close();
    }

    catch (Exception e)
    {
      throw
        new be.re.io.IOException
        (
          "The URL \"" + url.toString() + "\" can't be opened.",
          e
        );
    }
  }



  public static String
  unescapeUriSpecials(String s)
  {
    if (s == null)
    {
      return null;
    }

    int		count = 0;
    byte[]	result = new byte[s.length()];

    for (int i = 0; i < s.length(); ++i)
    {
      if
      (
        s.charAt(i) == '%'						&&
        s.length() - i > 2						&&
        be.re.util.Util.isInteger(s.substring(i + 1, i + 3), 16)
      )
      {
        result[count++] =
          (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
        i += 2;
      }
      else
      {
        result[count++] = (byte) s.charAt(i);
      }
    }

    try
    {
      return new String(result, 0, count, "UTF-8");
    }

    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e); // Would be a bug.
    }
  }



  public static File
  urlToFile(URL url)
  {
    return new File(unescapeUriSpecials(url.getFile()));
  }



  public static User
  userFromUrl(URL url)
  {
    User	user = new BasicUser();
    String	userInfo = url.getUserInfo();

    if (userInfo == null)
    {
      return user;
    }

    StringTokenizer	tokenizer = new StringTokenizer(userInfo, ":");

    if (tokenizer.hasMoreTokens())
    {
      user.setUsername(tokenizer.nextToken());
    }

    if (tokenizer.hasMoreTokens())
    {
      user.setPassword(unescapeUriSpecials(tokenizer.nextToken()));
    }

    return user;
  }



  private interface TestChar

  {

    /**
     * Return <code>true</code> if the character should not be escaped and
     * <code>false</code> otherwise.
     */

    public boolean	test	(char[] c, int i);

  } // TestChar

} // Util
