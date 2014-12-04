package be.re.util;

/**
 * @author Werner Donn\u00e9
 */

public class Array

{

  public static Object[]
  append(Object[] array, Object object)
  {
    Object[]	newArray =
      (Object[]) java.lang.reflect.Array.
        newInstance(array.getClass().getComponentType(), array.length + 1);

    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = object;

    return newArray;
  }



  public static byte[]
  append(byte[] array, byte b)
  {
    byte[]	newArray = new byte[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = b;

    return newArray;
  }



  public static char[]
  append(char[] array, char c)
  {
    char[]	newArray = new char[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = c;

    return newArray;
  }



  public static short[]
  append(short[] array, short s)
  {
    short[]	newArray = new short[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = s;

    return newArray;
  }



  public static int[]
  append(int[] array, int i)
  {
    int[]	newArray = new int[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = i;

    return newArray;
  }



  public static long[]
  append(long[] array, long l)
  {
    long[]	newArray = new long[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = l;

    return newArray;
  }



  public static float[]
  append(float[] array, float f)
  {
    float[]	newArray = new float[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = f;

    return newArray;
  }



  public static double[]
  append(double[] array, double d)
  {
    double[]	newArray = new double[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = d;

    return newArray;
  }



  public static Object[]
  append(Object[] array, Object[] objects)
  {
    Object[]	newArray =
      (Object[]) java.lang.reflect.Array.newInstance
      (
        array.getClass().getComponentType(),
        array.length + objects.length
      );

    System.arraycopy(array, 0, newArray, 0, array.length);
    System.arraycopy(objects, 0, newArray, array.length, objects.length);

    return newArray;
  }



  public static byte[]
  append(byte[] array, byte[] bytes)
  {
    byte[]	newArray = new byte[array.length + bytes.length];

    System.arraycopy(array, 0, newArray, 0, array.length);
    System.arraycopy(bytes, 0, newArray, array.length, bytes.length);

    return newArray;
  }



  public static char[]
  append(char[] array, char[] chars)
  {
    char[]	newArray = new char[array.length + chars.length];

    System.arraycopy(array, 0, newArray, 0, array.length);
    System.arraycopy(chars, 0, newArray, array.length, chars.length);

    return newArray;
  }



  public static short[]
  append(short[] array, short[] shorts)
  {
    short[]	newArray = new short[array.length + shorts.length];

    System.arraycopy(array, 0, newArray, 0, array.length);
    System.arraycopy(shorts, 0, newArray, array.length, shorts.length);

    return newArray;
  }



  public static int[]
  append(int[] array, int[] ints)
  {
    int[]	newArray = new int[array.length + ints.length];

    System.arraycopy(array, 0, newArray, 0, array.length);
    System.arraycopy(ints, 0, newArray, array.length, ints.length);

    return newArray;
  }



  public static long[]
  append(long[] array, long[] longs)
  {
    long[]	newArray = new long[array.length + longs.length];

    System.arraycopy(array, 0, newArray, 0, array.length);
    System.arraycopy(longs, 0, newArray, array.length, longs.length);

    return newArray;
  }



  public static float[]
  append(float[] array, float[] floats)
  {
    float[]	newArray = new float[array.length + floats.length];

    System.arraycopy(array, 0, newArray, 0, array.length);
    System.arraycopy(floats, 0, newArray, array.length, floats.length);

    return newArray;
  }



  public static double[]
  append(double[] array, double[] doubles)
  {
    double[]	newArray = new double[array.length + doubles.length];

    System.arraycopy(array, 0, newArray, 0, array.length);
    System.arraycopy(doubles, 0, newArray, array.length, doubles.length);

    return newArray;
  }



  public static boolean
  equal(Object[] array1, Object[] array2)
  {
    if (array1.length != array2.length)
    {
      return false;
    }

    for (int i = 0; i < array1.length; ++i)
    {
      if
      (
        (
          array1[i] == null		&&
          array2[i] != null
        )				||
        (
          array1[i] != null		&&
          array2[i] == null
        )				||
        !array1[i].equals(array2[i])
      )
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  equal(byte[] array1, byte[] array2)
  {
    if (array1.length != array2.length)
    {
      return false;
    }

    for (int i = 0; i < array1.length; ++i)
    {
      if (array1[i] != array2[i])
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  equal(char[] array1, char[] array2)
  {
    if (array1.length != array2.length)
    {
      return false;
    }

    for (int i = 0; i < array1.length; ++i)
    {
      if (array1[i] != array2[i])
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  equal(short[] array1, short[] array2)
  {
    if (array1.length != array2.length)
    {
      return false;
    }

    for (int i = 0; i < array1.length; ++i)
    {
      if (array1[i] != array2[i])
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  equal(int[] array1, int[] array2)
  {
    if (array1.length != array2.length)
    {
      return false;
    }

    for (int i = 0; i < array1.length; ++i)
    {
      if (array1[i] != array2[i])
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  equal(long[] array1, long[] array2)
  {
    if (array1.length != array2.length)
    {
      return false;
    }

    for (int i = 0; i < array1.length; ++i)
    {
      if (array1[i] != array2[i])
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  equal(float[] array1, float[] array2)
  {
    if (array1.length != array2.length)
    {
      return false;
    }

    for (int i = 0; i < array1.length; ++i)
    {
      if (array1[i] != array2[i])
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  equal(double[] array1, double[] array2)
  {
    if (array1.length != array2.length)
    {
      return false;
    }

    for (int i = 0; i < array1.length; ++i)
    {
      if (array1[i] != array2[i])
      {
        return false;
      }
    }

    return true;
  }



  public static boolean
  inArray(Object[] array, Object object)
  {
    return indexOf(array, object) != -1;
  }



  public static boolean
  inArray(byte[] array, byte b)
  {
    return indexOf(array, b) != -1;
  }



  public static boolean
  inArray(char[] array, char c)
  {
    return indexOf(array, c) != -1;
  }



  public static boolean
  inArray(short[] array, short s)
  {
    return indexOf(array, s) != -1;
  }



  public static boolean
  inArray(int[] array, int i)
  {
    return indexOf(array, i) != -1;
  }



  public static boolean
  inArray(long[] array, long l)
  {
    return indexOf(array, l) != -1;
  }



  public static boolean
  inArray(float[] array, float f)
  {
    return indexOf(array, f) != -1;
  }



  public static boolean
  inArray(double[] array, double d)
  {
    return indexOf(array, d) != -1;
  }



  public static int
  indexOf(Object[] array, Object object)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if (array[i].equals(object))
      {
        return i;
      }
    }

    return -1;
  }



  public static int
  indexOf(byte[] array, byte b)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if (array[i] == b)
      {
        return i;
      }
    }

    return -1;
  }



  public static int
  indexOf(char[] array, char c)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if (array[i] == c)
      {
        return i;
      }
    }

    return -1;
  }



  public static int
  indexOf(short[] array, short s)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if (array[i] == s)
      {
        return i;
      }
    }

    return -1;
  }



  public static int
  indexOf(int[] array, int i)
  {
    for (int j = 0; j < array.length; ++j)
    {
      if (array[j] == i)
      {
        return j;
      }
    }

    return -1;
  }



  public static int
  indexOf(long[] array, long l)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if (array[i] == l)
      {
        return i;
      }
    }

    return -1;
  }



  public static int
  indexOf(float[] array, float f)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if (array[i] == f)
      {
        return i;
      }
    }

    return -1;
  }



  public static int
  indexOf(double[] array, double d)
  {
    for (int i = 0; i < array.length; ++i)
    {
      if (array[i] == d)
      {
        return i;
      }
    }

    return -1;
  }



  public static Object[]
  insert(Object[] array, int pos, Object object)
  {
    Object[]	newArray =
      (Object[]) java.lang.reflect.Array.
        newInstance(array.getClass().getComponentType(), array.length + 1);

    System.arraycopy(array, 0, newArray, 0, pos);
    newArray[pos] = object;
    System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);

    return newArray;
  }



  public static byte[]
  insert(byte[] array, int pos, byte b)
  {
    byte[]	newArray = new byte[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    newArray[pos] = b;
    System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);

    return newArray;
  }



  public static char[]
  insert(char[] array, int pos, char c)
  {
    char[]	newArray = new char[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    newArray[pos] = c;
    System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);

    return newArray;
  }



  public static short[]
  insert(short[] array, int pos, short s)
  {
    short[]	newArray = new short[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    newArray[pos] = s;
    System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);

    return newArray;
  }



  public static int[]
  insert(int[] array, int pos, int i)
  {
    int[]	newArray = new int[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    newArray[pos] = i;
    System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);

    return newArray;
  }



  public static long[]
  insert(long[] array, int pos, long l)
  {
    long[]	newArray = new long[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    newArray[pos] = l;
    System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);

    return newArray;
  }



  public static float[]
  insert(float[] array, int pos, float f)
  {
    float[]	newArray = new float[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    newArray[pos] = f;
    System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);

    return newArray;
  }



  public static double[]
  insert(double[] array, int pos, double d)
  {
    double[]	newArray = new double[array.length + 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    newArray[pos] = d;
    System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);

    return newArray;
  }



  public static Object[]
  insert(Object[] array, int pos, Object[] objects)
  {
    Object[]	newArray =
      (Object[]) java.lang.reflect.Array.
        newInstance
        (
          array.getClass().getComponentType(),
          array.length + objects.length
        );

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(objects, 0, newArray, pos, objects.length);
    System.
      arraycopy(array, pos, newArray, pos + objects.length, array.length - pos);

    return newArray;
  }



  public static byte[]
  insert(byte[] array, int pos, byte[] bytes)
  {
    byte[]	newArray = new byte[array.length + bytes.length];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(bytes, 0, newArray, pos, bytes.length);
    System.
      arraycopy(array, pos, newArray, pos + bytes.length, array.length - pos);

    return newArray;
  }



  public static char[]
  insert(char[] array, int pos, char[] chars)
  {
    char[]	newArray = new char[array.length + chars.length];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(chars, 0, newArray, pos, chars.length);
    System.
      arraycopy(array, pos, newArray, pos + chars.length, array.length - pos);

    return newArray;
  }



  public static short[]
  insert(short[] array, int pos, short[] shorts)
  {
    short[]	newArray = new short[array.length + shorts.length];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(shorts, 0, newArray, pos, shorts.length);
    System.
      arraycopy(array, pos, newArray, pos + shorts.length, array.length - pos);

    return newArray;
  }



  public static int[]
  insert(int[] array, int pos, int[] ints)
  {
    int[]	newArray = new int[array.length + ints.length];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(ints, 0, newArray, pos, ints.length);
    System.
      arraycopy(array, pos, newArray, pos + ints.length, array.length - pos);

    return newArray;
  }



  public static long[]
  insert(long[] array, int pos, long[] longs)
  {
    long[]	newArray = new long[array.length + longs.length];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(longs, 0, newArray, pos, longs.length);
    System.
      arraycopy(array, pos, newArray, pos + longs.length, array.length - pos);

    return newArray;
  }



  public static float[]
  insert(float[] array, int pos, float[] floats)
  {
    float[]	newArray = new float[array.length + floats.length];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(floats, 0, newArray, pos, floats.length);
    System.
      arraycopy(array, pos, newArray, pos + floats.length, array.length - pos);

    return newArray;
  }



  public static double[]
  insert(double[] array, int pos, double[] doubles)
  {
    double[]	newArray = new double[array.length + doubles.length];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(doubles, 0, newArray, pos, doubles.length);
    System.
      arraycopy(array, pos, newArray, pos + doubles.length, array.length - pos);

    return newArray;
  }



  public static Object[]
  remove(Object[] array, int pos)
  {
    Object[]	newArray =
      (Object[]) java.lang.reflect.Array.
        newInstance(array.getClass().getComponentType(), array.length - 1);

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(array, pos + 1, newArray, pos, array.length - pos - 1);

    return newArray;
  }



  public static byte[]
  remove(byte[] array, int pos)
  {
    byte[]	newArray = new byte[array.length - 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(array, pos + 1, newArray, pos, array.length - pos - 1);

    return newArray;
  }



  public static char[]
  remove(char[] array, int pos)
  {
    char[]	newArray = new char[array.length - 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(array, pos + 1, newArray, pos, array.length - pos - 1);

    return newArray;
  }



  public static short[]
  remove(short[] array, int pos)
  {
    short[]	newArray = new short[array.length - 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(array, pos + 1, newArray, pos, array.length - pos - 1);

    return newArray;
  }



  public static int[]
  remove(int[] array, int pos)
  {
    int[]	newArray = new int[array.length - 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(array, pos + 1, newArray, pos, array.length - pos - 1);

    return newArray;
  }



  public static long[]
  remove(long[] array, int pos)
  {
    long[]	newArray = new long[array.length - 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(array, pos + 1, newArray, pos, array.length - pos - 1);

    return newArray;
  }



  public static float[]
  remove(float[] array, int pos)
  {
    float[]	newArray = new float[array.length - 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(array, pos + 1, newArray, pos, array.length - pos - 1);

    return newArray;
  }



  public static double[]
  remove(double[] array, int pos)
  {
    double[]	newArray = new double[array.length - 1];

    System.arraycopy(array, 0, newArray, 0, pos);
    System.arraycopy(array, pos + 1, newArray, pos, array.length - pos - 1);

    return newArray;
  }

} // Array
