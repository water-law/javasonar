package top.waterlaw.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelTest {

    public static void main(String[] args) throws IOException {
        RandomAccessFile aFile = new RandomAccessFile("nio-data.txt", "rw");
        FileChannel fileChannel = aFile.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(4);
        int bytesRead = fileChannel.read(buffer);

        while (bytesRead != -1) {
            System.out.println("read "+bytesRead);
            buffer.flip();
            while (buffer.hasRemaining()) {
                System.out.println((char)buffer.get());
                break;
            }
            buffer.clear();
//            buffer.compact();
            bytesRead = fileChannel.read(buffer);
        }
        aFile.close();
        fileChannel.close();
    }
}
