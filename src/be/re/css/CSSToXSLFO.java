package be.re.css;

import be.re.xml.sax.ProtectEventHandlerFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;



/**
 * Convenience class for the conversion from CSS to XSL-FO.
 * @author Werner Donn\u00e9
 */

public class CSSToXSLFO

{

  /**
   * Takes in an XML document and produces an XSL-FO document.
   */

  public static void
  convert(URL in, OutputStream out, URL userAgentStyleSheet)
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
      false,
      false
    );
  }



  public static void
  convert(InputStream in, OutputStream out)
    throws IOException, CSSToXSLFOException
  {
    convert(in, out, null);
  }



  public static void
  convert(InputStream in, OutputStream out, URL userAgentStyleSheet)
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
    Map			userAgentParameters,
    URL[]		preprocessors,
    boolean		validate,
    boolean		debug
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
          userAgentParameters,
          parent,
          debug
        );

      InputSource	source = new InputSource(in);

      if (baseUrl != null)
      {
        source.setSystemId(baseUrl.toString());
      }

      TransformerHandler	handler =
        be.re.xml.sax.Util.newSAXTransformerFactory().newTransformerHandler();

      handler.setResult(new StreamResult(out));
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
    boolean	debug = false;
    String	filename = null;
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
        if (args[i].equals("-fo"))
        {
          if (i == args.length - 1)
          {
            usage(1);
          }

          filename = args[++i];
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
            if (args[i].equals("-debug"))
            {
              debug = true;
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
        filename != null ?
          (OutputStream) new FileOutputStream(filename) :
          (OutputStream) System.out,
        baseUrl != null ? baseUrl : url,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToXSLFO.class.getResource("/catalog"),
        parameters,
        preprocessors,
        validate,
        debug
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
    System.err.println("Usage: be.re.css.CSSToXSLFO");
    System.err.println("  [-h]: show this help");
    System.err.println("  [-baseurl url]: base URL ");
    System.err.println("  [-c url_or_filename]: catalog for entity resolution");
    System.err.println("  [-config url_or_filename]: extra configuration");
    System.err.println("  [-debug]: debug mode");
    System.err.println("  [-fo filename]: output file, uses stdout by default");
    System.err.println("  [-p url_or_filename_comma_list]: preprocessors");
    System.err.println("  [-uacss url_or_filename]: User Agent style sheet");
    System.err.println("  [-v]: turn on validation");
    System.err.
      println("  [url_or_filename]: the input document, use stdin by default");
    System.err.println("  [parameter=value ...] ");
    System.err.println();
    Util.printUserAgentParameters(System.err);
    System.exit(code);
  }

} // CSSToXSLFO
