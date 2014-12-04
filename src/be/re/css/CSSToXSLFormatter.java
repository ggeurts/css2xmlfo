package be.re.css;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import jp.co.antenna.XfoJavaCtl.XfoObj;



/**
 * Convenience class for the conversion from CSS to the XSLFormatter XSL-FO
 * formatter.
 * @author Werner Donn\u00e9
 */

public class CSSToXSLFormatter

{

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

    File	f = File.createTempFile("be.re.css.", "css2xslformatter");

    f.deleteOnExit();

    try
    {
      CSSToXSLFO.convert
      (
        url != null ? url.openStream() : System.in,
        new FileOutputStream(f),
        baseUrl != null ? baseUrl : url,
        userAgentStyleSheet,
        catalog != null ?
          catalog : CSSToXSLFormatter.class.getResource("/catalog"),
        parameters,
        preprocessors,
        validate,
        System.getProperty("be.re.css.debug") != null
      );
    }

    catch (Throwable e)
    {
      System.err.println(e.getMessage());
      be.re.util.Util.printStackTrace(e);
    }

    XfoObj	o = new XfoObj();

    if (configuration != null)
    {
      o.addOptionFileURI(configuration.toString());
    }

    o.render(new FileInputStream(f), new FileOutputStream(pdf));
  }



  private static void
  usage(int code)
  {
    System.err.println("Usage: be.re.css.CSSToXSLFormatter");
    System.err.println("  [-h]: show this help");
    System.err.println("  [-baseurl url]: base URL ");
    System.err.println("  [-c url_or_filename]: catalog for entity resolution");
    System.err.println("  [-config url_or_filename]: extra configuration");
    System.err.println("  [-p url_or_filename_comma_list]: preprocessors");
    System.err.println("  [-uacss url_or_filename]: User Agent style sheet");
    System.err.println("  [-v]: turn on validation");
    System.err.
      println("  [url_or_filename]: the input document, uses stdin by default");
    System.err.println("  [parameter=value ...] ");
    System.err.println("  -pdf filename: output file");
    System.err.println();
    Util.printUserAgentParameters(System.err);
    System.exit(code);
  }

} // CSSToXSLFormatter
