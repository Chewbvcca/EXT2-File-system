import java.nio.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Class that manages the different levels of indirection of inode pointers of blocks,
 * the directories and their offsets and the reading of files.
 * @author Vlad Diaconu
 */
public class FileInfo
{
    
    private static StringBuilder directoryInformation = new StringBuilder();
    private static LinkedList<Directory> directories = new LinkedList<>();
    private static StringBuilder contentFile = new StringBuilder();

    public FileInfo()
    {}

    /**
     * Method that separates the first 12 direct pointers to data from the other 3 levels of indirection.
     * @param node is the inode of the block the method uses.
     * @param ext2 is the ext2 image the program reads from.
     * @param superblock is the superblock of the system.
     * @param groupDescriptor is the group descriptor of the block.
     */
    public static void divideBlocks(INode node,boolean isFile,Ext2Reader ext2,SuperBlock superblock,GroupDescriptor groupDescriptor)
    {
        directories.clear();

        //get the pointers from the nodes
        int[] blockPointers = node.getPointers();
        

        for(int i = 0; i < 12; ++i)
        {
            //iterate and get data from the first 12 blocks that point to some data
            if(blockPointers[i] != 0)
            {
                extractDirectData(blockPointers[i],isFile,ext2,superblock,groupDescriptor);
            }
        }     
            //perform indirection
            if(blockPointers[12] != 0) extractIndirectData1(blockPointers[12],isFile,ext2,superblock,groupDescriptor);
            
            //perform double indirection
            if(blockPointers[13] != 0) extractIndirectData2(blockPointers[13],isFile,ext2,superblock,groupDescriptor);
           
            //perform triple indirection
            if(blockPointers[14] != 0) extractIndirectData3(blockPointers[14],isFile,ext2,superblock,groupDescriptor);    
    }

    /**
     * Method that iterates through the first indirect pointers to data.
     * @param blockOffset is the offset at which this level of indirection
     * contains pointers to data.
     * @param ext2 is the ext2 image the program reads from.
     * @param superblock is the superblock of the system.
     * @param groupDescriptor is the group descriptor of the block.
     */
    private static void extractIndirectData1(int blockOffset,boolean isFile,Ext2Reader ext2,SuperBlock superblock,GroupDescriptor groupDescriptor)
    {
        
        ext2.seek(blockOffset * Ext2RoutineHandler.blockSize);
        byte[] indirectData = ext2.readFile(superblock.getiNodeSize());
        
        ByteBuffer b = ByteBuffer.wrap(indirectData);
        b.order(ByteOrder.LITTLE_ENDIAN);

        if(isFile == false)
        {
            for(int i = 0; i < b.limit(); i += Ext2RoutineHandler.byte_Value)
            {
                if(b.getInt(i) != 0)
                {
                    extractDirectData(b.getInt(i),isFile,ext2,superblock,groupDescriptor);
                }
            }
        }
        else
        {
            readFileContent(new INode(indirectData),ext2,superblock,groupDescriptor);
        }
    }

    /**
     * Method that iterates through the second indirect pointers to data.
     * @param blockOffset is the offset at which this level of indirection
     * contains pointers to other pointers.
     * @param ext2 is the ext2 image the program reads from.
     * @param superblock is the superblock of the system.
     * @param groupDescriptor is the group descriptor of the block.
     * NOTE: This method calls to the above one until the direct reading
     * is possible.
     */
    private static void extractIndirectData2(int blockOffset,boolean isFile,Ext2Reader ext2,SuperBlock superblock,GroupDescriptor groupDescriptor)
    {

        ext2.seek(blockOffset * Ext2RoutineHandler.blockSize);
        byte[] indirectData = ext2.readFile(superblock.getiNodeSize());
        ByteBuffer b = ByteBuffer.wrap(indirectData);
        b.order(ByteOrder.LITTLE_ENDIAN);

        for(int i = 0; i < b.limit(); i += Ext2RoutineHandler.byte_Value)
        {
            if(b.getInt(i) != 0)
            {     
                extractIndirectData1(b.getInt(i),isFile,ext2,superblock,groupDescriptor);
            }
        }
    }

    /**
     * Method that iterates through the third indirect pointers to data.
     * @param blockOffset is the offset at which this level of indirection
     * contains pointers to other pointers.
     * @param ext2 is the ext2 image the program reads from.
     * @param superblock is the superblock of the system.
     * @param groupDescriptor is the group descriptor of the block.
     * NOTE: This method calls to the above one until the direct reading
     * is possible.
     */
    private static void extractIndirectData3(int blockOffset,boolean isFile,Ext2Reader ext2,SuperBlock superblock,GroupDescriptor groupDescriptor)
    {

        ext2.seek(blockOffset * Ext2RoutineHandler.blockSize);
        byte[] indirectData = ext2.readFile(superblock.getiNodeSize());
        ByteBuffer b = ByteBuffer.wrap(indirectData);
        b.order(ByteOrder.LITTLE_ENDIAN);

        for(int i = 0; i < b.limit(); i += Ext2RoutineHandler.byte_Value)
        {
            if(b.getInt(i) != 0)
            {
                extractIndirectData2(b.getInt(i),isFile,ext2,superblock,groupDescriptor);
            }
        }
    }

