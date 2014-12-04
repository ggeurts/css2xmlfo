package be.re.io;

/**
 * Make it possible to wrap a cause.
 * @author Werner Donn\u00e9
 */

public class IOException extends java.io.IOException

{

  public
  IOException()
  {
  }



  public
  IOException(String s)
  {
    super(s);
  }



  public
  IOException(String message, Throwable cause)
  {
    super(message);
    initCause(cause);
  }



  public
  IOException(Throwable cause)
  {
    super();
    initCause(cause);
  }

} // IOException
