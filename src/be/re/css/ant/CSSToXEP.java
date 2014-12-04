package be.re.css.ant;

import java.io.FileOutputStream;
import java.net.URL;
import org.apache.tools.ant.BuildException;



/**
 * @author Werner Donn\u00e9
 */

public class CSSToXEP extends TaskBase

{

  private URL		configuration = null;
  private boolean	quiet = false;



  public void
  execute() throws BuildException
  {
    try
    {
      be.re.css.CSSToXEP.convert
      (
        input.openStream(),
        new FileOutputStream(output),
        baseUrl != null ? baseUrl : input,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToXEP.class.getResource("/catalog"),
        parameters,
        preprocessors,
        output.toString().toLowerCase().endsWith(".pdf") ?
          be.re.css.CSSToXEP.PDF :
          (
            output.toString().toLowerCase().endsWith(".ps") ?
              be.re.css.CSSToXEP.POSTSCRIPT : -1
          ),
        validate,
        quiet,
        configuration
      );
    }

    catch (Exception e)
    {
      e.printStackTrace();
      throw new BuildException(e);
    }
  }



  public void
  setConfig(String value)
  {
    configuration = createUrl(value);
  }



  public void
  setQuiet(boolean value)
  {
    quiet = value;
  }

} // CSSToXEP
