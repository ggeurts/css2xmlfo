package be.re.css;

import java.io.PrintStream;
import java.io.PrintWriter;



/**
 * General exception for the conversion from CSS to XSL-FO.
 * @author Werner Donne\u00e9
 */

public class CSSToXSLFOException extends Exception

{

  private Exception	exception;
  private String	message;



  public
  CSSToXSLFOException(Exception e)
  {
    this.exception = e;
  }



  public
  CSSToXSLFOException(String message)
  {
    this.message = message;
  }



  public Exception
  getException()
  {
    return exception;
  }



  public String
  getMessage()
  {
    return
      message != null ?
        message : (exception != null ? exception.getMessage() : null);
  }



  public void
  printStackTrace(PrintStream s)
  {
    super.printStackTrace(s);

    if (exception != null)
    {
      s.println("Caused by:");
      exception.printStackTrace(s);
    }
  }



  public void
  printStackTrace(PrintWriter s)
  {
    super.printStackTrace(s);

    if (exception != null)
    {
      s.println("Caused by:");
      exception.printStackTrace(s);
    }
  }

} // CSSToXSLFOException
