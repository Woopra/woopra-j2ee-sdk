/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.woopra.sss.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author dev
 */
public class AsyncClient implements Runnable {

    public static void main(String[] args) throws Exception {
        AsyncClient c = new AsyncClient();

        for (int i = 0; i < 100; i++) {

            c.send(new URL("http://www.woopra.com/track/ce/?ra=abc&alias=jadyounan.com&cookie=abcd&ce_name=x"));
            System.out.println("#" + i);
            Thread.sleep(1000);
        }
    }
    public synchronized static AsyncClient getInstance() throws IOException {
    	if (instance == null) {
    		instance = new AsyncClient();
    	}
    	return instance;
    }
    
    private static AsyncClient instance = null;
    private String ip = "www.woopra.com";
    private int port = 80;
    private Selector selector;
    @SuppressWarnings("rawtypes")
	private final List changeList = new LinkedList();

    private AsyncClient() throws IOException {
        this.selector = SelectorProvider.provider().openSelector();

        new Thread(this).start();
    }

    public void send(URL url, String userAgent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(("GET " + url.getFile() + " HTTP/1.1").getBytes());
        baos.write("\r\n".getBytes());
        baos.write("Host: woopra.com".getBytes());
        baos.write("\r\n".getBytes());
        if (userAgent != null) {
        	baos.write(("User-Agent: ".concat(userAgent)).getBytes());
        	baos.write("\r\n".getBytes());
        }
        baos.write("\r\n".getBytes());

        baos.flush();
        byte bytes[] = baos.toByteArray();
        baos.close();

        send(bytes);

    }
    
    public void send(URL url) throws IOException {
    	this.send(url, null);
    }

    @SuppressWarnings("unchecked")
	void send(byte[] data) throws IOException {


        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        socketChannel.connect(new InetSocketAddress(this.ip, this.port));

        synchronized(this.changeList) {
            this.changeList.add(new Request(socketChannel, SelectionKey.OP_CONNECT, data));
        }

        this.selector.wakeup();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void run() {
        while (true) {
            try {
                synchronized(this.changeList) {
                    Iterator changes = this.changeList.iterator();
                    while (changes.hasNext()) {
                        Request change = (Request) changes.next();

                        switch (change.type) {
                            case SelectionKey.OP_CONNECT:
                                change.channel.register(this.selector, change.type);
                                change.channel.keyFor(selector).attach(change.attachment);
                                break;
                        }
                    }
                    this.changeList.clear();
                }

                this.selector.select();

                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {

                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        try {
                            socketChannel.finishConnect();
                        } catch (IOException e) {
                            key.cancel();
                            return;
                        }

                        key.interestOps(SelectionKey.OP_WRITE);

                    } else if (key.isReadable()) {

                        key.channel().close();

                        key.cancel();

                    } else if (key.isWritable()) {

                        ByteBuffer buf = (ByteBuffer) key.attachment();

                        ((SocketChannel) key.channel()).write(buf);

                        if (buf.remaining() > 0) {
                            break;
                        }

                        key.interestOps(SelectionKey.OP_READ);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class Request {

    public SocketChannel channel;
    public int type;
    public ByteBuffer attachment;

    public Request(SocketChannel channel, int type, byte[] attachmentBytes) {
        this.channel = channel;
        this.type = type;
        this.attachment = ByteBuffer.wrap(attachmentBytes);
    }
}