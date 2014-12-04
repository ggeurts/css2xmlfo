package be.re.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Pattern;



/**
 * @author Werner Donn\u00e9
 */

public class Util

{

  private static final String[][]	DATE_FORMATS =
    {
      {"yyyy.MM.dd h:mm:ss a", "0"},
      {"yyyy.MM.dd h:mm a", "0"},
      {"yyyy.MM.dd HH:mm", "0"},
      {"yyyy.MM.dd HH:mm:ss", "0"},
      {"dd.MM.yyyy h:mm a", "0"},
      {"dd.MM.yyyy h:mm:ss a", "0"},
      {"dd.MM.yyyy HH:mm", "0"},
      {"dd.MM.yyyy HH:mm:ss", "0"},
      {"yyyy/MM/dd h:mm a", "0"},
      {"yyyy/MM/dd h:mm:ss a", "0"},
      {"yyyy/MM/dd HH:mm", "0"},
      {"yyyy/MM/dd HH:mm:ss", "0"},
      {"dd/MM/yyyy h:mm a", "0"},
      {"dd/MM/yyyy h:mm:ss a", "0"},
      {"dd/MM/yyyy HH:mm", "0"},
      {"dd/MM/yyyy HH:mm:ss", "0"},
      {"yyyy-MM-dd h:mm a", "0"},
      {"yyyy-MM-dd h:mm:ss a", "0"},
      {"yyyy-MM-dd HH:mm", "0"},
      {"yyyy-MM-dd HH:mm:ss", "0"},
      {"yyyy-MM-dd'T'h:mm a", "0"},
      {"yyyy-MM-dd'T'h:mm:ss a", "0"},
      {"yyyy-MM-dd'T'HH:mm", "0"},
      {"yyyy-MM-dd'T'HH:mm:ss", "0"},
      {"yyyy-MM-dd'T'HH:mm:ss.S", "0"},
      {"yyyy-MM-dd'T'HH:mm:ssZ", "0"},
      {"yyyy-MM-dd'T'HH:mm:ss.SZ", "0"},
      {"dd-MM-yyyy h:mm a", "0"},
      {"dd-MM-yyyy h:mm:ss a", "0"},
      {"dd-MM-yyyy HH:mm", "0"},
      {"dd-MM-yyyy HH:mm:ss", "0"},
      {"h:mm a", "1"},
      {"h:mm:ss a", "1"},
      {"HH:mm", "1"},
      {"HH:mm:ss", "1"},
      {"yyyy.MM.dd", "2"},
      {"dd.MM.yyyy", "2"},
      {"yyyy/MM/dd", "2"},
      {"dd/MM/yyyy", "2"},
      {"yyyy-MM-dd", "2"},
      {"dd-MM-yyyy", "2"}
    };

  private static Map		bundles = new HashMap();
  private static Set		countries =
    new HashSet(Arrays.asList(Locale.getISOCountries()));
  private static Set		languages =
    new HashSet(Arrays.asList(Locale.getISOLanguages()));
  private static Pattern	timeZonePattern =
    Pattern.compile("\\p{Digit}{2}:?\\p{Digit}{2}");
  private static Pattern	timestampPattern =
    Pattern.compile
    (
      "\\p{Digit}{4}(-\\p{Digit}{2}(-\\p{Digit}{2}" +
        "(T\\p{Digit}{2}:\\p{Digit}{2}(:\\p{Digit}{2}(.(\\p{Digit})+)?)?" +
        "(Z|((\\+|-)\\p{Digit}{2}:?\\p{Digit}{2}))?)?)?)?"
    );



  public static void
  attemptExitVM(int returnCode)
  {
    if
    (
      Util.class.getClassLoader() == null		||
        Util.class.getClassLoader().
          equals(ClassLoader.getSystemClassLoader())
    )
    {
      try
      {
        System.exit(returnCode);
      }

      catch (Throwable e)
      {
        // Only attempt.
      }
    }
  }



  public static String
  createDate(long time)
  {
    return new SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
  }



  public static Object[]
  createSet(Object[] collection)
  {
    Set	set = new HashSet();

    for (int i = 0; i < collection.length; ++i)
    {
      set.add(collection[i]);
    }

    return
      set.toArray
      (
        (Object[])
          Array.newInstance
          (
            collection.getClass().getComponentType(),
            set.size()
          )
      );
  }



  public static String
  createTimestamp(long time)
  {
    return
      new SimpleDateFormat
      (
        time % 1000 > 0 ?
          "yyyy-MM-dd'T'HH:mm:ss.SSSZ" : "yyyy-MM-dd'T'HH:mm:ssZ"
      ).format(new Date(time));
  }



