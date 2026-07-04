import Util.Netty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.uring.IoUringChannelOption;
import io.netty.handler.codec.http.HttpServerCodec;

public class Server {

    private final int port;

    private final EventLoopGroup bossGroup;
    public Server(int port, int threads) {
        bossGroup = Netty.newMultiThreadingEventLoopGroup(threads);
        this.port = port;
    }

    public void start() {
        EventLoopGroup workerGroup = Netty.newMultiThreadingEventLoopGroup(2);
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

                    .childHandler(new ChannelInitializer<ServerChannel>() {
                        @Override
                        protected void initChannel(ServerChannel ch) throws Exception {
                            ChannelPipeline pipe = ch.pipeline();
                            pipe.addLast(new HttpServerCodec());
                            pipe.addLast();
                        }
                    })
            ;
            if (Epoll.isAvailable()) // Linux Optimizations
                bootstrap
                        .option(EpollChannelOption.SO_REUSEPORT, true)      // Port Share Between Threads
                        .childOption(EpollChannelOption.TCP_QUICKACK,true)  // Delayed ACK Skip
                ;


            ChannelFuture c = bootstrap.bind(port).sync();
            System.out.println("Server Initialized :) on port "+port+";");
            if (Epoll.isAvailable())
                System.out.println("The Server is currently have maximum optimizations");
            else
                System.out.println("The Server is not on maximum optimizations, use container or run The Server in Linux");

            c.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new Server(10203,8).start();
    }

}
