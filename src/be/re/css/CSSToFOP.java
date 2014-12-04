package be.re.css;

import be.re.xml.sax.ProtectEventHandlerFilter;
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
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Options;
import org.apache.fop.messaging.MessageHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;



/**
 * Convenience class for the conversion from CSS to the FOP XSL-FO formatter.
 * @author Werner Donn\u00e9
 */

public class CSSToFOP

{

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
      false,
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
      false,
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
    boolean		quiet,
    boolean		validate
  ) throws IOException, CSSToXSLFOException
  {
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

      Driver	driver = new Driver();

      MessageHandler.setScreenLogger
      (
        quiet ?
          (Logger) new NullLogger() :
          (Logger) new ConsoleLogger(ConsoleLogger.LEVEL_INFO)
      );

      driver.setLogger
      (
        quiet ?
          (Logger) new NullLogger() :
          (Logger) new ConsoleLogger(ConsoleLogger.LEVEL_INFO)
      );

      driver.setRenderer(format);

      if (out != null)
      {
        driver.setOutputStream(out);
      }

      TransformerHandler        handler =
        be.re.xml.sax.Util.newSAXTransformerFactory().newTransformerHandler
        (
          new StreamSource
          (
            CSSToFOP.class.getResourceAsStream("style/fop_filter.xsl")
          )
        );

      handler.setResult(new SAXResult(driver.getContentHandler()));
      filter.setContentHandler(handler);
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
    File	configFile = null;
    int		format = -1;
    String	output = null;
    Map		parameters = new HashMap();
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
          if (args[i].equals("-q"))
          {
            quiet = true;
          }
          else
          {
            if (args[i].equals("-pdf"))
            {
              if (i == args.length - 1 || format != -1)
              {
                usage(1);
              }
  
              format = Driver.RENDER_PDF;
              output = args[++i];
            }
            else
            {
              if (args[i].equals("-ps"))
              {
                if (i == args.length - 1 || format != -1)
                {
                  usage(1);
                }
  
                format = Driver.RENDER_PS;
                output = args[++i];
              }
              else
              {
                if (args[i].equals("-svg"))
                {
                  if (i == args.length - 1 || format != -1)
                  {
                    usage(1);
                  }
  
                  format = Driver.RENDER_SVG;
                  output = args[++i];
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
                        if (args[i].equals("-fc"))
                        {
                          if (i == args.length - 1)
                          {
                            usage(1);
                          }
          
                          configFile = new File(args[++i]);
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
    }

    if (format == -1)
    {
      usage(1);
    }

    if (configFile != null)
    {
      new Options(configFile);
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
        output != null ? new FileOutputStream(output) : null,
        baseUrl != null ? baseUrl : url,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToFOP.class.getResource("/catalog"),
        parameters,
        preprocessors,
        format,
        quiet,
        validate
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
    System.err.println("Usage: be.re.css.CSSToFOP");
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
    System.err.
      println("  (-pdf filename | -ps filename | -svg filename): output file");
    System.err.println();
    Util.printUserAgentParameters(System.err);
    System.exit(code);
  }

} // CSSToFOP
