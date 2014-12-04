package be.re.css.ant;

import be.re.util.Array;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.Task;



/**
 * The base class for all CSSToXSLFO tasks.
 * @author Werner Donn\u00e9
 */

public abstract class TaskBase extends Task

{

  protected URL		baseUrl;
  protected URL		catalog;
  protected URL		input;
  protected File	output;
  protected Map		parameters = new HashMap();
  protected URL[]	preprocessors = new URL[0];
  protected URL		userAgentStyleSheet = null;
  protected boolean	validate = false;



  public Parameter
  createParameter()
  {
    return new Parameter();
  }



  public Preprocessor
  createPreprocessor()
  {
    return new Preprocessor();
  }



  protected URL
  createUrl(String s)
  {
    try
    {
      return
        be.re.net.Util.isUrl(s) ?
          new URL(s) : be.re.net.Util.fileToUrl(new File(s));
    }

    catch (MalformedURLException e)
    {
      throw new RuntimeException(e);
    }
  }



  public void
  setBaseurl(String value)
  {
    baseUrl = createUrl(value);
  }



  public void
  setCatalog(String value)
  {
    catalog = createUrl(value);
  }



  public void
  setInput(String value)
  {
    input = createUrl(value);
  }



  public void
  setOutput(File value)
  {
    output = value;
  }



  public void
  setUseragentstylesheet(String value)
  {
    userAgentStyleSheet = createUrl(value);
  }



  public void
  setValidate(boolean value)
  {
    validate = value;
  }



  public class Parameter

  {

    private String	name = null;
    private String	value = null;



    public void
    setName(String value)
    {
      name = value;

      if (this.value != null)
      {
        parameters.put(name, this.value);
      }
    }



    public void
    setValue(String value)
    {
      this.value = value;

      if (name != null)
      {
        parameters.put(name, this.value);
      }
    }

  } // Parameter



  public class Preprocessor

  {

    public void
    setStylesheet(String value)
    {
      preprocessors = (URL[]) Array.append(preprocessors, createUrl(value));
    }

  } // Preprocessor

} // TaskBase