  public static String
  createTimestampUTC(long time)
  {
    SimpleDateFormat	format =
      new SimpleDateFormat
      (
        time % 1000 > 0 ?
          "yyyy-MM-dd'T'HH:mm:ss.SSS" : "yyyy-MM-dd'T'HH:mm:ss"
      );

    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    return format.format(new Date(time)) + "Z";
  }



  public static String
  enumerate(String[] s, String separator)
  {
    StringBuffer	result = new StringBuffer(128);

    for (int i = 0; i < s.length; ++i)
    {
      if (i > 0)
      {
        result.append(separator);
      }

      result.append(s[i]);
    }

    return result.toString();
  }



  private static String[]
  findFormat(String value)
  {
    for (int i = 0; i < DATE_FORMATS.length; ++i)
    {
      SimpleDateFormat	format = new SimpleDateFormat(DATE_FORMATS[i][0]);

      format.setLenient(false);

      try
      {
        if (format.parse(value) != null)
        {
          return DATE_FORMATS[i];
        }
      }

      catch (ParseException e)
      {
      }
    }

    return null;
  }



  public static Throwable
  getCause(Throwable t, Class theClass)
  {
    return
      t == null ?
        null :
        (
          theClass.isAssignableFrom(t.getClass()) ?
            t : getCause(t.getCause(), theClass)
        );
  }



  public static String
  getCommonPrefix(String s1, String s2)
  {
    for (int i = 0; i < s1.length() && i < s2.length(); ++i)
    {
      if (s1.charAt(i) != s2.charAt(i))
      {
        return s1.substring(0, i);
      }
    }

    return s1.length() <= s2.length() ? s1 : s2;
  }



  public static String
  getCommonPrefix(String[] s)
  {
    if (s.length == 0)
    {
      return "";
    }

    String	result = s[0];

    for (int i = 1; i < s.length; ++i)
    {
      result = getCommonPrefix(result, s[i]);
    }

    return result;
  }



  public static String
  getLanguageTag(Locale locale)
  {
    return
      locale.getLanguage().toLowerCase() +
        (
          "".equals(locale.getCountry()) ?
            "" : ("-" + locale.getCountry().toUpperCase())
        );
  }



  public static Locale
  getLocale(String languageTag)
  {
    return
      languageTag.indexOf('-') != -1 ?
        new Locale
        (
          languageTag.substring(0, languageTag.indexOf('-')).toLowerCase(),
          languageTag.substring(languageTag.indexOf('-') + 1).toUpperCase()
        ) : new Locale(languageTag.toLowerCase());
  }



  public static File
  getPackageStorage(Class c)
  {
    File		current = new File(System.getProperty("user.home"));
    boolean		first = true;
    StringTokenizer	tokenizer = new StringTokenizer(c.getName(), ".");
    int			count = tokenizer.countTokens();

    for (int i = 0; i < count - 1; ++i)
    {
      current = new File(current, (first ? "." : "") + tokenizer.nextToken());
      first = false;
    }

    if (!current.exists())
    {
      current.mkdirs();
    }

    return current;
  }



  public static String[]
  getPathSegments(String path)
  {
    return split(path, "/");
  }



  public static String
  getReSystemProperty(String name) throws IOException
  {
    InputStream	in =
      Util.class.getClassLoader().
        getResourceAsStream("META-INF/services/be/re/" + name);

    if (in != null)
    {
      try
      {
        String[]      resource = readLineConfig(in);

        if (resource.length == 1)
        {
          return resource[0];
        }
      }

      finally
      {
        in.close();
      }
    }

    return null;
  }



  public static String
  getResource(String name)
  {
    return getResource(name, Locale.getDefault());
  }



  public static String
  getResource(String name, Locale locale)
  {
    return getResourceBundle(locale).getString(name);
  }



  public static ResourceBundle
  getResourceBundle()
  {
    return getResourceBundle(Locale.getDefault());
  }



  public static ResourceBundle
  getResourceBundle(Locale locale)
  {
    ResourceBundle	bundle = (ResourceBundle) bundles.get(locale);

    if (bundle == null)
    {
      bundle = ResourceBundle.getBundle("be.re.util.res.Res", locale);
      bundles.put(locale, bundle);
    }

    return bundle;
  }



  public static String
  getStackTrace()
  {
    try
    {
      throw new Exception("");
    }

    catch (Exception e)
    {
      StringWriter	writer = new StringWriter();

      e.printStackTrace(new PrintWriter(writer));

      return writer.toString();
    }
  }



