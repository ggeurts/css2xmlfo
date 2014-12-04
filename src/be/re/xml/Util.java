package be.re.xml;

import be.re.xml.sax.ErrorHandler;
import be.re.util.Equal;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributeListImpl;



/**
 * This class is a collection of untility functions.
 * @author Werner Donn\u00e9
 */

public class Util

{

  private static DocumentBuilderFactory	nonValidatingFactory;
  private static final Equal		elementSelector =
    new Equal()
    {
      public boolean
      equal(Object object, Object refData)
      {
        return object instanceof Element;
      }
    };
  private static final Equal		nameSelector =
    new Equal()
    {
      public boolean
      equal(Object object, Object refData)
      {
        return ((Node) object).getNodeName().equals((String) refData);
      }
    };
  private static final Equal		qnameSelector =
    new Equal()
    {
      public boolean
      equal(Object object, Object refData)
      {
        return
          (
            ((ExpandedName) refData).localName == null ||
              ((ExpandedName) refData).localName.
                equals(((Node) object).getLocalName())
          ) &&
            (
              ((ExpandedName) refData).namespaceURI.
                equals(((Node) object).getNamespaceURI()) ||
                (
                  "".equals(((ExpandedName) refData).namespaceURI) &&
                    ((Node) object).getNamespaceURI() == null
                )
            );
      }
    };
  private static TransformerFactory	transformerFactory;
  private static DocumentBuilderFactory	validatingFactory;



  public static Node
  clearNode(Node node)
  {
    while (node.hasChildNodes())
    {
      node.removeChild(node.getFirstChild());
    }

    return node;
  }



  private static void
  collectChild(Node node, List children)
  {
    if (node != null)
    {
      children.add(node);
      collectChild(node.getNextSibling(), children);
    }
  }



  /**
   * Returns all child nodes of a node as an array.
   */

  public static Node[]
  collectChildren(Node node)
  {
    List	children = new ArrayList();

    collectChild(node.getFirstChild(), children);

    return (Node[]) children.toArray(new Node[children.size()]);
  }



  public static Node
  copy(Node node, Document owner)
  {
    if (node == null)
    {
      return null;
    }

    Node	result = null;

    switch (node.getNodeType())
    {
      case Node.ATTRIBUTE_NODE:
        result = copyAttribute((Attr) node, owner);
        break;

      case Node.CDATA_SECTION_NODE:
        result = owner.createCDATASection(((CDATASection) node).getData());
        break;

      case Node.COMMENT_NODE:
        result = owner.createComment(((Comment) node).getData());
        break;

      case Node.DOCUMENT_FRAGMENT_NODE:
        result = owner.createDocumentFragment();
        break;

      case Node.ELEMENT_NODE:
        result = copyElement((Element) node, owner);
        break;

      case Node.ENTITY_REFERENCE_NODE:
        result = owner.createEntityReference(node.getNodeName());
        break;

      case Node.PROCESSING_INSTRUCTION_NODE:
        result =
          owner.createProcessingInstruction
          (
            ((ProcessingInstruction) node).getTarget(),
            ((ProcessingInstruction) node).getData()
          );
        break;

      case Node.TEXT_NODE:
        result = owner.createTextNode(((Text) node).getData());
        break;
    }

    if (result != null)
    {
      for (Node i = node.getFirstChild(); i != null; i = i.getNextSibling())
      {
        result.appendChild(copy(i, owner));
      }
    }

    return result;
  }



  private static Node
  copyAttribute(Attr attribute, Document owner)
  {
    Attr        result =
      attribute.getNamespaceURI() != null ?
        owner.
          createAttributeNS(attribute.getNamespaceURI(), attribute.getName()) :
        owner.createAttribute(attribute.getName());

    result.setValue(attribute.getValue());

    return result;
  }



