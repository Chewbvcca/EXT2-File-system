import java.util.*;
import java .io.*;
import java .nio.*;

/**
 * Class to represent the reading of files byte-wise.
 * @author Vlad Diaconu
 */

public class Volume
{
    private RandomAccessFile file;

    /**
     * Construct the volume of a FileSystem
     * @param file is the name of the file that 
     * the program is looking for.
     */
    public Volume(String file)
    {
        System.out.println("Attempting to read "+ file +" file...");
        try
        {
            this.file = new RandomAccessFile(file, "r");
        }
        catch(IOException ioe)
        {
            System.out.println("Something went wrong while attempting to read "+ file+ " \n" + ioe);
            System.exit(0);
        }
    }

    /**
     * Accessor that retreives the filesystem-image.
     * @return the random access file that was
     * read.
     */
    public RandomAccessFile getRAF()
    {
        return file;
    }

}