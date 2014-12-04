package be.re.css;

import java.util.Comparator;



/**
 * A comparator for sorting rules from least to most specific. The rules must
 * be split, i.e. they should have exactly one property.
 * @author Werner Donn\u00e9
 */

class RuleComparator implements Comparator

{

  public int
  compare(Object object1, Object object2)
  {
    Rule	rule1 = (Rule) object1;
    Rule	rule2 = (Rule) object2;
    Property	property1 = rule1.getProperty();
    Property	property2 = rule2.getProperty();
    int		result = property1.getName().compareTo(property2.getName());

    if (result == 0)
    {
      result =
        (
          !property1.getImportant() && property2.getImportant() ?
            -1 :
            (
              property1.getImportant() && !property2.getImportant() ? 1 : 0
            )
        );
    }

    if (result == 0)
    {
      result = rule1.getSpecificity() - rule2.getSpecificity();
    }

    if (result == 0)
    {
      result = rule1.getPosition() - rule2.getPosition();
    }

    return result;
  }

} // RuleComparator
