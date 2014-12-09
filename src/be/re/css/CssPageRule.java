package be.re.css;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents one CSS2 @page rule.
 *
 * @author Werner Donn\u00e9
 */
public class CssPageRule
{
    private final List<Property> properties = new ArrayList<>();
    private final String name;

    CssPageRule(String name)
    {
        this.name = name;
    }

    void addProperty(Property property)
    {
        properties.add(property);
    }

    String getName()
    {
        return name;
    }

    List<Property> getProperties()
    {
        return properties;
    }

    void setProperty(Property property)
    {
        for (Iterator<Property> i = properties.iterator(); i.hasNext();)
        {
            if (i.next().getName().equals(property.getName()))
            {
                i.remove();
            }
        }
        properties.add(property);
    }
} // PageRule
