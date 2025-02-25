package org.server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Scanner;

public class Server {
    private final int port;
    public final String pathToMainFolder = System.getProperty("user.dir")+"\\data\\";
    public Server(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            File d = new File(pathToMainFolder+"topics\\");
            if(!d.exists()) {
                if(!d.mkdirs()){
                    System.out.println("Не удалось создать главную директорию для данных!");
                }
            }
            console();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private void console() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String[] line = scanner.nextLine().toLowerCase().trim().split(" ");
                switch (line[0].trim()) {
                    case "load"-> {
                        if (line.length == 2)
                            loadTopicsAndVotes(line[1]);
                        else
                            System.out.println("Введите только 1 аргумент - путь до данных для загрузки");
                    }
                    case "save"-> {
                        if(line.length == 2)
                            saveTopicsAndVotes(line[1]);
                        else
                            System.out.println("Введите только 1 аргумент - путь до места для выгрузки данных");
                    }
                    case "exit"->{
                        System.out.println("Закрытие сервера...");
                        System.exit(0);
                    }
                    default -> System.out.println("Неизвестная команда");
                }
            }
        }).start();
    }

    private void saveTopicsAndVotes(String dest) {
        File folder = new File(pathToMainFolder+"\\topics\\");
        File destDir = new File(dest+"\\topics\\");
        if(destDir.mkdir() || destDir.exists()) {
            if (folder.exists()) {
                File[] listOfTopics = folder.listFiles();
                if (listOfTopics != null) {
                    for (File topic : listOfTopics) {
                        if(topic.isDirectory()){
                            try {
                                Files.copy(topic.toPath(), Paths.get(dest+"\\topics\\"+topic.getName()+"\\"), StandardCopyOption.REPLACE_EXISTING);
                                for (File vote: Objects.requireNonNull(topic.listFiles())
                                     ) {
                                    try {
                                        Files.copy(vote.toPath(), Paths.get(dest + "\\topics\\" + topic.getName() + "\\" + vote.getName()), StandardCopyOption.REPLACE_EXISTING);
                                    }
                                    catch (Exception e){
                                        System.out.println("Не удалось выгрузить " +vote.getPath());
                                    }
                                }

                            } catch (Exception e) {
                                System.out.println("Не удалось выгрузить " +topic.getPath());
                            }
                        }
                    }
                }
                else {
                    System.out.println("В файле нет разделов!");
                }
            }
            else{
                System.out.println("Файла по этому пути не существует: "+folder.getPath());
            }
        }
        else{
            System.out.println("Не удалось создать директорию "+dest+"\\topics\\");
        }
    }

    private void loadTopicsAndVotes(String depart) {
        File folder = new File(depart+"\\topics\\");
        File destDir = new File(pathToMainFolder+"\\topics\\");
        if(destDir.mkdir() || destDir.exists()) {
            if (folder.exists()) {
                File[] listOfTopics = folder.listFiles();
                if (listOfTopics != null) {
                    for (File topic : listOfTopics) {
                        if(topic.isDirectory()){
                            try {
                                Files.copy(topic.toPath(),Paths.get(pathToMainFolder+"\\topics\\"+topic.getName()+"\\"), StandardCopyOption.REPLACE_EXISTING);
                                for (File vote: Objects.requireNonNull(topic.listFiles())
                                ) {
                                    try {
                                        Files.copy(vote.toPath(),Paths.get(pathToMainFolder + "\\topics\\" + topic.getName() + "\\" + vote.getName()), StandardCopyOption.REPLACE_EXISTING);
                                    }
                                    catch (Exception e){
                                        System.out.println("Не удалось выгрузить " +vote.getPath());
                                    }
                                }

                            } catch (Exception e) {
                                System.out.println("Не удалось выгрузить " +topic.getPath());
                            }
                        }

                    }
                }
                else {
                    System.out.println("База данных пуста!");
                }
            }
            else{
                System.out.println("База данных пуста!");
            }
        }
        else{
            System.out.println("Не удалось создать директорию "+depart+"\\topics\\");
        }
    }


    public static void main(String[] args) throws Exception {
        new Server(8080).run();
    }

}