import java.nio.*;

/**
 * Class to describe the group descriptor for a certain block.
 * @author Vlad Diaconu
 */
public class GroupDescriptor
{
    private ByteBuffer buffer;
    private int[] pointers;

    /**
     * Method that allows the construction of a group descriptor.
     * @param content is the data within the group descriptor.
     * @param group is the block containing the number of inode pointers within the
     * group descriptor.
     */
    public GroupDescriptor(byte[] content, int group)
    {
        buffer = ByteBuffer.wrap(content);

        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        pointers = new int[group];

        for(int i = 0; i < group ; ++i)
        {
            pointers[i] = buffer.getInt((Ext2RoutineHandler.groupDescriptorSize * i) + Ext2RoutineHandler.iNodeTableOffset);
             System.out.println(pointers[i]);
        }
        
    }
    
    /**
     * @return the inode table pointers.
     */
    public int[] getPointers()
    {
        return pointers;
    }

}