    /**
     * Method that iterates through the inodes of the directory we are
     * currently search for.
     * @param blockOffset is the offset at which data has been found.
     * @param ext2 is the ext2 image the program reads from.
     * @param superblock is the superblock of the system.
     * @param groupDescriptor is the group descriptor of the block.
     */
    private static void extractDirectData(int blockOffset,boolean isFile,Ext2Reader ext2,SuperBlock superblock,GroupDescriptor groupDescriptor)
    {
            /* Load the current directory details. */
        ext2.seek(blockOffset * Ext2RoutineHandler.blockSize);
        byte[] data = ext2.readFile(Ext2RoutineHandler.blockSize);
       
        ByteBuffer b = ByteBuffer.wrap(data);
        b.order(ByteOrder.LITTLE_ENDIAN);

        short directorySize;

                    /* Display the details of subdirectories/files. */
            for(int i = 0; i < b.limit(); i += directorySize)
            {
                
                int iNodeOffset = b.getInt(i);
                
                int currentBlock = getCurrentINodeBlock(iNodeOffset,superblock,groupDescriptor);
               
                ext2.seek(currentBlock);
                byte[] data2 = ext2.readFile(Ext2RoutineHandler.blockSize);
                
                INode node = new INode(data2);
                node.extractDetails();

                //use the size of each directory as an iterator
                directorySize = b.getShort(i + Ext2RoutineHandler.byte_Value);

                byte directoryNameSize = b.get(i + Ext2RoutineHandler.directoryNameOffset);

                byte[] directoryName = new byte[directoryNameSize];
                
                //convert the name of the directory to a string byte by byte
                for(int j = 0 ; j < directoryName.length; ++j)
                {
                    directoryName[j] = b.get(i + j + (Ext2RoutineHandler.byte_Value * 2));
                }

                directoryInformation.append(
                                node.getPermissions() + "   " +
                                node.getHardLinks()   + "   " +
                                node.getUserID()      + "   " +
                                node.getGroupID()     + "   " +
                                node.getSize()        + "   " +
                                node.getDate()        + "   " +
                                new String(directoryName).trim()
                                );
                directoryInformation.append("\n");

                Directory dir = new Directory(
                                                data2, 
                                                new String(directoryName).trim(),
                                                directoryInformation.toString(),
                                                currentBlock,
                                                node.getPointers()
                                             );
                                            
                directories.add(dir);
                directoryInformation.setLength(0);

            }
    }

    /**
     * Method to determine the block within which an inode
     * resides.
     * All calculations and formulas applied in this method
     * are references to this article.
     * https://wiki.osdev.org/Ext2#Inode_Type_and_Permissions
     */
    private static int getCurrentINodeBlock(int iNodeOffset, SuperBlock superblock,GroupDescriptor groupDescriptor)
    {
        int total = superblock.getiNodeNumber();

        //if the inode is between the root and the total number of inodes in the filesystem.
        if(iNodeOffset >= 2 && iNodeOffset < total)
        {
            int iNodesInGroup = superblock.getiNodesInGroup();
            int iNodeSize = superblock.getiNodeSize();
            int[] pointers = groupDescriptor.getPointers();
            int iNodeTablePointer;

            double currentBlock;
            
            //make the block offset start from 0 not from 1 as with inodes.
            iNodeOffset--;
            iNodeTablePointer = pointers[iNodeOffset / iNodesInGroup];

            //generate the block within data resides in.
            currentBlock = ((((double) iNodeOffset % iNodesInGroup) * iNodeSize / Ext2RoutineHandler.blockSize) + iNodeTablePointer) * Ext2RoutineHandler.blockSize;

                return (int) currentBlock;
        }

        return 0;
    }

    /**
     * Method that returns a list of all subdirectories from within
     * a directory.
     * @param directories is a list containing all the files/subdirectories
     * of a directory.
     */
    public static LinkedList<Directory> getDirectories()
    {
        return directories;
    }

    /**
     * Method that allows the conversion of bytes to readable
     * human ASCII code if a readable file is found.
     * @param node is the inode that points to this file.
     * @param ext2 is the filesystem the program reads from.
     */
    public static void readFileContent(INode node,Ext2Reader ext2,SuperBlock superblock,GroupDescriptor groupDescriptor)
    {
        node.extractDetails();

        int[] copy = node.getPointers();
                    
        for(int m = 0; m < 12; ++m)
        {    
            if(copy[m] != 0)
            {
                ext2.seek(copy[m] * Ext2RoutineHandler.blockSize);
                byte[] c = ext2.readFile(Ext2RoutineHandler.blockSize);
                
                for(int n = 0; n < c.length; ++n)
                    contentFile.append((char)c[n]);
            }
        }
        if(copy[12] != 0)
        extractIndirectData1(copy[12],true,ext2,superblock,groupDescriptor);
        if(copy[13] != 0) 
        extractIndirectData2(copy[13],true,ext2,superblock,groupDescriptor);
        if(copy[14] != 0) 
        extractIndirectData3(copy[14],true,ext2,superblock,groupDescriptor);
    }

    public static void CAT()
    {

        System.out.println(contentFile.toString());
        contentFile.setLength(0);
    }
}