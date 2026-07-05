package org.Amin.Contact;

import io.netty.handler.codec.http.HttpObjectAggregator;
import org.Amin.Contact.Handlers.JsonDecoder;
import org.Amin.Contact.Handlers.MainHandler;
import org.Amin.Contact.Util.Netty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    private final int port;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public final AtomicBoolean shuttingDown = new AtomicBoolean(false);


    public Server(int port, int bossThreads, int workerThreads) {
        bossGroup = Netty.newMultiThreadingEventLoopGroup(bossThreads);
        workerGroup = Netty.newMultiThreadingEventLoopGroup(workerThreads);
        this.port = port;
    }

    public void start() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(Netty.getServerSocketClass())
                    // Some Normal Optimizations + Prevent some DDOS Attacks
                    .option(ChannelOption.SO_BACKLOG,1<<13)             // 8K Max Req
                    .option(ChannelOption.TCP_FASTOPEN, 1<<10)          // 1k Max
                    .childOption(ChannelOption.TCP_NODELAY, true)       // Nagle Skip
                    .childOption(ChannelOption.SO_KEEPALIVE, true)      // Kill Dead Guys

                    // SO_RCVBUF or SO_SNDBUF for later

                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipe = ch.pipeline();
                            pipe.addLast(new HttpServerCodec());
                            pipe.addLast(new HttpObjectAggregator(1 << 10)); // 1KB
                            pipe.addLast(new JsonDecoder());
                            pipe.addLast(new MainHandler(Server.this));
                        }
                    })
            ;
            if (Epoll.isAvailable()) // Linux Optimizations
                bootstrap
                        .option(EpollChannelOption.SO_REUSEPORT, true)      // Port Share Between Threads
                        .childOption(EpollChannelOption.TCP_QUICKACK,true)  // Delayed ACK Skip
                    //TODO add more options later, for best latency,throughput
                ;


            ChannelFuture c = bootstrap.bind(port).sync();
            System.out.println("Server Initialized :) on port "+port+";");
            if (Epoll.isAvailable())
                System.out.println("The Server is currently have maximum optimizations");
            else
                System.out.println("The Server is not on maximum optimizations, use container or run The Server in Linux");

            c.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println("Take a look:"+e);
            Thread.currentThread().interrupt(); // Dont Break Loop Chain
        }
    }

    public void shutdown() {
        System.out.println("System is shutting down; GoodBye :)");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        new Server(10203,4,16).start();
    }

}
