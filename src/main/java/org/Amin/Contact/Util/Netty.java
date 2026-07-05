package org.Amin.Contact.Util;

import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Netty {
    public static EventLoopGroup newMultiThreadingEventLoopGroup(int threads) {
        return new MultiThreadIoEventLoopGroup(
                threads,
                Epoll.isAvailable() ?
                        EpollIoHandler.newFactory(1<<10, DefaultSelectStrategyFactory.INSTANCE) :
                        KQueue.isAvailable() ?
                                KQueueIoHandler.newFactory() :
                                NioIoHandler.newFactory()
        );
    }

    public static Class<? extends ServerChannel> getServerSocketClass() {
        return Epoll.isAvailable() ?
                EpollServerSocketChannel.class :
                KQueue.isAvailable() ?
                        KQueueServerSocketChannel.class :
                        NioServerSocketChannel.class;
    }
}