  public static String
  getSystemProperty(String propertyName) throws IOException
  {
    String	result = System.getProperty(propertyName);

    if (result == null)
    {
      InputStream     in =
        Util.class.getClassLoader().
          getResourceAsStream("META-INF/services/" + propertyName);

      if (in != null)
      {
        String[]      resource = readLineConfig(in);

        if (resource.length == 1)
        {
          result = resource[0];
        }
      }
    }

    return result;
  }



  public static TimeZone
  getTimeZone(String s)
  {
    return getTimeZone(s, getTimeZoneOffset(s));
  }



  private static TimeZone
  getTimeZone(String s, int offset)
  {
    if (offset == -1)
    {
      return TimeZone.getDefault();
    }

    s = s.substring(offset);

    return
      TimeZone.getTimeZone
      (
        "Z".equals(s) ? "GMT" : (s.startsWith("GMT") ? s : ("GMT" + s))
      );
  }



  private static int
  getTimeZoneOffset(String s)
  {
    int	index;

    return
      s.charAt(s.length() - 1) == 'Z' ?
        (s.length() - 1) :
        (
          (index = s.lastIndexOf("GMT")) != -1 ?
            index :
            (
              (
                (index = s.lastIndexOf('+')) != -1 ||
                  (index = s.lastIndexOf('-')) != -1
              ) && timeZonePattern.matcher(s.substring(index + 1)).matches() ?
                index : -1
            )
        );
  }



  public static boolean
  isDateSet(String dateTime)
  {
    return isDateSet(findFormat(dateTime));
  }



  private static boolean
  isDateSet(String[] format)
  {
    return format != null && !format[1].equals("1");
  }



  public static boolean
  isDateTime(String s)
  {
    return findFormat(s) != null;
  }



  public static boolean
  isDouble(String s)
  {
    try
    {
      Double.parseDouble(s);

      return true;
    }

    catch (NumberFormatException e)
    {
      return false;
    }
  }



  public static boolean
  isFloat(String s)
  {
    try
    {
      Float.parseFloat(s);

      return true;
    }

    catch (NumberFormatException e)
    {
      return false;
    }
  }



  public static boolean
  isInteger(String s)
  {
    try
    {
      Integer.parseInt(s);

      return true;
    }

    catch (NumberFormatException e)
    {
      return false;
    }
  }



  public static boolean
  isInteger(String s, int radix)
  {
    try
    {
      Integer.parseInt(s, radix);

      return true;
    }

    catch (NumberFormatException e)
    {
      return false;
    }
  }



  public static boolean
  isJCEInstalled()
  {
    try
    {
      Class.forName("javax.crypto.Cipher").
        getMethod("getInstance", new Class[] {String.class}).
          invoke(null, new Object[] {"PBEWithMD5AndDES/CBC/PKCS5Padding"});
      return true;
    }

    catch (Throwable e)
    {
      return false;
    }
  }



  /**
   * Supports only the primary and country subtags. (See also RFC 4646.)
   */

  public static boolean
  isLanguageTag(String s)
  {
    Locale	locale = getLocale(s);

    return
      languages.contains(locale.getLanguage().toLowerCase()) &&
        (
          "".equals(locale.getCountry()) ||
            countries.contains(locale.getCountry().toUpperCase())
        );
  }



  public static boolean
  isLong(String s)
  {
    try
    {
      Long.parseLong(s);

      return true;
    }

    catch (NumberFormatException e)
    {
      return false;
    }
  }



  public static boolean
  isLong(String s, int radix)
  {
    try
    {
      Long.parseLong(s, radix);

      return true;
    }

    catch (NumberFormatException e)
    {
      return false;
    }
  }



  public static boolean
  isMac()
  {
    return System.getProperty("mrj.version") != null;
  }



  public static boolean
  isTimeSet(long time)
  {
    Calendar	calendar = Calendar.getInstance();

    calendar.setTimeInMillis(time);

    return
      calendar.get(Calendar.HOUR_OF_DAY) > 0 ||
        calendar.get(Calendar.MINUTE) > 0 ||
        calendar.get(Calendar.SECOND) > 0 ||
        calendar.get(Calendar.MILLISECOND) > 0;
  }



  public static boolean
  isTimeSet(String dateTime)
  {
    return isTimeSet(findFormat(dateTime));
  }



  private static boolean
  isTimeSet(String[] format)
  {
    return format != null && !format[1].equals("2");
  }



  public static boolean
  isTimestamp(String s)
  {
    try
    {
      return parseTimestamp(s) != -1;
    }

    catch (Exception e)
    {
      return false;
    }
  }



  public static boolean
  isWindows()
  {
    return System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;
  }



  public static String
  list(String[] list, String separator)
  {
    StringBuilder	builder = new StringBuilder(256);

    for (int i = 0; i < list.length; ++i)
    {
      if (i > 0)
      {
        builder.append(separator);
      }

      builder.append(list[i]);
    }

    return builder.toString();
  }



