import java.nio.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Class that allows for the creation of a directory.
 * @author Vlad Diaconu
 */
public class Directory
{
    private String directoryName;
    private String details;
    private int block;
    private INode node;
    private int[] subDirectories = new int[15];
 
    /**
     * Method that allows the construction of a directory.
     * @param data is the data as an array of bytes that contains details
     * about the entry.
     * @param directoryName is the name of the directory that is created.
     * @param details is a string that contains details about the entry such as
     * permissions, size,hard links, date when was last modified.
     * @param block is the block of memory containing the directory.
     * @param pointers is an array of integers pointing towards all subdirectories and
     * files contained in the directory.
     */
    public Directory(byte[] data, String directoryName, String details,int block,int[] pointers)
    {
        this.directoryName = directoryName;
        this.details = details;
        this.block = block;

        node = new INode(data);
        node.extractDetails();
    }
    
    /**
     * @return the name of the directory .
     */
    public String getName()
    {
        return directoryName;
    }

    /**
     * @return the details about the directory.
     */
    public String getDetails()
    {
        return details;
    }

    /**
     * @return the inode that helps identify the directory.
     */
    public INode getINode()
    {
        return node;
    }

    /**
     * @return the block containing the directory.
     */
    public int getBlock()
    {
        return block;
    }

    /**
     *@return an array of pointers towards subdirectories and files
     * within the directory.
     */
    public int[] getSubDirectories()
    {
        return subDirectories;
    }
}   
