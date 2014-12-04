package be.re.css;

import be.re.xml.sax.ProtectEventHandlerFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.cli.CommandLineOptions;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;



/**
 * Convenience class for the conversion from CSS to the new FOP XSL-FO
 * formatter.
 * @author Werner Donn\u00e9
 */

public class CSSToFOPNew

{

  public static void
  convert(URL in, OutputStream out, URL userAgentStyleSheet, String format)
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
      null,
      false,
      null
    );
  }



  public static void
  convert(InputStream in, OutputStream out, String format)
    throws IOException, CSSToXSLFOException
  {
    convert(in, out, null, format);
  }



  public static void
  convert
  (
    InputStream		in,
    OutputStream	out,
    URL			userAgentStyleSheet,
    String		format
  ) throws IOException, CSSToXSLFOException
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
      null,
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
    String		format,
    File		configFile,
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
      configFile,
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
    String		format,
    File		configFile,
    boolean		validate,
    FOUserAgent		agent
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

      FopFactory	factory = FopFactory.newInstance();

      if (configFile != null)
      {
        factory.setUserConfig
        (
          new DefaultConfigurationBuilder().buildFromFile(configFile)
        );
      }

      if (agent == null)
      {
        agent = factory.newFOUserAgent();
      }

      filter.setContentHandler
      (
        factory.newFop(format, agent, out).getDefaultHandler()
      );

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
    String[]	fopOptions = null;
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

      if (args[i].equals("-fop"))
      {
        fopOptions = new String[args.length - ++i];

        for (int j = 0; i < args.length; ++i, ++j)
        {
          fopOptions[j] = args[i];
        }
      }
      else
      {
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

    parameters.put
    (
      "base-url",
      baseUrl != null ?
        baseUrl.toString() : (url != null ? url.toString() : "")
    );

    FOUserAgent	agent = null;

    FopCommandLine	options = new FopCommandLine();

    options.
      parse(setDummyInputFile(fopOptions != null ? fopOptions : new String[0]));
    agent = options.getFOUserAgent();

    try
    {
      convert
      (
        url != null ? url.openStream() : System.in,
        new FileOutputStream(options.getOutputFile()),
        baseUrl != null ? baseUrl : url,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToFOPNew.class.getResource("/catalog"),
        parameters,
        preprocessors,
        options.getOutputFormat(),
        null,
        validate,
        agent
      );
    }

    catch (Throwable e)
    {
      System.err.println(e.getMessage());
      be.re.util.Util.printStackTrace(e);
    }
  }



  private static String[]
  setDummyInputFile(String[] args) throws Exception
  {
    String[]	result = new String[args.length + 2];

    System.arraycopy(args, 0, result, 0, args.length);
    result[result.length - 2] = "-fo";
    result[result.length - 1] =
      be.re.io.Util.createTempFile("css2fopnew.", null).getAbsolutePath();

    return result;
  }



  private static void
  usage(int code)
  {
    System.err.println("Usage: be.re.css.CSSToFOPNew");
    System.err.println("  [-h]: show this help");
    System.err.println("  [-baseurl url]: base URL");
    System.err.println("  [-c url_or_filename]: catalog for entity resolution");
    System.err.println("  [-p url_or_filename_comma_list]: preprocessors");
    System.err.println("  [-uacss url_or_filename]: User Agent style sheet");
    System.err.println("  [-v]: turn on validation");
    System.err.
      println("  [url_or_filename]: the input document, uses stdin by default");
    System.err.println("  [parameter=value ...] ");
    System.err.
      println("  -fop options: the rest of the command-line is for FOP");
    System.err.println();
    Util.printUserAgentParameters(System.err);
    System.exit(code);
  }



  private static class FopCommandLine extends CommandLineOptions

  {

    public FOUserAgent
    getFOUserAgent()
    {
      return super.getFOUserAgent();
    }



    public String
    getOutputFormat() throws FOPException
    {
      return super.getOutputFormat();
    }

  } // FopCommandLine

} // CSSToFOPNew
