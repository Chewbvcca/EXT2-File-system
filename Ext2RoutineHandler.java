import java.nio.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ext2RoutineHandler
{
    public static void runRoutine()
    {
        String file = Ext2Reader.getFileSystem();
        Volume v = new Volume(file);
        Ext2Reader ext2File = new Ext2Reader(v);

        /* Extract Superblock and the file-image details */
        SuperBlock superblock = new SuperBlock(ext2File.read(superblockOffset, blockSize));
        superblock.extractDetails();
        /* Extract and read the group descriptor */
        GroupDescriptor gDescriptor = new GroupDescriptor(ext2File.read(groupDescriptorOffset,blockSize),superblock.getGroupNumber());

        commandLineService(gDescriptor,ext2File,superblock);

    }  

    /**
     * Method to print the contents of the iNode tables.
     * @param g is the group descriptor for the specified
     * iNode table.
     * @param ext2File is the file from which the program reads
     * content.
     * @param sb is the superblock containing deatils about the superblock.
     */
    public static void commandLineService(GroupDescriptor g,Ext2Reader ext2File,SuperBlock sb)
    {
        boolean running = true;
        int[] pointers = g.getPointers();
        System.out.println("~~~~~~~~~~~~~~ROOT INODE~~~~~~~~~~~~~~~~~");

            INode iNode = new INode(ext2File.read(blockOffset * pointers[0] + sb.getiNodeSize(),blockSize));
           
            iNode.extractDetails();

            System.out.println(iNode.getPermissions()+" "+ iNode.getHardLinks() +" "+ iNode.getUserID()+" "+ iNode.getGroupID()
            +" "+ iNode.getSize() +" "+ iNode.getDate());

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println();
        System.out.println();
        FileInfo f = new FileInfo();
        f.divideBlocks(iNode,iNode.isFile(),ext2File,sb,g);        
        LinkedList<Directory> dir = f.getDirectories();
        
        Stack<String> path = new Stack<String>();

        while(running)
        {   
            System.out.print("\nuser:$ ");
            System.out.print(path.toString()
                            .replaceAll("\\[", "")
                            .replaceAll("]", "")
                            .replaceAll(",","")
                            .replaceAll(" ","")
                            );
            
            Scanner s = new Scanner(System.in);
            String command = s.nextLine();

            if(command.equals("cat"))
            {
                Scanner s2 = new Scanner(System.in);
                String file = s2.nextLine();
                for( Directory i : dir)
                {
                    String name = i.getName();
                    if(name.equals(file))
                    {
                        if( i.getINode().isFile() == true)
                        {
                            f.readFileContent(i.getINode(),ext2File,sb,g);
                            f.CAT();
                        }
                        else
                        {
                            System.out.println(name + " is not a file.");
                        }
                    }
                    
                }  
            }
            if(command.equals("exit"))
            {
                System.out.println("Program terminated by user.");
                running = false;
            }
            if(command.equals("ls"))
            {
                for(Directory i : dir)
                {
                    System.out.print (i.getDetails());
                }
            }
            if(command.equals("dump"))
            {
                System.out.println("Type starting byte:");
                Scanner s3 = new Scanner(System.in);
                long start = 
                Long.parseLong(s3.nextLine());

                System.out.println("Type length of sequence:");
                Scanner s4 = new Scanner(System.in);
                long length = Long.parseLong(s4.nextLine());

                try
                {
                    dumpHexBytes(ext2File.read(start,length));
                }
                catch(Exception e)
                {
                    e .printStackTrace();
                }
            }
            if(command.equals("read"))
            {
                System.out.println("Type the name of the file:");
                Scanner s3 = new Scanner(System.in);
                String fileName = s3.nextLine();

                for(Directory i : dir)
                {
                    if(fileName.equals(i.getName()))
                    {
                        System.out.println("You are currently referencing this file: " + i.getName());
                         System.out.println(i.getDetails());
                    }
                }
                System.out.println("Type the starting byte of sequence:");
                Scanner s4 = new Scanner(System.in);
                long start = Long.parseLong(s3.nextLine());

                System.out.println("Type the end byte of sequence:");
                Scanner s5 = new Scanner(System.in);
                long end = Long.parseLong(s5.nextLine());

                for(Directory i : dir)
                {
                    if(fileName.equals(i.getName()))
                    {
                        byte[] conversion = ext2File.readFromFile(i,start,end);
                        dumpFileINFO(conversion, i.getINode(),ext2File,(int)start,(int)end);
                        break;
                    }
                }
            }
            if(command.equals("cd"))
            {
                Scanner s5 = new Scanner(System.in);
                String directory = s5.nextLine();

                for(Directory i : dir)
                {
                    if(directory.equals(".."))
                    {
                        if(path.peek() != null)
                        {
                            path.pop();

                            for(Directory j : dir)
                                if("..".equals(j.getName()))
                                {
                                    f.divideBlocks(j.getINode(),j.getINode().isFile(),ext2File,sb,g);
                                    break;
                                }   
                            
                            break; 
                        }
                    }
                    else if(directory.equals(i.getName()))
                    {
                        if(i.getINode().isFile())
                        {
                            System.out.println(directory + " is not a directory.");
                            break;
                        }
                        else
                        {
                            path.push(directory+ "/");
                            f.divideBlocks(i.getINode(),i.getINode().isFile(),ext2File,sb,g);
                            break;
                        }              
                    }
                }
            }
        }
    }

    /**
     * Method that is used for debugging hex code.
     * The method is converting bytes to hex and then to stirng.
     * @param bytes is an array of bytes that the algorithm uses 
     * in order to perfom the conversions.
     */
    public static void dumpHexBytes(byte[] bytes) throws IOException
    {
        System.out.println("~~~~~~~~~~~~~~~DUMPING HEX~~~~~~~~~~~~~~~~");
        int alignerHEX = 16;
        int reminderHEX = 0;

        StringBuilder stringBuilderHEX = new StringBuilder();

        for(int i = 0; i < bytes.length; ++i) 
        {
            alignerHEX -= 1;
            
            if(alignerHEX != 0)
            {
                stringBuilderHEX.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                stringBuilderHEX.append(" ");
            }
            else
            {
                stringBuilderHEX.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                stringBuilderHEX.append("\n");
                alignerHEX = 16;
            }
        }
        if(bytes.length % 16 != 0)
        {
            do 
            {
                ++reminderHEX;
            }while((bytes.length + reminderHEX) %16 != 0);
           
            while(reminderHEX -- != 0)
            {
              stringBuilderHEX.append("XX ");
            }
        }
        System.out.println(stringBuilderHEX.toString());

        System.out.println("~~~~~~~~~~~END OF DUMPED HEX~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~DUMPING HEX AS ASCII~~~~~~~~~~~");

        int alignerASCII = 16;

        StringBuilder stringBuilderASCII = new StringBuilder();

        for(byte i : bytes)
        {
            alignerASCII -= 1;
            if(alignerASCII != 0)
            {
                int convertor = i;
                if(convertor != 0)
                {
                    stringBuilderASCII.append((char)convertor);
                    stringBuilderASCII.append(" ");
                }
                else
                {
                    stringBuilderASCII.append(". ");
                }
            }
            else
            {
                int convertor = i;
                if(convertor != 0)
                {
                    stringBuilderASCII.append((char)convertor);
                    stringBuilderASCII.append("\n");
                }
                else
                {
                    stringBuilderASCII.append(".\n");
                }
                alignerASCII = 16;
            }
        }
        System.out.println(stringBuilderASCII.toString());

        System.out.println("~~~~~~~END OF DUMPED HEX AS ASCII~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    }   

    public static void dumpFileINFO(byte[] data,INode node,Ext2Reader ext2,int start,int end)
    {
        StringBuilder contentFile = new StringBuilder();
        int[] copy = node.getPointers();
                    
        for(int m = 0; m < copy.length; ++m)
        {
                        
            if(copy[m] != 0)
            {
                ext2.seek(copy[m] * Ext2RoutineHandler.blockSize + start);
                byte[] c = ext2.readFile(end-start);
                
                for(int n = 0; n < c.length; ++n)
                    contentFile.append((char)c[n]);
            }
        }
        System.out.println(contentFile.toString().trim());
    }


        /* VALUES FOR INODE FILE MODES */
     static final int IFSCK = 0xC000;      // Socket
     static final int IFLNK = 0xA000;      // Symbolic Link
     static final int IFREG = 0x8000;      // Regular File
     static final int IFBLK = 0x6000;      // Block Device
     static final int IFDIR = 0x4000;      // Directory
     static final int IFCHR = 0x2000;      // Character Device
     static final int IFIFO = 0x1000;      // FIFO

     static final int ISUID = 0x0800;      // Set process User ID
     static final int ISGID = 0x0400;      // Set process Group ID
     static final int ISVTX = 0x0200;      // Sticky bit


     static final int IRUSR = 0x0100;      // User read
     static final int IWUSR = 0x0080;      // User write
     static final int IXUSR = 0x0040;      // User execute

     static final int IRGRP = 0x0020;      // Group read
     static final int IWGRP = 0x0010;      // Group write
     static final int IXGRP = 0x0008;      // Group execute

     static final int IROTH = 0x0004;      // Others read
     static final int IWOTH = 0x0002;      // Others wite
     static final int IXOTH = 0x0001;      // Others execute

    /*  SUPERBLOCK OFFSETS AND VALUES */
     static final short magicNumber =(short) 0xef53; //magic number in any ext2 file system
     static final int blockSize = 1024;    // every block's capacity within the volume
     static final int blockOffset = 1024;  //the superblock starts at byte 1024
     static final int superblockOffset = 1024; 
     static final int groupDescriptor = 2048;
     static final int magicNumberOffset = 56;
     static final int iNodeCounter = 0;
     static final int groupsNumber = 3; 
     static final int blockCounter = 4;
     static final int iNodePointers = 15;
     static final int fileSystemNameSize = 16;
     static final int fileSystemBlockSizeOffset = 24;
     static final int blocksInGroup = 32;
     static final int iNodesInGroup = 40;
     static final int iNodeSize = 88;
     static final int fileSystemOffset = 120;
     
     /* GROUP DESCRIPTOR OFFSETS AND VALUES  */
     static final int groupDescriptorOffset = 2048;
     static final int groupDescriptorSize = 32;
     static final int iNodeTableOffset = 8;

         /* INODE OFFSETS AND VALUES  */
      static final int iNodePointerSize = 15;
        
      static final int iNode_typeOffset = 0;
      static final int iNode_userIDOffset = 2;
      static final int iNode_lastAccessOffset = 8;
      static final int iNode_creationTime = 12;
      static final int iNode_lastModificationOffset = 16;
      static final int iNode_groupIDOffset = 24;
      static final int iNode_hardLinksOffset = 26;
      static final int iNode_blockPointerOffset = 40;
      static final int iNode_upperBitsOffset = 108;
      static final int iNode_lowerBitsOffset = 4;
      static final int iNode_rootOffset = 2;
          
            /*GENERIC VALUES */
      static final int byte_Value = 4;
      static final int directoryNameOffset = 6;
      
}