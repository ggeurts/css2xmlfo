package be.re.css.ant;

import java.io.FileOutputStream;
import org.apache.tools.ant.BuildException;



/**
 * @author Werner Donn\u00e9
 */

public class CSSToXSLFO extends TaskBase

{

  public void
  execute() throws BuildException
  {
    try
    {
      be.re.css.CSSToXSLFO.convert
      (
        input.openStream(),
        new FileOutputStream(output),
        baseUrl != null ? baseUrl : input,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToXSLFO.class.getResource("/catalog"),
        parameters,
        preprocessors,
        validate,
        false
      );
    }

    catch (Exception e)
    {
      e.printStackTrace();
      throw new BuildException(e);
    }
  }

} // CSSToXSLFO
