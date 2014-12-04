package be.re.xml.sax;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



public class ErrorHandler implements org.xml.sax.ErrorHandler

{

  private boolean	warnings;



  public
  ErrorHandler()
  {
    this(false);
  }



  public
  ErrorHandler(boolean warnings)
  {
    this.warnings = warnings;
  }



  public void
  error(SAXParseException exception) throws SAXException
  {
    System.err.println("Error: " + getMessage(exception));
  }



  public void
  fatalError(SAXParseException exception) throws SAXException
  {
    System.err.println("Fatal error: " + getMessage(exception));
  }



  private String
  getMessage(SAXParseException e)
  {
    return
      (e.getPublicId() != null ? (e.getPublicId() + ": ") : "") +
        (e.getSystemId() != null ? (e.getSystemId() + ": ") : "") + "Line " +
        String.valueOf(e.getLineNumber()) + ": " + e.getMessage();
  }



  public void
  warning(SAXParseException exception) throws SAXException
  {
    if (warnings)
    {
      System.err.println("Warning: " + getMessage(exception));
    }
  }

} // ErrorHandler
