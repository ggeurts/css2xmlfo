package be.re.io;

import java.io.File;
import java.io.IOException;

public class Util
{
    /**
     * Creates a temporary directory. When the resulting
     * <code>java.io.File</code> object is garbage collected, the directory will
     * not be deleted along. If the caller hasn't deleted it the file will be
     * deleted on exit of the VM.
     */
    public static File createTempFile(String prefix, String suffix) throws IOException
    {
        return createTempFile(prefix, suffix, null);
    }

    /**
     * Creates a temporary file. When the resulting <code>java.io.File</code>
     * object is garbage collected, the file will not be deleted along. If the
     * caller hasn't deleted it the file will be deleted on exit of the VM.
     */
    public static File createTempFile(String prefix, String suffix, File directory) throws IOException
    {
        File result = File.createTempFile(prefix, suffix, directory);
        result.deleteOnExit();
        return result;
    }
} // Util