  private static Node
  copyElement(Element element, Document owner)
  {
    NamedNodeMap	attributes = element.getAttributes();
    Element		result =
      element.getNamespaceURI() != null ?
        owner.createElementNS(element.getNamespaceURI(), element.getTagName()) :
        owner.createElement(element.getTagName());

    for (int i = 0; i < attributes.getLength(); ++i)
    {
      Attr	attribute = (Attr) copy(attributes.item(i), owner);

      if (attribute.getNamespaceURI() != null)
      {
        result.setAttributeNodeNS(attribute); // Why does this method exist?
      }
      else
      {
        result.setAttributeNode(attribute);
      }
    }

    return result;
  }



  /**
   * Extracts the attributes of an element for use in a SAX-environment.
   */

  public static AttributeList
  createAttributeList(Element element)
  {
    AttributeListImpl	attributes = new AttributeListImpl();
    NamedNodeMap	map = element.getAttributes();

    for (int i = 0; i < map.getLength(); ++i)
    {
      Attr	attribute = (Attr) map.item(i);

      attributes.
        addAttribute(attribute.getName(), "CDATA", attribute.getValue());
    }

    return attributes;
  }



  /**
   * Runs a complete DOM-element through a <code>DocumentHandler</code>.
   */

  public static void
  elementToDocumentHandler(Element element, DocumentHandler handler)
    throws SAXException
  {
    handler.startElement(element.getTagName(), createAttributeList(element));
    elementToDocumentHandlerChild(element.getFirstChild(), handler);
    handler.endElement(element.getTagName());
  }



  private static void
  elementToDocumentHandlerChild(Node node, DocumentHandler handler)
    throws SAXException
  {
    if (node == null)
    {
      return;
    }

    switch (node.getNodeType())
    {
      case Node.ELEMENT_NODE:
        elementToDocumentHandler((Element) node, handler);
        break;

      case Node.PROCESSING_INSTRUCTION_NODE:
        processingInstructionToDocumentHandler
        (
          (ProcessingInstruction) node,
          handler
        );
        break;

      case Node.TEXT_NODE:
        textToDocumentHandler((Text) node, handler);
        break;
    }

    elementToDocumentHandlerChild(node.getNextSibling(), handler);
  }



  public static String
  escapeText(String value)
  {
    return escapeText(value, true);
  }



  public static String
  escapeText(String value, boolean minimum)
  {
    StringBuffer	buffer = new StringBuffer((int) (value.length() * 1.1));

    for (int i = 0; i < value.length(); ++i)
    {
      if (value.charAt(i) == '&')
      {
        buffer.append("&amp;");
      }
      else
      {
        if (value.charAt(i) == '\'' && !minimum)
        {
          buffer.append("&apos;");
        }
        else
        {
          if (value.charAt(i) == '>' && !minimum)
          {
            buffer.append("&gt;");
          }
          else
          {
            if (value.charAt(i) == '<')
            {
              buffer.append("&lt;");
            }
            else
            {
              if (value.charAt(i) == '"' && !minimum)
              {
                buffer.append("&quot;");
              }
              else
              {
                buffer.append(value.charAt(i));
              }
            }
          }
        }
      }
    }

    return buffer.toString();
  }



  /**
   * Searches the ancestor chain of a node for a node called
   * <code>name</code>. If no such node is found <code>null</code> is returned.
   */

  public static Node
  findAncestor(Node node, String name)
  {
    return
      node == null ?
        null :
        (
          node.getNodeName().equalsIgnoreCase(name) ?
            node : findAncestor(node.getParentNode(), name)
        );
  }



  /**
   * Does the same as <code>findAncestor</code> except that the search stops
   * when a node called <code>limitingElement</code> is encountered.
   * @see #findAncestor
   */

  public static Node
  findAncestorNotBeyond(Node node, String name, String limitingElement)
  {
    return
      node == null ?
        null :
        (
          node.getNodeName().equalsIgnoreCase(name) ?
            node :
            (
              node.getNodeName().equalsIgnoreCase(limitingElement) ?
                null :
                findAncestorNotBeyond
                (
                  node.getParentNode(),
                  name,
                  limitingElement
                )
            )
        );
  }



  public static DocumentBuilder
  getDocumentBuilder(URL catalog, boolean validating)
    throws IOException, ParserConfigurationException
  {
    return getDocumentBuilder(newDocumentBuilderFactory(validating), catalog);
  }



