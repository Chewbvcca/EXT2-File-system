import java.nio.*;
import java.util.*;
/**
 * Class to represent the superblock from the volume.
 * In the ext2 file system this always starts at byte offset = 1024.
 * @author Vlad Diaconu
 */
 public class SuperBlock
 {
    private String volumeName; 
    private ByteBuffer buffer;

    private short magicNumber;
    private int iNodeNumber;
    private int blockNumber;
    private int blocksInGroup;
    private int iNodesInGroup;
    private int groupNumber = 0;
    private int iNodeSize;
    private int fileSystemBlockSize;
    private String magicNumberCopy;

    /**
     * Constructor that allows the creation of the superblock 
     * within any volume of a ext2 type of file.
     * @param content is an array of bytes read from the filesystem-image.
     * The constructor also modifies the order the bytes within the buffer 
     * by @param ByteOrder.LITTLE_ENDIAN (MSB is first).
     */
    public SuperBlock(byte[] content)
    {
        buffer = ByteBuffer.wrap(content);

        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Method that extracts the details of the file from the superblock.
     * The method uses the specified offsets to reach specific blocks of
     * information and convert that to either hex or int.
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * The method also prints the extracted details on the command line.
     * @param volumeName = the name of the File.
     * @param magicNumberCopy = hex representation of the ext2 "magic number".
     * @param iNodeNumber = the total number of inodes in the given file.
     * @param iNodesInGroup = the total number of inodes in a given group.
     * @param iNodeSize = the size of all the inodes in the volume.
     * @param blockNumber = the number of blocks in the volume.
     * @param blocksInGroup = the number of blocks in a given group.
     * @param fileSystemBlockSize = the size of a block in the filesystem.
     * @param groupNumber = total number of groups in a file.
     */
    public void extractDetails()
    {
        byte[] file = new byte[Ext2RoutineHandler.fileSystemNameSize];
        
        for(int i = 0; i < Ext2RoutineHandler.fileSystemNameSize; ++i)
            file[i] = buffer.get(Ext2RoutineHandler.fileSystemOffset + i);

        volumeName = new String(file);
        System.out.println("~~~~~FILE |"+ volumeName + "| DETAILS~~~~~");

        magicNumber = buffer.getShort(Ext2RoutineHandler.magicNumberOffset);
        magicNumberCopy = Integer.toHexString(magicNumber - 0xffff0000);
        System.out.println("Magic number: 0x" + magicNumberCopy);

        iNodeNumber = buffer.getInt(Ext2RoutineHandler.iNodeCounter);
        System.out.println("Number of iNodes in file: " + iNodeNumber);

        blockNumber = buffer.getInt(Ext2RoutineHandler.blockCounter);
        System.out.println("Number of blocks: " + blockNumber);

        blocksInGroup = buffer.getInt(Ext2RoutineHandler.blocksInGroup);
        System.out.println("Number of blocks in a group: " + blocksInGroup);

        iNodesInGroup = buffer.getInt(Ext2RoutineHandler.iNodesInGroup);
        System.out.println("Number of iNodes in a group: " + iNodesInGroup);
        
        iNodeSize = buffer.getInt(Ext2RoutineHandler.iNodeSize);
        System.out.println("Size of a iNode in file: " + iNodeSize);

        fileSystemBlockSize = 1024 * (int)Math.pow(2,buffer.getInt(Ext2RoutineHandler.fileSystemBlockSizeOffset));
        System.out.println("Block size of filesystem: " + fileSystemBlockSize);

        groupNumber =(int)(blockNumber / blocksInGroup);
        if((blockNumber % blocksInGroup) != 0)
            groupNumber += 1;
        System.out.println("Number of block groups is: " + groupNumber);

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    /**
     * Accessor that retrieves the name of the volume.
     * @return the name of the filesystem as a string
     */
    public String getVolume()
    { return volumeName;}

    /**
     * Accessor that retrieves the "magic number" of 
     * any ext2 file.
     * @return a int representation of the number.
     */
    public int getMagicNumber()
    {return magicNumber;}

    /**
     * Accessor that retrieves the total number of inodes
     * in the file.
     * @return a int representing the total number of 
     * inodes in the file.
     */
    public int getiNodeNumber()
    { return iNodeNumber;}

    /**
     * Accessor that retrieves the total number of blocks
     * of information from the file.
     * @return an int representing the number of blocks in
     * the file.
     */
    public int getBlockNumber()
    {return blockNumber;}

    /**
     * Accessor that retrieves the total number of blocks
     * in the file.
     * @return a int representing the total number of 
     * blocks in the file.
     */
    public int getBlocksInGroup()
    { return blocksInGroup;}

    /**
     * Accessor that retrieves the total number of blocks
     * in the file.
     * @return a int representing the total number of 
     * blocks in the file.
     */
    public int getGroupNumber()
    { return groupNumber;}

    /**
     * Accessor that retrieves the number of inodes
     * in a single group.
     * @return an int representing the inodes in a
     * group.
     */
    public int getiNodesInGroup()
    { return iNodesInGroup;}

    /**
     * Accessor that retrieves the size of inodes
     * in a file .
     * @return an int representing the size of all
     * inodes in a filesystem.
     */
    public int getiNodeSize()
    { return iNodeSize;}
 }