package be.re.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;



public class FlushOutputStream extends FilterOutputStream

{

  public
  FlushOutputStream(OutputStream out)
  {
    super(out);
  }



  public void
  write(byte b) throws IOException
  {
    out.write(b);
    out.flush();
  }

} // FlushOutputStream