  public static DocumentBuilder
  getDocumentBuilder(DocumentBuilderFactory factory, URL catalog)
    throws IOException, ParserConfigurationException
  {
    DocumentBuilder	result = factory.newDocumentBuilder();

    result.setErrorHandler(new ErrorHandler(false));

    if (catalog != null)
    {
      result.setEntityResolver(new CatalogResolver(catalog));
    }

    return result;
  }



  public static DocumentBuilderFactory
  getDocumentBuilderFactory(boolean validating)
    throws ParserConfigurationException
  {
    if (validating && validatingFactory != null)
    {
      return validatingFactory;
    }

    if (!validating && nonValidatingFactory != null)
    {
      return nonValidatingFactory;
    }

    DocumentBuilderFactory	factory = newDocumentBuilderFactory(validating);

    if (validating)
    {
      validatingFactory = factory;
    }
    else
    {
      nonValidatingFactory = factory;
    }

    return factory;
  }



  public static Element
  getNextSiblingElement(Node node, String namespaceURI, String localName)
  {
    if (node == null)
    {
      return null;
    }

    return
      node instanceof Element &&
        new ExpandedName(node.getNamespaceURI(), node.getLocalName()).
          equals(new ExpandedName(namespaceURI, localName)) ?
        (Element) node :
        getNextSiblingElement(node.getNextSibling(), namespaceURI, localName);
  }



  public static Element
  getPreviousSiblingElement(Node node, String namespaceURI, String localName)
  {
    if (node == null)
    {
      return null;
    }

    return
      node instanceof Element &&
        new ExpandedName(node.getNamespaceURI(), node.getLocalName()).
          equals(new ExpandedName(namespaceURI, localName)) ?
        (Element) node :
        getPreviousSiblingElement
        (
          node.getPreviousSibling(),
          namespaceURI,
          localName
        );
  }



  public static QName
  getQName(Node node)
  {
    return
      node.getPrefix() != null ?
        new QName
        (
          node.getNamespaceURI(),
          node.getLocalName(),
          node.getPrefix()
        ) :
        (
          node.getNamespaceURI() != null ?
            new QName(node.getNamespaceURI(), node.getLocalName()) :
            new QName(node.getLocalName())
        );
  }



  /**
   * Assumes pure #PCDATA, no mixed content.
   */

  public static String
  getText(Node node)
  {
    if (node == null)
    {
      return null;
    }

    NodeList	list = node.getChildNodes();
    int		position = 0;
    int		size = getTextSize(list);
    char[]	array = new char[size];

    for (int i = 0; i < list.getLength(); ++i)
    {
      if (list.item(i) instanceof Text)
      {
        char[]	chars = ((Text) list.item(i)).getData().toCharArray();

        System.arraycopy(chars, 0, array, position, chars.length);
        position += chars.length;
      }
    }

    return new String(array);
  }



  private static int
  getTextSize(NodeList list)
  {
    int	result = 0;

    for (int i = 0; i < list.getLength(); ++i)
    {
      if (list.item(i) instanceof Text)
      {
        result += ((Text) list.item(i)).getLength();
      }
    }

    return result;
  }



  public static TransformerFactory
  getTransformerFactory() throws TransformerConfigurationException
  {
    if (transformerFactory == null)
    {
      transformerFactory = newTransformerFactory();
    }

    return transformerFactory;
  }



