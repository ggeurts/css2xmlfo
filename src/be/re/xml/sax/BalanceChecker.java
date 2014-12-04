package be.re.xml.sax;

import be.re.xml.ExpandedName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;



public class BalanceChecker extends XMLFilterImpl

{

  private Stack		elements = new Stack();
  private File		file;
  private int		indent = 0;
  private PrintStream	out;



  public
  BalanceChecker(File file)
  {
    this.file = file;
  }



  public
  BalanceChecker(File file, XMLReader parent)
  {
    super(parent);
    this.file = file;
  }



  public void
  endDocument() throws SAXException
  {
    for (int i = elements.size() - 1; i >=0; --i)
    {
      write("Element " + elements.get(i).toString() + " is not closed.");
    }

    out.close();
    super.endDocument();
  }



  public void
  endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    ExpandedName	element = new ExpandedName(namespaceURI, localName);

    if (elements.isEmpty())
    {
      write
      (
        "Closing " + element.toString() + " while no open elements are left."
      );
    }
    else
    {
      ExpandedName	name = (ExpandedName) elements.pop();

      if (!name.equals(element))
      {
        write
        (
          "Closing " + element.toString() + " while expecting " +
            name.toString() + "."
        );
      }
    }

    indent -= 2;
    write("</" + element.toString() + ">");
    super.endElement(namespaceURI, localName, qName);
  }



  private void
  openWriter() throws SAXException
  {
    if (out == null)
    {
      try
      {
        out = new PrintStream(new FileOutputStream(file));
      }

      catch (IOException e)
      {
        throw new SAXException(e);
      }
    }
  }



  public void
  startDocument() throws SAXException
  {
    openWriter();
    super.startDocument();
  }



  public void
  startElement
  (
    String	namespaceURI,
    String	localName,
    String	qName,
    Attributes	atts
  ) throws SAXException
  {
    openWriter();

    ExpandedName	name = new ExpandedName(namespaceURI, localName);

    write("<" + name.toString() + ">");
    indent += 2;
    elements.push(name);
    super.startElement(namespaceURI, localName, qName, atts);
  }



  private void
  write(String s)
  {
    if (indent > 0)
    {
      char[]	c = new char[indent];

      Arrays.fill(c, ' ');
      out.print(c);
    }

    out.println(s);
    out.flush();
  }

} // BalanceChecker
