package be.re.css.ant;

import java.io.File;
import java.io.FileOutputStream;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Options;
import org.apache.tools.ant.BuildException;



/**
 * @author Werner Donn\u00e9
 */

public class CSSToFOP extends TaskBase

{

  private File		configuration = null;
  private boolean	quite = false;



  public void
  execute() throws BuildException
  {
    try
    {
      if (configuration != null)
      {
        new Options(configuration);
      }

      be.re.css.CSSToFOP.convert
      (
        input.openStream(),
        new FileOutputStream(output),
        baseUrl != null ? baseUrl : input,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToFOP.class.getResource("/catalog"),
        parameters,
        preprocessors,
        output.toString().toLowerCase().endsWith(".pdf") ?
          Driver.RENDER_PDF :
          (
            output.toString().toLowerCase().endsWith(".ps") ?
              Driver.RENDER_PS :
              (
                output.toString().toLowerCase().endsWith(".svg") ?
                  Driver.RENDER_SVG : -1
              )
          ),
        true,
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
    configuration = value;
  }



  public void
  setQuite(boolean value)
  {
    quite = value;
  }

} // CSSToFOP