  private static boolean
  isWhiteSpace(String s)
  {
    for (int i = 0; i < s.length(); ++i)
    {
      if (!Character.isWhitespace(s.charAt(i)))
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  isXml(String mimeType)
  {
    mimeType = stripMimeTypeParameters(mimeType).toLowerCase();

    return
      "text/xml".equals(mimeType) || "application/xml".equals(mimeType) ||
        mimeType.endsWith("+xml");
  }



  public static boolean
  isXmlChar(char c)
  {
    return
      c == 0x9 || c == 0xa || c == 0xd || (c >= 0x20 && c <= 0xd7ff) ||
        (c >= 0xe000 && c <= 0xfffd);
  }



  public static boolean
  isXmlText(String s)
  {
    return isXmlText(s.toCharArray());
  }



  public static boolean
  isXmlText(char[] c)
  {
    for (int i = 0; i < c.length; ++i)
    {
      if (!isXmlChar(c[i]))
      {
        return false;
      }
    }

    return true;
  }



  public static DocumentBuilderFactory
  newDocumentBuilderFactory(boolean validating)
    throws ParserConfigurationException
  {
    try
    {
      String			className =
        be.re.util.Util.
          getSystemProperty("javax.xml.parsers.DocumentBuilderFactory");
      DocumentBuilderFactory	factory =
        className != null ?
          (DocumentBuilderFactory) Class.forName(className).newInstance() :
          DocumentBuilderFactory.newInstance();

      factory.setNamespaceAware(true);
      factory.setValidating(validating);

      return factory;
    }

    catch (Exception e)
    {
      throw new ParserConfigurationException(e.getMessage());
    }
  }



  public static TransformerFactory
  newTransformerFactory() throws TransformerConfigurationException
  {
    try
    {
      String	className =
        be.re.util.Util.
          getSystemProperty("javax.xml.transform.TransformerFactory");

      return
        className != null ?
          (TransformerFactory) Class.forName(className).newInstance() :
          TransformerFactory.newInstance();
    }

    catch (Exception e)
    {
      throw new TransformerConfigurationException(e);
    }
  }



  /**
   * Passes a DOM-processing-instruction to a <code>DocumentHandler</code>.
   */

  public static void
  processingInstructionToDocumentHandler
  (
    ProcessingInstruction	processingInstruction,
    DocumentHandler		handler
  ) throws SAXException
  {
    handler.processingInstruction
    (
      processingInstruction.getTarget(),
      processingInstruction.getData()
    );
  }



  public static void
  removeChildren(Node node)
  {
    while (node.hasChildNodes())
    {
      node.removeChild(node.getLastChild());
    }
  }



  public static void
  removeIgnorableSpace(Element element)
  {
    element.normalize();
    removeIgnorableSpace(element.getFirstChild());
  }



  private static void
  removeIgnorableSpace(Node node)
  {
    if (node == null)
    {
      return;
    }

    removeIgnorableSpace(node.getNextSibling());
    removeIgnorableSpace(node.getFirstChild());

    if (node instanceof Text && isWhiteSpace(((Text) node).getData()))
    {
      node.getParentNode().removeChild(node);
    }
  }



  private static void
  selectChild(Node node, Equal equal, Object refData, List children)
  {
    if (node != null)
    {
      if (equal.equal(node, refData))
      {
        children.add(node);
      }

      selectChild(node.getNextSibling(), equal, refData, children);
    }
  }



  private static void
  selectChildNotBeyond
  (
    Node	node,
    Equal	equal,
    Object	refData,
    Equal	equalLimit,
    Object	refDataLimit,
    List	children
  )
  {
    if (node != null)
    {
      if (equal.equal(node, refData))
      {
        children.add(node);
      }

      if (!equalLimit.equal(node, refDataLimit))
      {
        selectChildNotBeyond
        (
          node.getNextSibling(),
          equal,
          refData,
          equalLimit,
          refDataLimit,
          children
        );
      }
    }
  }



  /**
   * Returns all direct child nodes of <code>node</code> that meet the
   * condition expressed by <code>equal</code> and <code>refData</code>.
   * @see be.re.util.Equal
   */

  public static Node[]
  selectChildren(Node node, Equal equal, Object refData)
  {
    List	children = new ArrayList();

    selectChild(node.getFirstChild(), equal, refData, children);

    return (Node[]) children.toArray(new Node[children.size()]);
  }



  /**
   * Returns all direct child nodes of <code>node</code> called
   * <code>name</code>.
   */

  public static Node[]
  selectChildren(Node node, String name)
  {
    return selectChildren(node, nameSelector, name);
  }



  /**
   * Returns all direct child nodes of <code>node</code> with namespace
   * <code>namespaceURI</code> and local name <code>localName</code>.
   * <code>localName</code> may be null;
   */

  public static Node[]
  selectChildren(Node node, String namespaceURI, String localName)
  {
    return
      selectChildren
      (
        node,
        qnameSelector,
        new ExpandedName(namespaceURI, localName)
      );
  }



  /**
   * Returns all direct child nodes of <code>node</code> that meet the
   * condition expressed by <code>equal</code> and <code>refData</code>. The
   * search stops if a node meeting the condition expressed by
   * <code>equalLimit</code> and <code>refDataLimit</code> is encountered.
   * @see be.re.util.Equal
   */

  public static Node[]
  selectChildrenNotBeyond
  (
    Node	node,
    Equal	equal,
    Object	refData,
    Equal	equalLimit,
    Object	refDataLimit
  )
  {
    List	children = new ArrayList();

    selectChildNotBeyond
    (
      node.getFirstChild(),
      equal,
      refData,
      equalLimit,
      refDataLimit,
      children
    );

    return (Node[]) children.toArray(new Node[children.size()]);
  }



  /**
   * Returns all direct child nodes of <code>node</code> called
   * <code>name</code>. The search stops is a node called <code>limit</code> is
   * encountered.
   */

  public static Node[]
  selectChildrenNotBeyond(Node node, String name, String limit)
  {
    return
      selectChildrenNotBeyond(node, nameSelector, name, nameSelector, limit);
  }



  /**
   * Returns an element if there is exactly one that matches and
   * <code>null</code> otherwise.
   */

  public static Element
  selectElement(Node node, ExpandedName[] path)
  {
    List	result = new ArrayList();

    selectElements(node, path, 0, result);

    return result.size() == 1 ? (Element) result.get(0) : null;
  }



  public static Element[]
  selectElements(Node node)
  {
    List	children = new ArrayList();

    selectChild(node.getFirstChild(), elementSelector, null, children);

    return (Element[]) children.toArray(new Element[children.size()]);
  }



  /**
   * Returns all elements that match the path.
   */

  public static Element[]
  selectElements(Node node, ExpandedName[] path)
  {
    List	result = new ArrayList();

    selectElements(node, path, 0, result);

    return (Element[]) result.toArray(new Element[0]);
  }



  private static void
  selectElements(Node node, ExpandedName[] path, int position, List result)
  {
    if (position == path.length)
    {
      return;
    }

    Node[]	children =
      selectChildren
      (
        node,
        path[position].namespaceURI,
        path[position].localName
      );

    for (int i = 0; i < children.length; ++i)
    {
      if (position < path.length - 1)
      {
        selectElements(children[i], path, position + 1, result);
      }
      else
      {
        result.add((Element) children[i]);
      }
    }
  }



  /**
   * Returns the first direct child of <code>node</code> that meets the
   * condition expressed by <code>equal</code> and <code>refData</code>.
   * @see be.re.util.Equal
   */

  public static Node
  selectFirstChild(Node node, Equal equal, Object refData)
  {
    return
     node == null ?
       null : selectNextSiblingSibling(node.getFirstChild(), equal, refData);
  }



  /**
   * Returns the first direct child of <code>node</code> called
   * <code>name</code>.
   */

  public static Node
  selectFirstChild(Node node, String name)
  {
    return selectFirstChild(node, nameSelector, name);
  }



  /**
   * Returns the first direct child of <code>node</code> with namespace
   * <code>namespaceURI</code> and local name <code>localName</code>.
   */

  public static Node
  selectFirstChild(Node node, String namespaceURI, String localName)
  {
    return
      selectFirstChild
      (
        node,
        qnameSelector,
        new ExpandedName(namespaceURI, localName)
      );
  }



  /**
   * Returns the first direct child element of <code>node</code>.
   */

  public static Element
  selectFirstElement(Node node)
  {
    return (Element) selectFirstChild(node, elementSelector, null);
  }



  /**
   * Returns the last direct child of <code>node</code> that meets the
   * condition expressed by <code>equal</code> and <code>refData</code>.
   * @see be.re.util.Equal
   */

  public static Node
  selectLastChild(Node node, Equal equal, Object refData)
  {
    return
      node == null ?
        null :
        selectPreviousSiblingSibling(node.getLastChild(), equal, refData);
  }



  /**
   * Returns the last direct child of <code>node</code> called
   * <code>name</code>.
   */

  public static Node
  selectLastChild(Node node, String name)
  {
    return selectLastChild(node, nameSelector, name);
  }



  /**
   * Returns the last direct child of <code>node</code> with namespace
   * <code>namespaceURI</code> and local name <code>localName</code>.
   */

  public static Node
  selectLastChild(Node node, String namespaceURI, String localName)
  {
    return
      selectLastChild
      (
        node,
        qnameSelector,
        new ExpandedName(namespaceURI, localName)
      );
  }



  /**
   * Returns the next sibling of <code>node</code> that meets the
   * condition expressed by <code>equal</code> and <code>refData</code>.
   * @see be.re.util.Equal
   */

  public static Node
  selectNextSibling(Node node, Equal equal, Object refData)
  {
    return
      node == null ?
        null :
        selectNextSiblingSibling(node.getNextSibling(), equal, refData);
  }



  /**
   * Returns the next sibling of <code>node</code> called <code>name</code>.
   */

  public static Node
  selectNextSibling(Node node, String name)
  {
    return selectNextSibling(node, nameSelector, name);
  }



  /**
   * Returns the next sibling of <code>node</code> with namspace
   * <code>namespaceURI</code> and local name <code>localName</code>.
   */

  public static Node
  selectNextSibling(Node node, String namespaceURI, String localName)
  {
    return
      selectNextSibling
      (
        node,
        qnameSelector,
        new ExpandedName(namespaceURI, localName)
      );
  }



  private static Node
  selectNextSiblingSibling(Node node, Equal equal, Object refData)
  {
    return
      node == null ?
        null :
        (
          equal.equal(node, refData) ?
            node :
            selectNextSiblingSibling(node.getNextSibling(), equal, refData)
        );
  }



  /**
   * Returns the previous sibling of <code>node</code> that meets the
   * condition expressed by <code>equal</code> and <code>refData</code>.
   * @see be.re.util.Equal
   */

  public static Node
  selectPreviousSibling(Node node, Equal equal, Object refData)
  {
    return
      node == null ?
        null :
        selectPreviousSiblingSibling(node.getPreviousSibling(), equal, refData);
  }



  /**
   * Returns the previous sibling of <code>node</code> called <code>name</code>.
   */

  public static Node
  selectPreviousSibling(Node node, String name)
  {
    return selectPreviousSibling(node, nameSelector, name);
  }



  /**
   * Returns the previous sibling of <code>node</code> with namespace
   * <code>namespaceURI</code> and local name <code>localName</code>.
   */

  public static Node
  selectPreviousSibling(Node node, String namespaceURI, String localName)
  {
    return
      selectPreviousSibling
      (
        node,
        qnameSelector,
        new ExpandedName(namespaceURI, localName)
      );
  }



  private static Node
  selectPreviousSiblingSibling(Node node, Equal equal, Object refData)
  {
    return
      node == null ?
        null :
        (
          equal.equal(node, refData) ?
            node :
            selectPreviousSiblingSibling
            (
              node.getPreviousSibling(),
              equal,
              refData
            )
        );
  }



  public static void
  setTransformerParameters(Transformer transformer, Map parameters)
  {
    for (Iterator i = parameters.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry	entry = (Map.Entry) i.next();

      transformer.
        setParameter((String) entry.getKey(), (String) entry.getValue());
    }
  }



  private static String
  stripMimeTypeParameters(String mimeType)
  {
    int	index = mimeType.indexOf(';');

    return index != -1 ? mimeType.substring(0, index) : mimeType;
  }



  /**
   * Passes a DOM-text-node to a <code>DocumentHandler</code>.
   */

  public static void
  textToDocumentHandler(Text text, DocumentHandler handler) throws SAXException
  {
    String	data = text.getData();
    char[]	chars = new char[data.length()];

    data.getChars(0, chars.length, chars, 0);
    handler.characters(chars, 0, chars.length);
  }

} // Util
