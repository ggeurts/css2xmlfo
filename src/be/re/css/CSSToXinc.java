package be.re.css;

import be.re.xml.sax.ProtectEventHandlerFilter;
import com.lunasil.xf.XincEngine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;



/**
 * Convenience class for the conversion from CSS to the Xinc XSL-FO formatter.
 * @author Werner Donn\u00e9
 */

public class CSSToXinc

{

  public static final int	PDF = 0;



  public static void
  convert(URL in, OutputStream out, URL userAgentStyleSheet, int format)
    throws IOException, CSSToXSLFOException
  {
    convert
    (
      in.openStream(),
      out,
      in,
      userAgentStyleSheet,
      null,
      new HashMap(),
      null,
      format,
      false
    );
  }



  public static void
  convert(InputStream in, OutputStream out, int format)
    throws IOException, CSSToXSLFOException
  {
    convert(in, out, null, format);
  }



  public static void
  convert(InputStream in, OutputStream out, URL userAgentStyleSheet, int format)
    throws IOException, CSSToXSLFOException
  {
    convert
    (
      in,
      out,
      null,
      userAgentStyleSheet,
      null,
      new HashMap(),
      null,
      format,
      false
    );
  }



  public static void
  convert
  (
    InputStream		in,
    OutputStream	out,
    URL			baseUrl,
    URL			userAgentStyleSheet,
    URL			catalog,
    Map			parameters,
    URL[]		preprocessors,
    int			format,
    boolean		validate
  ) throws IOException, CSSToXSLFOException
  {
    convert
    (
      in,
      out,
      baseUrl,
      userAgentStyleSheet,
      catalog,
      parameters,
      preprocessors,
      format,
      validate,
      null
    );
  }



  public static void
  convert
  (
    InputStream		in,
    OutputStream	out,
    URL			baseUrl,
    URL			userAgentStyleSheet,
    URL			catalog,
    Map			parameters,
    URL[]		preprocessors,
    int			format,
    boolean		validate,
    URL			configuration
  ) throws IOException, CSSToXSLFOException
  {
    if (format != PDF)
    {
      throw
        new IllegalArgumentException
        (
          "be.re.css.CSSToXinc.convert takes only be.re.css.CSSToXinc.PDF " +
            "as a format value."
        );
    }

    try
    {
      XMLReader	parser = be.re.xml.sax.Util.getParser(catalog, validate);
      XMLFilter	parent = new ProtectEventHandlerFilter(true, true, parser);

      if (preprocessors != null)
      {
        parent = Util.createPreprocessorFilter(preprocessors, parent);
      }

      XMLFilter	filter =
        new CSSToXSLFOFilter
        (
          baseUrl,
          userAgentStyleSheet,
          parameters,
          parent,
          System.getProperty("be.re.css.debug") != null
        );

      InputSource	source = new InputSource(in);

      if (baseUrl != null)
      {
        source.setSystemId(baseUrl.toString());
      }

      XincEngine	engine = new XincEngine();

      if (configuration != null)
      {
        try
        {
          engine.setConfiguration
          (
            be.re.xml.Util.newDocumentBuilderFactory(false).
              newDocumentBuilder().parse(configuration.toString())
          );
        }

        catch (IOException e)
        {
          throw e;
        }

        catch (Exception e)
        {
          throw new be.re.io.IOException(e);
        }
      }

      engine.setOutputStream(out);
      filter.setContentHandler(engine.createContentHandler());
      filter.parse(source);
    }

    catch (Exception e)
    {
      throw new CSSToXSLFOException(e);
    }
  }



  public static void
  main(String[] args) throws Exception
  {
    URL		baseUrl = null;
    URL		catalog = null;
    URL		configuration = null;
    String	pdf = null;
    Map		parameters = new HashMap();
    URL[]	preprocessors = null;
    URL		url = null;
    URL		userAgentStyleSheet = null;
    boolean	validate = false;

    for (int i = 0; i < args.length; ++i)
    {
      if (args[i].equals("-h"))
      {
        usage(0);
      }

      if (args[i].equals("-baseurl"))
      {
        if (i == args.length - 1)
        {
          usage(1);
        }

        baseUrl = Util.createUrl(args[++i]);
      }
      else
      {
        if (args[i].equals("-uacss"))
        {
          if (i == args.length - 1)
          {
            usage(1);
          }

          userAgentStyleSheet = Util.createUrl(args[++i]);
        }
        else
        {
          if (args[i].equals("-pdf"))
          {
            if (i == args.length - 1)
            {
              usage(1);
            }

            pdf = args[++i];
          }
          else
          {
            if (args[i].equals("-c"))
            {
              if (i == args.length - 1)
              {
                usage(1);
              }

              catalog = Util.createUrl(args[++i]);
            }
            else
            {
              if (args[i].equals("-p"))
              {
                if (i == args.length - 1)
                {
                  usage(1);
                }

                preprocessors = Util.createUrls(args[++i]);
              }
              else
              {
                if (args[i].equals("-v"))
                {
                  validate = true;
                }
                else
                {
                  if (args[i].equals("-config"))
                  {
                    if (i == args.length - 1)
                    {
                      usage(1);
                    }

                    configuration = Util.createUrl(args[++i]);
                  }
                  else
                  {
                    if (args[i].indexOf('=') != -1)
                    {
                      parameters.put
                      (
                        args[i].substring(0, args[i].indexOf('=')),
                        args[i].indexOf('=') == args[i].length() - 1 ?
                          "" : args[i].substring(args[i].indexOf('=') + 1)
                      );
                    }
                    else
                    {
                      if (url != null)
                      {
                        usage(1);
                      }

                      url = Util.createUrl(args[i]);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (pdf == null)
    {
      usage(1);
    }

    parameters.put
    (
      "base-url",
      baseUrl != null ?
        baseUrl.toString() : (url != null ? url.toString() : "")
    );

    try
    {
      convert
      (
        url != null ? url.openStream() : System.in,
        new FileOutputStream(pdf),
        baseUrl != null ? baseUrl : url,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToXinc.class.getResource("/catalog"),
        parameters,
        preprocessors,
        PDF,
        validate,
        configuration
      );
    }

    catch (Throwable e)
    {
      System.err.println(e.getMessage());
      be.re.util.Util.printStackTrace(e);
    }
  }



  private static void
  usage(int code)
  {
    System.err.println("Usage: be.re.css.CSSToXinc");
    System.err.println("  [-h]: show this help");
    System.err.println("  [-baseurl url]: base URL ");
    System.err.println("  [-c url_or_filename]: catalog for entity resolution");
    System.err.println("  [-config url_or_filename]: extra configuration");
    System.err.println("  [-p url_or_filename_comma_list]: preprocessors");
    System.err.println("  [-uacss url_or_filename]: User Agent style sheet");
    System.err.println("  [-v]: turn on validation");
    System.err.
      println("  [url_or_filename]: the input document, use stdin by default");
    System.err.println("  [parameter=value ...] ");
    System.err.println("  -pdf filename: output file");
    System.err.println();
    Util.printUserAgentParameters(System.err);
    System.exit(code);
  }

} // CSSToXinc
