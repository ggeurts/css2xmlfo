package be.re.net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;



/**
 * Several Internet protocols use headers. This class can be used to manage
 * them. Headers can occur more than once. Even name/value pairs may be
 * dupplicated.
 * @author Werner Donn\u00e9
 */

public class Headers

{

  private List	headers = new ArrayList();



  /**
   * Adds a name/value pair.
   */

  public void
  add(String name, String value)
  {
    headers.add(new Header(name, value));
  }



  /**
   * Adds all the headers in <code>headers</code>.
   */

  public void
  add(Headers headers)
  {
    Header[]	all = headers.getAll();

    for (int i = 0; i < all.length; ++i)
    {
      add(all[i].getName(), all[i].getValue());
    }
  }



  /**
   * Removes all headers.
   */

  public void
  clear()
  {
    headers.clear();
  }



  /**
   * Returns all the values for the header <code>name</code> in the order of
   * appearance.
   */

  public String[]
  get(String name)
  {
    List	values = new ArrayList();

    for (Iterator i = headers.iterator(); i.hasNext();)
    {
      Header	tuple = (Header) i.next();

      if (tuple.name.equalsIgnoreCase(name))
      {
        values.add(tuple.value);
      }
    }

    return (String[]) values.toArray(new String[values.size()]);
  }



  /**
   * Returns all headers.
   */

  public Header[]
  getAll()
  {
    return (Header[]) headers.toArray(new Header[headers.size()]);
  }



  /**
   * Returns all the values for the header <code>name</code> in the order of
   * appearance. If a header value is a comma-separated list, the elements in
   * the list are added separately in the order of appearance and without
   * surrounding whitespace.
   */

  public String[]
  getValuesFromList(String name)
  {
    List	values = new ArrayList();

    for (Iterator i = headers.iterator(); i.hasNext();)
    {
      Header	tuple = (Header) i.next();

      if (tuple.name.equalsIgnoreCase(name))
      {
        StringTokenizer	tokenizer = new StringTokenizer(tuple.value, ",");

        while (tokenizer.hasMoreTokens())
        {
          values.add(tokenizer.nextToken().trim());
        }
      }
    }

    return (String[]) values.toArray(new String[values.size()]);
  }



  /**
   * Removes all headers with <code>name</code> as their name.
   */

  public void
  remove(String name)
  {
    for (Iterator i = headers.iterator(); i.hasNext();)
    {
      Header	tuple = (Header) i.next();

      if (tuple.name.equalsIgnoreCase(name))
      {
        i.remove();
      }
    }
  }



  /**
   * Removes one name/value pair.
   */

  public void
  remove(String name, String value)
  {
    for (Iterator i = headers.iterator(); i.hasNext();)
    {
      Header	tuple = (Header) i.next();

      if (tuple.name.equalsIgnoreCase(name) && tuple.value.equals(value))
      {
        i.remove();
      }
    }
  }



  /**
   * Replaces all the headers with <code>name</code> as their name with the
   * given name/value pair.
   */

  public void
  set(String name, String value)
  {
    remove(name);
    add(name, value);
  }



  /**
   * Replaces one name/value pair.
   */

  public void
  set(String name, String oldValue, String newValue)
  {
    for (Iterator i = headers.iterator(); i.hasNext();)
    {
      Header	tuple = (Header) i.next();

      if (tuple.name.equalsIgnoreCase(name) && tuple.value.equals(oldValue))
      {
        tuple.value = newValue;
      }
    }
  }



  /**
   * Returns the number of headers.
   */

  public int
  size()
  {
    return headers.size();
  }



  /**
   * A string representation of all headers. The string can be used in Internet
   * protocols.
   */

  public String
  toString()
  {
    String	result = "";

    for (Iterator i = headers.iterator(); i.hasNext();)
    {
      Header	tuple = (Header) i.next();

      result += tuple.name + ":" + tuple.value + "\r\n";
    }

    return result;
  }



  /**
   * Represents one header.
   * @author Werner Donn\u00e9
   */

  public class Header

  {

    private String	name;
    private String	value;



    public
    Header(String name, String value)
    {
      this.name = name;
      this.value = value;
    }



    public String
    getName()
    {
      return name;
    }



    public String
    getValue()
    {
      return value;
    }

  } // Header

} // Headers
