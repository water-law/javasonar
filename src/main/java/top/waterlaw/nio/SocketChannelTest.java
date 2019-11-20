package top.waterlaw.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SocketChannelTest {


    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("waterlaw.top", 80));

        while (!socketChannel.finishConnect()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        StringBuilder builder = new StringBuilder();
        byte[] request = builder.append("GET https://www.waterlaw.top/ HTTP/1.1\r\n").append("Host: www.waterlaw.top\r\n")
                .append("Connection: keep-alive\r\n")
                .append("Cache-Control: no-cache\r\n").append("Upgrade-Insecure-Requests: 1\r\n")
                .append("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36\r\n")
                .append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\n")
                .append("Accept-Encoding: gzip, deflate, br\r\n").append("Accept-Language: zh-CN,zh;q=0.9\r\n")
                .append("\r\n").toString().getBytes();

        socketChannel.write(ByteBuffer.wrap(request));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int bytesRead = socketChannel.read(byteBuffer);
        boolean readed = false;
        while (bytesRead != -1) {
            if(bytesRead == 0 && readed) {
                break;
            }
            else if(bytesRead == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("CCCCCCCCCCCC");
                continue;
            }

            byteBuffer.flip();
            String receivedString= Charset.forName("UTF-8").newDecoder().decode(byteBuffer).toString();
            System.out.println(receivedString);
            while (byteBuffer.hasRemaining()) {
                System.out.println((char)byteBuffer.get());
            }
            byteBuffer.clear();
            readed = true;
            bytesRead = socketChannel.read(byteBuffer);
        }
        socketChannel.close();
    }
}
