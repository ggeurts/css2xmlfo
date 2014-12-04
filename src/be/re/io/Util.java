package be.re.io;

import be.re.util.UUID;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;



public class Util

{

  private static ResourceBundle	bundle = null;
  private static boolean	deleteOnExitHookAdded = false;
  private static final Set	dirsToDelete = new HashSet();



  /**
   * Creates a temporary directory. When the resulting <code>java.io.File</code>
   * object is garbage collected, the directory will not be deleted along. If
   * the caller hasn't deleted it the file will be deleted on exit of the VM.
   */

  public static File
  createTempDir(String prefix, String suffix, File directory)
    throws IOException
  {
    final File	result = createTempFileName(prefix, suffix, directory);

    if (!result.mkdir())
    {
      throw
        new IOException
        (
          "Can't create temporary directory \"" + result.getAbsolutePath() +
            "\"."
        );
    }

    deleteDirOnExit(result);

    return
      new File(result.getAbsolutePath())
      {
        public boolean
        delete()
        {
          synchronized (dirsToDelete)
          {
            dirsToDelete.remove(result);
          }

          return result.delete();
        }
      };
  }



  public static File
  createTempFile(String prefix, String suffix) throws IOException
  {
    return createTempFile(prefix, suffix, null);
  }



  /**
   * Creates a temporary file. When the resulting <code>java.io.File</code>
   * object is garbage collected, the file will not be deleted along. If the
   * caller hasn't deleted it the file will be deleted on exit of the VM.
   */

  public static File
  createTempFile(String prefix, String suffix, File directory)
    throws IOException
  {
    File	result = createTempFileName(prefix, suffix, directory);

    if (!result.createNewFile())
    {
      throw
        new IOException
        (
          "Can't create temporary file \"" + result.getAbsolutePath() + "\"."
        );
    }

    result.deleteOnExit();

    return result;
  }



  public static File
  createTempFileName(String prefix, String suffix, File directory)
  {
    return
      new File
      (
        directory != null ?
          directory : new File(System.getProperty("java.io.tmpdir")),
        (prefix != null ? prefix : "") + UUID.generateFormatted() +
          (suffix != null ? suffix : ".tmp")
      );
  }



  public static boolean
  deleteDir(File dir)
  {
    if (dir.isDirectory())
    {
      File[]	files = dir.listFiles();

      for (int i = 0; i < files.length; ++i)
      {
        deleteDir(files[i]);
      }
    }

    return dir.delete();
  }



  public static synchronized void
  deleteDirOnExit(File dir)
  {
    if (!deleteOnExitHookAdded)
    {
      deleteOnExitHookAdded = true;

      Runtime.getRuntime().addShutdownHook
      (
        new Thread
        (
          new Runnable()
          {
            public void
            run()
            {
              synchronized (dirsToDelete)
              {
                for (Iterator i = dirsToDelete.iterator(); i.hasNext();)
                {
                  deleteDir((File) i.next());
                }
              }
            }
          }
        )
      );
    }

    synchronized (dirsToDelete)
    {
      dirsToDelete.add(dir);
    }
  }



  static String
  getResource(String key)
  {
    if (bundle == null)
    {
      bundle = ResourceBundle.getBundle("be.re.io.res.Res");
    }

    return bundle.getString(key);
  }



  public static boolean
  isSymbolicLink(File file) throws IOException
  {
    return !file.getCanonicalPath().equals(file.getAbsolutePath());
  }

} // Util
