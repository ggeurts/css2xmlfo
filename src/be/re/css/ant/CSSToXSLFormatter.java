package be.re.css.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import jp.co.antenna.XfoJavaCtl.XfoObj;
import org.apache.tools.ant.BuildException;



/**
 * @author Werner Donn\u00e9
 */

public class CSSToXSLFormatter extends TaskBase

{

  public void
  execute() throws BuildException
  {
    File	f = null;

    try
    {
      f = File.createTempFile("be.re.css.", "css2xslformatter");
      f.deleteOnExit();

      be.re.css.CSSToXSLFO.convert
      (
        input.openStream(),
        new FileOutputStream(f),
        baseUrl != null ? baseUrl : input,
        userAgentStyleSheet,
        catalog != null ?
          catalog : CSSToXSLFormatter.class.getResource("/catalog"),
        parameters,
        preprocessors,
        validate,
        false
      );

      new XfoObj().render(new FileInputStream(f), new FileOutputStream(output));
    }

    catch (Exception e)
    {
      e.printStackTrace();
      throw new BuildException(e);
    }

    finally
    {
      if (f != null)
      {
        f.delete();
      }
    }
  }

} // CSSToXSLFormatter
