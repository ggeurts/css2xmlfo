package be.re.css;

import be.re.xml.sax.ProtectEventHandlerFilter;
import com.renderx.xep.FOTarget;
import com.renderx.xep.FormatterImpl;
import com.renderx.xep.lib.DefaultLogger;
import com.renderx.xep.lib.Logger;
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
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;



/**
 * Convenience class for the conversion from CSS to the XEP XSL-FO formatter.
 * @author Werner Donn\u00e9
 */

public class CSSToXEP

{

  public static final int	PDF = 0;
  public static final int	POSTSCRIPT = 1;



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
      false,
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
    boolean		quiet,
    URL			configuration
  ) throws IOException, CSSToXSLFOException
  {
    if (format != PDF && format != POSTSCRIPT)
    {
      throw
        new IllegalArgumentException
        (
          "be.re.css.CSSToXEP.convert takes only be.re.css.CSSToXEP.PDF " +
            "or be.re.css.CSSToXEP.POSTSCRIPT as format values."
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

      if (configuration != null)
      {
        new FormatterImpl
        (
          new StreamSource(configuration.toString())
        ).render
        (
          new SAXSource(filter, source),
          new FOTarget(out, format == PDF ? "PDF" : "PostScript"),
          quiet ? Logger.NULL_LOGGER : new DefaultLogger()
        );
      }
      else
      {
        new FormatterImpl().render
        (
          new SAXSource(filter, source),
          new FOTarget(out, format == PDF ? "PDF" : "PostScript"),
          quiet ? Logger.NULL_LOGGER : new DefaultLogger()
        );
      }
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
    String	postScript = null;
    URL[]	preprocessors = null;
    boolean	quiet = false;
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
            if (args[i].equals("-ps"))
            {
              if (i == args.length - 1)
              {
                usage(1);
              }

              postScript = args[++i];
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
                      if (args[i].equals("-q"))
                      {
                        quiet = true;
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
      }
    }

    if
    (
      (
        pdf == null		&&
        postScript == null
      )				||
      (
        pdf != null		&&
        postScript != null
      )
    )
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
        new FileOutputStream(pdf != null ? pdf : postScript),
        baseUrl != null ? baseUrl : url,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToXEP.class.getResource("/catalog"),
        parameters,
        preprocessors,
        pdf != null ? PDF : POSTSCRIPT,
        validate,
        quiet,
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
    System.err.println("Usage: be.re.css.CSSToXEP");
    System.err.println("  [-h]: show this help");
    System.err.println("  [-baseurl url]: base URL ");
    System.err.println("  [-c url_or_filename]: catalog for entity resolution");
    System.err.println("  [-config url_or_filename]: extra configuration");
    System.err.println("  [-p url_or_filename_comma_list]: preprocessors");
    System.err.println("  [-q]: quiet mode");
    System.err.println("  [-uacss url_or_filename]: User Agent style sheet");
    System.err.println("  [-v]: turn on validation");
    System.err.
      println("  [url_or_filename]: the input document, uses stdin by default");
    System.err.println("  [parameter=value ...] ");
    System.err.println("  (-pdf filename | -ps filename): output file");
    System.err.println();
    Util.printUserAgentParameters(System.err);
    System.exit(code);
  }

} // CSSToXEP
