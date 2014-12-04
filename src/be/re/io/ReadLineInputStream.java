package be.re.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;



/**
 * @author Werner Donn\u00e9
 */

public class ReadLineInputStream extends FilterInputStream

{

  public
  ReadLineInputStream(InputStream in)
  {
    super(new PushbackInputStream(in));
  }



  /**
   * Reads bytes until '\r', '\n' or "\r\n" is encountered and returns them in
   * an array. The line termination sequence is discarded and not included in
   * the array. If the end of the stream has been reached <code>null</code> is
   * returned.
   */

  public byte[]
  readLine() throws IOException
  {
    return readLine(new byte[1024], 0);
  }



  private byte[]
  readLine(byte[] buffer, int off) throws IOException
  {
    Arrays.fill(buffer, off, buffer.length, (byte) 0);

    int	i;

    for
    (
      i = off;
      i < buffer.length && (buffer[i] = (byte) in.read()) != '\n' &&
        buffer[i] != '\r' && buffer[i] != -1;
      ++i
    );

    if (i < buffer.length)
    {
      if (i == 0 && buffer[i] == -1)
      {
        return null;
      }

      byte[]	result = new byte[i];

      System.arraycopy(buffer, off, result, 0, result.length);

      if ((char) buffer[i] == '\r')
      {
        byte	b = (byte) in.read();

        if (b != '\n')
        {
          ((PushbackInputStream) in).unread(b);
        }
      }

      return result;
    }

    return readLine(realloc(buffer), i);
  }



  private static byte[]
  realloc(byte[] buffer)
  {
    byte[]	result = new byte[buffer.length * 2];

    System.arraycopy(buffer, 0, result, 0, buffer.length);

    return result;
  }

} // ReadLineInputStream
