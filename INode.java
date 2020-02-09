import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Class to represent a inode within a filesystem.
 * @author Vlad Diaconu
 */
public class INode
{
    private ByteBuffer buffer;
    
    private short type;
    private short userID;
    private int lastTimeModified;
    private int lastTimeAccessed;
    private int groupID;
    private int sizeUpper;
    private int sizeLower;
    private long fileSize;
    private int hardLinks;
    private int[] blockPointers;

    private boolean file;
    private StringBuilder permissions;

    /**
     * Method to create an inode.
     * @param content is the byte array containing information about the inode.
     */
    public INode(byte[] content)
    {
        buffer = ByteBuffer.wrap(content);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        blockPointers = new int[15];
    }

    /**
     * Method to extract the contents of an iNode given specific offsets.
     * The method also prints details about the iNode such as:
     * @param type which is linked to the permissions section of the iNode.
     * @param userID which is the id of the user.
     * @param lastTimeAccessed is the time gone since the file was last accessed.
     * @param lastTimeModified is the time gone since the file was last modified.
     * @param groupID is the id of the group to which the file belongs to.
     * @param hardLinks is the number of references of the file.
     * @param blockPointer is the number of indirected references the iNode has.
     */
    public void extractDetails()
    {
        permissions = new StringBuilder();

        type = buffer.getShort(Ext2RoutineHandler.iNode_typeOffset);
        userID = buffer.getShort(Ext2RoutineHandler.iNode_userIDOffset);
        lastTimeAccessed = buffer.getInt(Ext2RoutineHandler.iNode_lastAccessOffset);
        sizeUpper = buffer.getInt(Ext2RoutineHandler.iNode_upperBitsOffset);
        sizeLower = buffer.getInt(Ext2RoutineHandler.iNode_lowerBitsOffset);
        lastTimeModified = buffer.getInt(Ext2RoutineHandler.iNode_lastModificationOffset);
        groupID = buffer.getShort(Ext2RoutineHandler.iNode_groupIDOffset);
        hardLinks = buffer.getShort(Ext2RoutineHandler.iNode_hardLinksOffset);

        fileSize = (long) (sizeUpper << 32 | sizeLower & 0xffffffl);

        for(int i = 0; i < blockPointers.length; ++i)
        {
            blockPointers[i] = buffer.getInt(4 * i + Ext2RoutineHandler.iNode_blockPointerOffset);
        }

                        /* "TYPE" BLOCK OF PERMISSIONS */
        if((int)(type & Ext2RoutineHandler.IFSCK) == Ext2RoutineHandler.IFSCK)
            permissions.append("s");
        else if((int)(type & Ext2RoutineHandler.IFLNK) == Ext2RoutineHandler.IFLNK)
            permissions.append("l");
        else if((int)(type & Ext2RoutineHandler.IFREG) == Ext2RoutineHandler.IFREG)
           { permissions.append("-"); file = true;}
        else if((int)(type & Ext2RoutineHandler.IFBLK) == Ext2RoutineHandler.IFBLK)
            permissions.append("b");
        else if((int)(type & Ext2RoutineHandler.IFDIR) == Ext2RoutineHandler.IFDIR)
           { permissions.append("d"); file = false;}
        else if((int)(type & Ext2RoutineHandler.IFCHR) == Ext2RoutineHandler.IFCHR)
            permissions.append("c");
        else if((int)(type & Ext2RoutineHandler.IFIFO) == Ext2RoutineHandler.IFIFO)
            permissions.append("p");
        
                        /* "USER" BLOCK OF PERMISSIONS */
        if((int)(type & Ext2RoutineHandler.IRUSR) == Ext2RoutineHandler.IRUSR)
            permissions.append("r");
        else
            permissions.append("-");
        if((int)(type & Ext2RoutineHandler.IWUSR) == Ext2RoutineHandler.IWUSR)
            permissions.append("w");
        else
            permissions.append("-");
        if((int)(type & Ext2RoutineHandler.IXUSR) == Ext2RoutineHandler.IXUSR)
            permissions.append("x");
        else
            permissions.append("-");

                        /* "GROUP" BLOCK OF PERMISSIONS */
        if((int)(type & Ext2RoutineHandler.IRGRP) == Ext2RoutineHandler.IRGRP)
            permissions.append("r");
        else
            permissions.append("-");
        if((int)(type & Ext2RoutineHandler.IWGRP) == Ext2RoutineHandler.IWGRP)
            permissions.append("w");
        else
            permissions.append("-");
        if((int)(type & Ext2RoutineHandler.IXGRP) == Ext2RoutineHandler.IXGRP)
            permissions.append("x");
        else
            permissions.append("-");

                        /* "OTHER" BLOCK OF PERMISSIONS */
        if((int)(type & Ext2RoutineHandler.IROTH) == Ext2RoutineHandler.IROTH)
            permissions.append("r");
        else
            permissions.append("-");
        if((int)(type & Ext2RoutineHandler.IWOTH) == Ext2RoutineHandler.IWOTH)
            permissions.append("w");
        else
            permissions.append("-");
        if((int)(type & Ext2RoutineHandler.IXOTH) == Ext2RoutineHandler.IXOTH)
            permissions.append("x");
        else
            permissions.append("-");
        if((int)(type & Ext2RoutineHandler.ISVTX) == Ext2RoutineHandler.ISVTX)
            permissions.append("t");
        else
            permissions.append("");
    }

    /**
     * @return the user ID of certain file.
     */
    public String getUserID()
    {
        if(userID == 0)
            return "root";
        else
            return "user";
    }

    /**
     * @return the group ID of certain file.
     */
    public String getGroupID()
    {
        if(groupID == 0)
            return "root";
        else
            return "staff";
    }

    /**
     * @return the full permissions of a file
     */
    public String getPermissions()
    {
        return permissions.toString();
    }

    /**
     * @return a boolean stating if the accessed block corresponds 
     * to a regular file (TRUE) or a directory (FALSE).
     */
    public boolean isFile()
    {
       return file;
    }

    /**
     * @return the size of the file that the inode points to.
     */
    public long getSize()
    {
        return fileSize;
    }

    /**
     * @return the number of references (hard links) that a
     * file has.
     */
    public int getHardLinks()
    {
        return hardLinks;
    }

    /**
     * @return an array of pointers to blocks of data within the
     * inode.
     */
    public int[] getPointers()
    {
        return blockPointers;
    }

    /**
     * @return the date of when the file was last modified.
     */
    public Date getDate()
    {
        return new Date((long)lastTimeModified * 1000);
    }


}