  /**
   * If the date is not set the current date will be used. If the time is not
   * set midnight will be used.
   */

  public static long
  parseDateTime(String s)
  {
    String[]	format = findFormat(s);

    if (format == null)
    {
      return -1;
    }

    long	time = parseTimestamp(s, new String[]{format[0]});

    if (isDateSet(format) && isTimeSet(format))
    {
      return time;
    }

    if (!isDateSet(format))
    {
      Calendar	calendar = Calendar.getInstance();
      Calendar	now = Calendar.getInstance();

      calendar.setTimeInMillis(time);
      now.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
      calendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
      calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
      time = calendar.getTime().getTime();
    }

    if (!isTimeSet(format))
    {
      time = setTime(time, 0, 0, 0);
    }

    return time;
  }



  /**
   * Parse an ISO-8601 date. Returns <code>-1</code> if the date couldn't be
   * parsed.
   */

  public static long
  parseTimestamp(String s)
  {
    if (!timestampPattern.matcher(s).matches())
    {
      return -1;
    }

    int		timeZoneOffset = getTimeZoneOffset(s);
    Calendar	calendar = Calendar.getInstance(getTimeZone(s, timeZoneOffset));

    calendar.setLenient(false);
    calendar.setTimeInMillis(0);
    calendar.set(Calendar.YEAR, Integer.parseInt(s.substring(0, 4)));

    if (s.length() > 4)
    {
      calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(5, 7)) - 1);

      if (s.length() > 7)
      {
        calendar.
          set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(8, 10)));

        if (s.length() > 10)
        {
          calendar.
            set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(11, 13)));
          calendar.set(Calendar.MINUTE, Integer.parseInt(s.substring(14, 16)));

          if (s.length() > 16 && s.charAt(16) == ':')
          {
            calendar.
              set(Calendar.SECOND, Integer.parseInt(s.substring(17, 19)));

            if (s.length() > 19 && s.charAt(19) == '.')
            {
              calendar.set
              (
                Calendar.MILLISECOND,
                Math.round
                (
                  Float.parseFloat
                  (
                    "0." +
                      s.substring
                      (
                        20,
                        timeZoneOffset != -1 ? timeZoneOffset : s.length()
                      )
                  ) * 1000
                )
              );
            }
          }
        }
      }
    }

    return calendar.getTimeInMillis();
  }



  public static long
  parseTimestamp(String s, String[] patterns)
  {
    Date	result = null;

    for (int i = 0; i < patterns.length && result == null; ++i)
    {
      try
      {
        SimpleDateFormat	format = new SimpleDateFormat(patterns[i]);

        format.setLenient(false);
        result = format.parse(s);
      }

      catch (ParseException e)
      {
      }
    }

    return result == null ? -1 : result.getTime();
  }



  public static String
  patternToRegexp(String pattern)
  {
    return
      pattern.replaceAll("\\+", "\\+").replaceAll("\\.", "\\.").
        replaceAll("\\$", "\\$").replaceAll("\\*", ".*");
  }



  public static void
  printStackTrace(Throwable e)
  {
    if (System.getProperty("be.re.stack") != null)
    {
      e.printStackTrace();
    }
  }



  public static String[]
  readLineConfig(InputStream in) throws IOException
  {
    return readLineConfig(new BufferedReader(new InputStreamReader(in)));
  }



  public static String[]
  readLineConfig(BufferedReader in) throws IOException
  {
    String	line = "";
    List	result = new ArrayList();
    String	s;

    while ((s = in.readLine()) != null)
    {
      if (s.indexOf('#') != -1)
      {
        s = s.substring(0, s.indexOf('#'));
      }

      s = s.trim();

      if (s.length() > 0)
      {
        if (s.charAt(s.length() - 1) == '\\')
        {
          line += s.substring(0, s.length() - 1);
        }
        else
        {
          line += s;
          result.add(line);
          line = "";
        }
      }
    }

    return (String[]) result.toArray(new String[result.size()]);
  }



  public static long
  setTime(long date, int hourOfDay, int minute, int second)
  {
    Calendar	calendar = Calendar.getInstance();

    calendar.setTimeInMillis(date);
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, second);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar.getTime().getTime();
  }



  public static String[]
  split(String s, String separator)
  {
    List		result = new ArrayList();
    StringTokenizer	tokenizer = new StringTokenizer(s, separator);

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



  public static int
  waitFor(Process process)
  {
    while (true)
    {
      try
      {
        return process.waitFor();
      }

      catch (InterruptedException e)
      {
      }
    }
  }

} // Util
