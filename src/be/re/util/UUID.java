package be.re.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;



/**
 * Generates a version 1 UUID according to RFC 4122.
 * @author Werner Donn\u00e9
 */

public class UUID

{

  private static final Random	random = new SecureRandom();
  private static final long	startGregorianSinceEpoch =
    -10010304000000L - // Gregorian in milliseconds since NTP (1/1/1900).
    2208988800000L; // Epoch in milliseconds since NTP.

  private static long		lastTimestamp = 0;
  private static long		timestampCounter = 0;



  static
  {
    random.setSeed(System.currentTimeMillis());
  }



  public static String
  format(byte[] uuid)
  {
    if (uuid.length != 16)
    {
      throw new IllegalArgumentException("UUID should be 16 bytes.");
    }

    char[]	result = new char[36];

    format(uuid, 0, 4, result, 0); // time_low
    result[8] = '-';
    format(uuid, 4, 2, result, 9); // time_mid
    result[13] = '-';
    format(uuid, 6, 2, result, 14); // time_hi
    result[18] = '-';
    format(uuid, 8, 2, result, 19); // time_hi
    result[23] = '-';
    format(uuid, 10, 6, result, 24); // time_hi

    return new String(result);
  }



  private static void
  format(byte[] uuid, int offsetUUID, int len, char[] result, int offsetResult)
  {
    for (int i = 0; i < len; ++i)
    {
      result[offsetResult + 2 * i] =
        toHex((0x00ff & uuid[i + offsetUUID]) >>> 4);
      result[offsetResult + 2 * i + 1] =
        toHex(0x000f & uuid[i + offsetUUID]);
    }
  }



  public static synchronized byte[]
  generate()
  {
    byte[]	clockSequence = new byte[2];
    byte[]	node = new byte[6];
    byte[]	result = new byte[16];

    setTimestamp(result);
    result[7] &= 0x1f; // version
    random.nextBytes(clockSequence);
    clockSequence[0] &= 0xbf; // variant
    System.arraycopy(clockSequence, 0, result, 8, clockSequence.length);
    random.nextBytes(node);
    node[0] |= 0x80; // multicast bit
    System.arraycopy(node, 0, result, 10, node.length);

    return result;
  }



  public static String
  generateFormatted()
  {
    return format(generate());
  }



  public static boolean
  isUUID(String s)
  {
    if (s.length() != 36)
    {
      return false;
    }

    for (int i = 0; i < 36; ++i)
    {
      if (i == 8 || i == 13 || i == 18 || i == 23)
      {
        if (s.charAt(i) != '-')
        {
          return false;
        }
      }
      else
      {
        char	c = s.charAt(i);

        if
        (
          !(
            (c >= '0' && c <= '9')	||
            (c >= 'a' && c <= 'f')	||
            (c >= 'A' && c <= 'F')
          )
        )
        {
          return false;
        }
      }
    }

    return true;
  }



  public static void
  main(String[] args)
  {
    System.out.println(generateFormatted());
  }



  private static void
  setTimestamp(byte[] result)
  {
    long	time =
      (System.currentTimeMillis() + startGregorianSinceEpoch) * 10000;

    if (time != lastTimestamp)
    {
      lastTimestamp = time;
      timestampCounter = 0;
    }

    time += timestampCounter++;

    ByteArrayOutputStream	out = new ByteArrayOutputStream(8);

    try
    {
      new DataOutputStream(out).writeLong(time);
    }

    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    byte[]	bytes = out.toByteArray();

    System.arraycopy(bytes, 4, result, 0, 4); // time_low
    System.arraycopy(bytes, 2, result, 4, 2); // time_mid
    System.arraycopy(bytes, 0, result, 6, 2); // time_hi
  }



  private static char
  toHex(int i)
  {
    return (char) (i + (i < 10 ? 48 : 87));
  }

} // UUID
