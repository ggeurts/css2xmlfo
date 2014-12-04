package be.re.css;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Represents one CSS2 @page rule.
 * @author Werner Donn\u00e9
 */

class PageRule

{

  private int		position;
  private List		properties = new ArrayList();
  private String	name;



  PageRule(String name, int position)
  {
    this.name = name;
    this.position = position;
  }



  void
  addProperty(Property property)
  {
    properties.add(property);
  }



  String
  getName()
  {
    return name;
  }



  int
  getPosition()
  {
    return position;
  }



  Property[]
  getProperties()
  {
    return (Property[]) properties.toArray(new Property[properties.size()]);
  }



  void
  setProperty(Property property)
  {
    for (Iterator i = properties.iterator(); i.hasNext();)
    {
      if (((Property) i.next()).getName().equals(property.getName()))
      {
        i.remove();
      }
    }

    properties.add(property);
  }



  /**
   * Splits this rule into a set of equivalent rules in which there is only one
   * property. For each property of this rule there will be a new one.
   */

  PageRule[]
  split()
  {
    PageRule[]      result = new PageRule[getProperties().length];

    for (int i = 0; i < result.length; ++i)
    {
      result[i] = new PageRule(getName(), getPosition());
      result[i].addProperty(getProperties()[i]);
    }

    return result;
  }

} // PageRule
