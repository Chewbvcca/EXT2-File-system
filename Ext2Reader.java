import java.io.*;
import java.util.*;
/**
 * Class to represent an ext2File that can read byte-wise
 * and capture the disk name. 
 * @author Vlad Diaconu
 */
public class Ext2Reader
{
    private RandomAccessFile volume;
    private long position = 0L;

    /**
     * Construct the ext2 filesystem-image.
     * @param volume is the volume of the image, 
     * the first thing the program reads.
     */
    public Ext2Reader(Volume volume)
    {
        this.volume = volume.getRAF();
    }

    /**
     * Retreive the name of the (or path to the) filesystem.
     * @return an (expected) string as the name or path to 
     * the filesystem.
     */
    public static String getFileSystem()
    {
        Scanner s = new Scanner(System.in);
        System.out.println("Give the name of the disk to read (if in the same directory with this) or provide the path to reach it.");
        System.out.print("Disk name: ");

        return s.nextLine();
    }

    /**
     * Start reading byte blocks from @param start
     * and continue until @param size is reached.
     * This method may read over the size given.
     * @return readData, and array of bytes
     */
    public byte[] read(long start, long size)
    {
        if(start > 0)
        {

            byte[] readData = new byte[(int) size];
           
            try
            {   
                volume.seek(start);
                volume.readFully(readData);
            }
            catch(IOException ioe)
            {
                System.out.println(ioe);
            }
        
            return readData;
        }
        else
        {
            throw new UnsupportedOperationException("The starting point for reading has to be represented by a positive number");
        }
    }

    /**
     * Start reading byte blocks from the start of 
     * the file and continue until @param size is 
     * reached.
     * This method may read over the size given.
     * @return readData, and array of bytes
     */
    public byte[] readFile(long size)
    {
        byte[] readData = new byte[(int) size];
           
        try
        {   
            volume.seek(position);
            volume.readFully(readData);
        }
        catch(IOException ioe)
        {
            System.out.println(ioe);
        }
        
        return readData;
    }

    /**
     * Start reading byte blocks from the start of 
     * the file and continue until @param size is 
     * reached.
     * This method may read over the size given.
     * @return readData, and array of bytes
     */
    public byte[] readFromFile(Directory dir,long start,long end)
    {
        byte[] readData = new byte[(int)(end  - start)];

        try
        {   
            volume.seek(dir.getBlock() + start);
            volume.readFully(readData);
        }
        catch(IOException ioe)
        {
            System.out.println(ioe);
        }
        
        return readData;
    }

    /**
     * Change the position from which you start reading
     * the bytes with the read() and readFile().
     * @param position is the desired start in order to read
     * bytes.
     */
    public void seek(long position)
    {
        this.position = position;
    }

    /**
     * @return the position from which the program last
     * read bytes with the function read() or readFile().
     */
    public long position()
    {
        return position;
    }
    
}