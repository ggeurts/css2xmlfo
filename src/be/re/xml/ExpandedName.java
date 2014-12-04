package be.re.xml;

import javax.xml.namespace.QName;
import org.w3c.dom.Node;



/**
 * A struct to represent the expanded name of an element.
 * @author Werner Donn\u00e9
 */

public class ExpandedName

{

  public String	localName;

  /**
   * The empty string or <code>null</code> indicates absence of a namespace.
   */

  public String	namespaceURI;



  public
  ExpandedName(String namespaceURI, String localName)
  {
    this.namespaceURI = namespaceURI == null ? "" : namespaceURI;
    this.localName = localName;
  }



  public
  ExpandedName(QName name)
  {
    this(name.getNamespaceURI(), name.getLocalPart());
  }



  public
  ExpandedName(Node node)
  {
    this(node.getNamespaceURI(), node.getLocalName());
  }



  public boolean
  equals(Object o)
  {
    return
      o instanceof ExpandedName &&
        namespaceURI.equals(((ExpandedName) o).namespaceURI) &&
        localName.equals(((ExpandedName) o).localName);
  }



  public int
  hashCode()
  {
    return toString().hashCode();
  }



  public boolean
  noNamespace()
  {
    return namespaceURI == null || namespaceURI.equals("");
  }



  public String
  toString()
  {
    return namespaceURI + "#" + localName;
  }

} // ExpandedName
