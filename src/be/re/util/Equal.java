package be.re.util;

/**
 * The Equal interface is meant for generic algorithms that need to be able to
 * test two objects for equality. The comparison criterion is kept out of the
 * algorithm.
 * It is also kept out of the objects themselves. This would not be the case
 * if <code> Object.equals </code> were used. That method implies only one
 * criterion.
 */

public interface Equal
{
  /**
   * The actual equal method.
   * @param object the object encountered in the course of the algorithm.
   * @param refData the reference data the encountered object is compared with.
   * Normally this data is constant during the algorithm.
   * @return <code> true </code> if the objects are equal according to the
   * criterion, <code> false </code> otherwise.
   */

  public boolean	equal	(Object object, Object refData);
}
