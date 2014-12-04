package be.re.util;

import java.util.HashSet;
import java.util.Set;



public class DigitalTree implements Cloneable

{

  private static final int	BITS = 8;
  private static final int	MASK = (0x1 << BITS) - 1;
  private static final int	SIZE = (0x1 << BITS);

  private Set		keys;
  private Node[]	root = new Node[SIZE];



  public
  DigitalTree()
  {
    this(false);
  }



  public
  DigitalTree(boolean saveKeys)
  {
    keys = saveKeys ? new HashSet() : null;
  }



  /**
   * Returns a shallow copy.
   */

  public Object
  clone()
  {
    DigitalTree	copy = null;

    try
    {
      copy = (DigitalTree) super.clone();
    }

    catch (CloneNotSupportedException e)
    {
      return null;
    }

    copy.root = cloneNodes(root);

    if (keys != null)
    {
      copy.keys = (Set) ((HashSet) keys).clone();
    }

    return copy;
  }



  private static Node[]
  cloneNodes(Node[] nodes)
  {
    Node[]	result = new Node[nodes.length];

    for (int i = 0; i < nodes.length; ++i)
    {
      if (nodes[i] != null)
      {
        result[i] = (Node) nodes[i].clone();
      }
    }

    return result;
  }



  public Object
  get(String key)
  {
    Node[]	current = root;
    int		i = 0;
    int		length = key.length();
    Node	node = null;

    for (i = 0; i < length && current != null; ++i)
    {
      char	c = key.charAt(i);

      for (int j = 0; j < 16 / BITS && current != null; ++j)
      {
        node = current[((MASK << (j * BITS)) & c) >>> (j * BITS)];
        current = node != null ? node.nodes : null;
      }
    }

    return i == length && node != null ? node.object : null;
  }



  public Set
  keySet()
  {
    return keys;
  }



  public void
  put(String key, Object o)
  {
    Node[]	current = root;
    int		length = key.length();
    Node	node = null;

    for (int i = 0; i < length; ++i)
    {
      char	c = key.charAt(i);

      for (int j = 0; j < 16 / BITS; ++j)
      {
        int	value = ((MASK << (j * BITS)) & c) >>> (j * BITS);

        if (current[value] == null)
        {
          current[value] = new Node();
        }

        if
        (
          current[value].nodes == null	&&
          (
            i < length - 1		||
            j < (16 / BITS) - 1
          )
        )
        {
          current[value].nodes = new Node[SIZE];
        }

        node = current[value];
        current = current[value].nodes;
      }
    }

    node.object = o;

    if (keys != null)
    {
      keys.add(key);
    }
  }



  public void
  remove(String key)
  {
    put(key, null);
  }



  private static class Node implements Cloneable

  {

    private Node[]	nodes;
    private Object	object;



    protected Object
    clone()
    {
      Node	copy = null;

      try
      {
        copy = (Node) super.clone();
      }

      catch (CloneNotSupportedException e)
      {
        return null;
      }

      if (nodes != null)
      {
        copy.nodes = cloneNodes(nodes);
      }

      return copy;
    }

  } // Node

} // DigitalTree
