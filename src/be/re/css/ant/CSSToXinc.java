package be.re.css.ant;

import java.io.FileOutputStream;
import org.apache.tools.ant.BuildException;



/**
 * @author Werner Donn\u00e9
 */

public class CSSToXinc extends TaskBase

{

  public void
  execute() throws BuildException
  {
    try
    {
      be.re.css.CSSToXinc.convert
      (
        input.openStream(),
        new FileOutputStream(output),
        baseUrl != null ? baseUrl : input,
        userAgentStyleSheet,
        catalog != null ? catalog : CSSToXinc.class.getResource("/catalog"),
        parameters,
        preprocessors,
        be.re.css.CSSToXinc.PDF,
        validate
      );
    }

    catch (Exception e)
    {
      e.printStackTrace();
      throw new BuildException(e);
    }
  }

} // CSSToXinc
