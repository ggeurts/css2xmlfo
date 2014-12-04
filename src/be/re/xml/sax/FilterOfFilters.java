package be.re.xml.sax;

import be.re.io.FlushOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



/**
 * The given array of filters is connected into a filter-chain and encapsulated
 * in this filter, so you can insert the whole in a chain as one filter. The
 * wiring of the event chains will be interrupted for those filters that don't
 * implement the corresponding handler interfaces.
 * @author Werner Donn\u00e9
 */

public class FilterOfFilters extends XMLFilterImpl

{

  private XMLFilter	first;
  private XMLFilter	last;



  public
  FilterOfFilters(XMLFilter[] filters)
  {
    this(filters, false);
  }



  public
  FilterOfFilters(XMLFilter[] filters, boolean debug)
  {
    setupChain(debug ? addDebug(filters) : filters);
  }



  public
  FilterOfFilters(XMLFilter[] filters, XMLReader parent)
  {
    this(filters, false, parent);
  }



  public
  FilterOfFilters(XMLFilter[] filters, boolean debug, XMLReader parent)
  {
    setupChain(debug ? addDebug(filters) : filters);
    setParent(parent);
  }



  private XMLFilter[]
  addDebug(XMLFilter[] filters)
  {
    XMLFilter[]	result = new XMLFilter[filters.length * 2 + 1];

    result[0] =
      new Tee
      (
        new ContentHandler[]
        {
          outputHandler(toString() + "_input.xml")
        }
      );

    for (int i = 0; i < filters.length; ++i)
    {
      result[i * 2 + 1] = filters[i];
      result[i * 2 + 2] =
        new Tee
        (
          new ContentHandler[]
          {
            new BalanceChecker
            (
              new File(toString() + "_" + filters[i].toString() + ".balance")
            ),
            outputHandler(toString() + "_" + filters[i].toString())
          }
        );
    }

    return result;
  }



  public boolean
  getFeature(String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    return first != null ? first.getFeature(name) : super.getFeature(name);
  }



  public XMLReader
  getParent()
  {
    return first != null ? first.getParent() : super.getParent();
  }



  public Object
  getProperty(String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    return first != null ? first.getProperty(name) : super.getProperty(name);
  }



  private static ContentHandler
  outputHandler(String filename)
  {
    try
    {
      TransformerHandler	handler =
        Util.newSAXTransformerFactory().newTransformerHandler();

      handler.setResult
      (
        new StreamResult(new FlushOutputStream(new FileOutputStream(filename)))
      );

      return handler;
    }

    catch (Exception e)
    {
      throw new UndeclaredThrowableException(e);
    }
  }



  public void
  parse(InputSource input) throws IOException, SAXException
  {
    if (last != null)
    {
      last.parse(input);
    }
    else
    {
      super.parse(input);
    }
  }



  public void
  setContentHandler(ContentHandler handler)
  {
    if (last != null)
    {
      last.setContentHandler(handler);
    }
    else
    {
      super.setContentHandler(handler);
    }
  }



  public void
  setDTDHandler(DTDHandler handler)
  {
    if (last != null)
    {
      last.setDTDHandler(handler);
    }
    else
    {
      super.setDTDHandler(handler);
    }
  }



  public void
  setEntityResolver(EntityResolver resolver)
  {
    if (last != null)
    {
      last.setEntityResolver(resolver);
    }
    else
    {
      super.setEntityResolver(resolver);
    }
  }



  public void
  setErrorHandler(ErrorHandler handler)
  {
    if (last != null)
    {
      last.setErrorHandler(handler);
    }
    else
    {
      super.setErrorHandler(handler);
    }
  }



  public void
  setFeature(String name, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    if (first != null)
    {
      first.setFeature(name, value);
    }
    else
    {
      super.setFeature(name, value);
    }
  }



  public void
  setParent(XMLReader parent)
  {
    if (first != null)
    {
      first.setParent(parent);
    }
    else
    {
      super.setParent(parent);
    }
  }



  public void
  setProperty(String name, Object value)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
    if (first != null)
    {
      first.setProperty(name, value);
    }
    else
    {
      super.setProperty(name, value);
    }
  }



  private void
  setupChain(XMLFilter[] filters)
  {
    if (filters.length == 0)
    {
      return;
    }

    for (int i = filters.length - 1; i > 0; --i)
    {
      filters[i].setParent(filters[i - 1]);

      // The following connections make it work also when this filter is
      // inserted in a chain that is already running, i.e. for which parse is
      // already called.

      if (filters[i] instanceof ContentHandler)
      {
        filters[i - 1].setContentHandler((ContentHandler) filters[i]);
      }

      if (filters[i] instanceof DTDHandler)
      {
        filters[i - 1].setDTDHandler((DTDHandler) filters[i]);
      }

      if (filters[i] instanceof EntityResolver)
      {
        filters[i - 1].setEntityResolver((EntityResolver) filters[i]);
      }

      if (filters[i] instanceof ErrorHandler)
      {
        filters[i - 1].setErrorHandler((ErrorHandler) filters[i]);
      }
    }

    first = filters[0];
    last = filters[filters.length - 1];

    if (first instanceof ContentHandler)
    {
      super.setContentHandler((ContentHandler) first);
    }

    if (first instanceof DTDHandler)
    {
      super.setDTDHandler((DTDHandler) first);
    }

    if (first instanceof EntityResolver)
    {
      super.setEntityResolver((EntityResolver) first);
    }

    if (first instanceof ErrorHandler)
    {
      super.setErrorHandler((ErrorHandler) first);
    }
  }

} // FilterOfFilters
