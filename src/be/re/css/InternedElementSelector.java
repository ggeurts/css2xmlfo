package be.re.css;

import org.w3c.css.sac.ElementSelector;



public class InternedElementSelector implements ElementSelector

{

  private String	localName;
  private String	namespaceURI;
  private short		selectorType;



  public
  InternedElementSelector(ElementSelector selector)
  {
    localName =
      selector.getLocalName() == null ? null : selector.getLocalName().intern();
    namespaceURI =
      selector.getNamespaceURI() == null ?
        null : selector.getNamespaceURI().intern();
    selectorType = selector.getSelectorType();
  }



  public String
  getLocalName()
  {
    return localName;
  }



  public String
  getNamespaceURI()
  {
    return namespaceURI;
  }



  public short
  getSelectorType()
  {
    return selectorType;
  }

} // InternedElementSelector
