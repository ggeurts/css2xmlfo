package be.re.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



/**
 * This class reads data from an input stream and writes it to an output stream
 * in a separate thread. The thread stops when the input stream is closed
 * or when an exception occurs.
 * @author Werner Donn\u00e9
 */

public class StreamConnector

{

  private IOException	exception = null;
  private Thread	thread;



  public
  StreamConnector(InputStream in, OutputStream out)
  {
    this(in, out, true, true);
  }



  public
  StreamConnector
  (
    InputStream		in,
    OutputStream	out,
    boolean		closeInput,
    boolean		closeOutput
  )
  {
    this(in, out, 0x10000, closeInput, closeOutput);
  }



  public
  StreamConnector
  (
    InputStream		in,
    OutputStream	out,
    boolean		closeInput,
    boolean		closeOutput,
    boolean		flush
  )
  {
    this(in, out, 0x10000, closeInput, closeOutput, flush);
  }



  public
  StreamConnector
  (
    InputStream		in,
    OutputStream	out,
    int			bufferSize,
    boolean		closeInput,
    boolean		closeOutput
  )
  {
    this(in, out, bufferSize, closeInput, closeOutput, true);
  }



  public
  StreamConnector
  (
    final InputStream	in,
    final OutputStream	out,
    final int		bufferSize,
    final boolean	closeInput,
    final boolean	closeOutput,
    final boolean	flush
  )
  {
    (
      thread =
        new Thread
        (
          new Runnable()
          {
            public void
            run()
            {
              try
              {
                copy
                (
                  in,
                  out,
                  bufferSize,
                  closeInput,
                  closeOutput,
                  flush
                );
              }

              catch (IOException e)
              {
                exception = e;
              }
            }
          }
        )
    ).start();
  }



  public static void
  copy(InputStream in, OutputStream out) throws IOException
  {
    copy(in, out, true, true);
  }



  public static void
  copy
  (
    InputStream		in,
    OutputStream	out,
    boolean		closeInput,
    boolean		closeOutput
  ) throws IOException
  {
    copy(in, out, 0x10000, closeInput, closeOutput);
  }



  public static void
  copy
  (
    InputStream		in,
    OutputStream	out,
    boolean		closeInput,
    boolean		closeOutput,
    boolean		flush
  ) throws IOException
  {
    copy(in, out, 0x10000, closeInput, closeOutput, flush);
  }



  public static void
  copy
  (
    InputStream		in,
    OutputStream	out,
    int			bufferSize,
    boolean		closeInput,
    boolean		closeOutput
  ) throws IOException
  {
    copy(in, out, bufferSize, closeInput, closeOutput, true);
  }



  public static void
  copy
  (
    InputStream		in,
    OutputStream	out,
    int			bufferSize,
    boolean		closeInput,
    boolean		closeOutput,
    boolean		flush
  ) throws IOException
  {
    byte[]	buffer = new byte[bufferSize];
    int		len;

    while ((len = in.read(buffer)) != -1)
    {
      out.write(buffer, 0, len);

      if (flush)
      {
        out.flush();
      }
    }

    if (closeInput)
    {
      in.close();
    }

    if (closeOutput)
    {
      out.close();
    }
    else
    {
      if (flush)
      {
        out.flush();
      }
    }
  }



  /**
   * Breaks the connection. It doesn't touch the streams.
   */

  public void
  disconnect()
  {
    thread.interrupt();
  }



  /**
   * Joins with the thread implementing the io processing.
   */

  public void
  join() throws IOException, InterruptedException
  {
    thread.join();

    if (exception != null)
    {
      throw exception;
    }
  }

} // StreamConnector
