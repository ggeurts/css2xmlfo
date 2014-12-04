package be.re.xml.sax;

import java.io.IOException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * If the parent already has an entity resolver on it, it will not be
 * replaced by this filter.
 * @author Werner Donn\u00e9
 */

public class ProtectEventHandlerFilter extends XMLFilterImpl

{

  private boolean	entityResolver;
  private boolean	errorHandler;



  public
  ProtectEventHandlerFilter(boolean entityResolver, boolean errorHandler)
  {
    this.entityResolver = entityResolver;
    this.errorHandler = errorHandler;
  }



  public
  ProtectEventHandlerFilter
  (
    boolean	entityResolver,
    boolean	errorHandler,
    XMLReader	parent
  )
  {
    super(parent);
    this.entityResolver = entityResolver;
    this.errorHandler = errorHandler;
  }



  public void
  parse(InputSource input) throws IOException, SAXException
  {
    if (getParent() != null)
    {
      setupParse();
      getParent().parse(input);
    }
  }



  public void
  parse(String systemId) throws IOException, SAXException
  {
    if (getParent() != null)
    {
      setupParse();
      getParent().parse(systemId);
    }
  }



  private void
  setupParse()
  {
    if (!entityResolver || getParent().getEntityResolver() == null)
    {
      getParent().setEntityResolver(this);
    }

    getParent().setContentHandler(this);
    getParent().setDTDHandler(this);

    if (!errorHandler || getParent().getErrorHandler() == null)
    {
      getParent().setErrorHandler(this);
    }
  }

} // ProtectEventHandlerFilter
