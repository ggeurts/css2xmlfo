package be.re.css.ant;

import java.io.File;
import java.io.FileOutputStream;
import org.apache.tools.ant.BuildException;



/**
 * @author Werner Donn\u00e9
 */

public class CSSToFOPNew extends TaskBase

{

  private File	configuration = null;



  public void
  execute() throws BuildException
  {
    try
    {
      be.re.css.CSSToFOPNew.convert
      (
        input.openStream(),
        new FileOutputStream(output),
        baseUrl != null ? baseUrl : input,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToFOPNew.class.getResource("/catalog"),
        parameters,
        preprocessors,
        output.toString().toLowerCase().endsWith(".pdf") ?
          "application/pdf" :
          (
            output.toString().toLowerCase().endsWith(".ps") ?
              "application/postscript" :
              (
                output.toString().toLowerCase().endsWith(".svg") ?
                  "image/svg+xml" : null
              )
          ),
        configuration,
        validate
      );
    }

    catch (Exception e)
    {
      e.printStackTrace();
      throw new BuildException(e);
    }
  }



  public void
  setConfig(File value)
  {
    value = configuration;
  }

} // CSSToFOPNew
