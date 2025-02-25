package org.server;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.client.ClientData;
import org.client.ClientList;
import org.command.CmdClientParser;
import org.data.Vote;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOG = Logger.getLogger(ServerHandler.class.getName());
    public final String path = System.getProperty("user.dir")+"\\data\\topics\\";//предполагается, что юзер-это сервер и им открыта папка проекта
    private final ClientList clientList = new ClientList();

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientList.addClient(ctx.channel());//присваиваем каждому подключению свой блок данных
        LOG.info("Установлено соединение с клиентом "+ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientList.removeClient(ctx.channel());
        LOG.info("Разорвано соединение с клиентом "+ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        msg = msg.toLowerCase();
        String[] line = msg.split(" ");
        ClientData cData = clientList.getClientData(ctx.channel());

        if(cData.isCreatingVote) {
            creatingVote(msg,ctx);

        }
        else if(cData.isVoting){
            voting(msg,ctx);
        }
        else {
            cmdExecute(line, ctx);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.info("Клиент "+ctx.channel().remoteAddress()+" отключился от сервера");
        ctx.close();
    }
    public void creatingVote(String msg, ChannelHandlerContext ctx){
        ClientData cData = clientList.getClientData(ctx.channel());
        if(cData.creatingStage == 0){
            if(!msg.trim().equals("")){
                String[] topicList = (new File(path+cData.topicName)).list();
                assert topicList != null;
                if(!Arrays.stream(topicList).toList().contains(msg.trim()+".vote")) {
                    cData.newVote.name = msg.trim();
                    cData.creatingStage++;
                    ctx.writeAndFlush("Введите описание голосования:");
                }
                else {
                    ctx.writeAndFlush("Голосование с таким именем в разделе "+cData.topicName+" уже существует!");
                }
            }
            else {
                ctx.writeAndFlush("Введите название голосования!");
            }
        }
        else if(cData.creatingStage == 1){
            if(!msg.trim().equals("")){
                cData.newVote.desc=msg;
                cData.creatingStage++;
                ctx.writeAndFlush("Сколько должно быть вариантов голосования?");
            }
            else {
                ctx.writeAndFlush("Введите описание голосования!");
            }
        }
        else if(cData.creatingStage == 2){
            if(!msg.trim().equals("")){
                try{
                    cData.numAns=Integer.valueOf(msg.trim());
                    cData.creatingStage++;
                    ctx.writeAndFlush("Построчно введите все варианты ответа:");
                }
                catch(NumberFormatException e) {
                    ctx.writeAndFlush("Введенное значение должно быть целым числом!");
                }

            }
            else {
                ctx.writeAndFlush("Введенное значение должно быть целым числом!");
            }
        }
        else if(cData.creatingStage <= 3+cData.numAns){
            System.out.println(cData.creatingStage);
            if(!msg.trim().equals("")) {
                cData.newVote.answers.add(msg);
                cData.newVote.voteCount.add(new ArrayList<>());
                cData.creatingStage++;
                if(cData.creatingStage == 3+cData.numAns){
                    cData.isCreatingVote = false;
                    cData.creatingStage = 0;
                    cData.numAns = 0;
                    if(!(new File(path+cData.topicName+"\\"+cData.newVote.name+".vote")).exists()) {
                        cData.newVote.serializeAndWrite(path + cData.topicName + "\\");
                        ctx.writeAndFlush("Создание голосования завершено!");
                        LOG.info("Клиент "+ctx.channel().remoteAddress()+ " создал голосование "+cData.topicName+"\\"+cData.newVote.name+".vote");
                    }
                    else{
                        ctx.writeAndFlush("Голосование с таким названием в этом разделе уже было создано!");//если создадут два одинаковых голосования, перезаписи не будет
                    }
                }
            }
            else{
                ctx.writeAndFlush("Вам осталось ввести "+String.valueOf(cData.creatingStage -1+cData.numAns)+" вариантов ответа:");
            }
        }
    }
    public void authorize(String[] line, ChannelHandlerContext ctx){
        if(clientList.getClientData(ctx.channel()).getUsername() == null){
            String name = CmdClientParser.login(line);
            if(name != null && !name.equals("")){
                clientList.getClientData(ctx.channel()).setUsername(name);
                ctx.writeAndFlush("Вы успешно авторизовались!");
                LOG.info("Клиент "+ctx.channel().remoteAddress()+" авторизовался под именем "+name);
            }
            else{
                ctx.writeAndFlush("Укажите имя пользователя через тег -u или -username.");
            }
        }
        else{
            ctx.writeAndFlush("Вы уже авторизованы!");
        }
    }
    public synchronized void voting(String msg, ChannelHandlerContext ctx){ //второе место, где изменяются данные датасета, все голоса будут засчитываться о очереди и перезаписи не будет
        ClientData cData = clientList.getClientData(ctx.channel());
        String pathToVote = path + cData.topicName+"\\"+cData.voteName+".vote";
        if(!msg.trim().equals("")){
            try{
                int var = Integer.parseInt(msg.trim());
                Vote vote = Vote.deserialize(pathToVote);
                assert vote!=null;
                if (var <=0 || var >vote.voteCount.size()){
                    ctx.writeAndFlush("Выберите вариант от 1 до "+vote.voteCount.size() + "!");
                }
                else {
                    vote.voteCount.get(var - 1).add(cData.getUsername());
                    cData.isVoting = false;
                    vote.serializeAndWrite(path + cData.topicName+"\\");
                    ctx.writeAndFlush("Ваш голос засчитан!");
                    LOG.info("Клиент "+ctx.channel().remoteAddress()+ " проголосовал в "+cData.topicName+"\\"+cData.voteName+".vote"+" за "+(var-1)+" вариант");
                }
            }
            catch(NumberFormatException e) {
                ctx.writeAndFlush("Введенное значение должно быть целым числом!");
            }

        }
        else {
            ctx.writeAndFlush("Введенное значение должно быть целым числом!");
        }
    }
    public void cmdExecute(String[] line,ChannelHandlerContext ctx){
        ClientData cData = clientList.getClientData(ctx.channel());
        if(cData.getUsername() == null && !line[0].trim().equals("login")) {
            if(!line[0].trim().equals("exit")){
                ctx.writeAndFlush("Соединение разорвано.").addListener(ChannelFutureListener.CLOSE);
            }
            else{
                ctx.writeAndFlush("Для начала авторизуйтесь!");
            }
        }
        else {
            switch (line[0].trim()) {
                case "login" -> authorize(line, ctx);
                case "create" -> {
                    if (line.length > 1 && (line[1].trim().equals("topic"))) {
                        String name = CmdClientParser.createTopic(line);
                        if (name != null) {
                            if (createTopic(name)) {
                                ctx.writeAndFlush("Раздел создан");
                            } else {
                                ctx.writeAndFlush("Раздел уже существует");
                            }
                        }
                        else{
                            ctx.writeAndFlush("Укажите название раздела через тег -n или -name.");
                        }
                    } else if (line.length > 1 && line[1].trim().equals("vote")) {
                        String name = CmdClientParser.createVote(line);
                        if (name != null) {
                            if ((new File(path + name)).exists()) {
                                cData.isCreatingVote = true;
                                cData.newVote = new Vote(cData.getUsername());
                                cData.topicName = name;
                                ctx.writeAndFlush("Введите название для голосования:");
                            }
                            else {
                                ctx.writeAndFlush("Такого раздела не существует");
                            }
                        }
                        else{
                            ctx.writeAndFlush("Укажите название раздела, в котором хотите создать голосование, через тег -t или -topic.");
                        }

                    }
                    else{
                        ctx.writeAndFlush("Укажите, что вы хотите создать: раздел(topic) или голосование(vote)");
                    }
                }
                case "view" -> {
                    String[] args = CmdClientParser.view(line);
                    if(args!=null){

                        File topDir = new File(path + args[0]+"\\");
                        if (topDir.exists()) {
                            if(args.length ==1){
                                String[] voteList = topDir.list();
                                assert voteList!=null;
                                ctx.writeAndFlush(Arrays.stream(voteList).toList().toString().replace("[","").replace("]","").replace(".vote",""));
                                LOG.info("Клиент "+ctx.channel().remoteAddress()+" запросил список голосований в разделе "+args[0]);
                            }
                            else if((new File(path + args[0]+"\\"+args[1]+".vote")).exists()){
                                Vote toShow = Vote.deserialize(path + args[0]+"\\"+args[1]+".vote");
                                assert toShow != null;
                                ctx.writeAndFlush(toShow.toString());
                                LOG.info("Клиент "+ctx.channel().remoteAddress()+" запросил информацию о голосовании "+args[0]+"\\"+args[1]);
                            }
                            else {
                                ctx.writeAndFlush("Такого голосования не существует!");
                            }

                        }
                        else {
                            ctx.writeAndFlush("Такого раздела не существует!");
                        }
                    }
                    else{
                        ctx.writeAndFlush("Укажите название раздела через тег -t или -topic, если хотите увидеть список голосований.\nДобавьте через тег -v или -vote название голосования, чтобы увидеть подробности по нему.");
                    }
                }
                case "vote" -> {
                    String[] args = CmdClientParser.vote(line);
                    if(args!=null){
                        if ((new File(path + args[0])).exists()) {
                            if((new File(path + args[0]+"\\"+args[1]+".vote")).exists()){
                                Vote vote = Vote.deserialize(path + args[0]+"\\"+args[1]+".vote");
                                assert vote != null;
                                boolean isVoted = false;
                                for (ArrayList<String> var: vote.voteCount
                                     ) {
                                    if(var.contains(cData.getUsername())){
                                        isVoted = true;
                                        break;
                                    }
                                }
                                if(!isVoted) {
                                    ctx.writeAndFlush(vote.toString());
                                    ctx.writeAndFlush("\nВыберите вариант ответа:");
                                    cData.topicName = args[0];
                                    cData.voteName = args[1];
                                    cData.isVoting = true;
                                }
                                else {
                                    ctx.writeAndFlush("Вы уже голосовали в этом разделе!");
                                }
                            }
                            else {
                                ctx.writeAndFlush("Такого голосования не существует!");
                            }

                        }
                        else {
                            ctx.writeAndFlush("Такого раздела не существует!");
                        }
                    }
                    else{
                        ctx.writeAndFlush("Укажите название раздела и голосования, за которое хотите проголосовать.\nДля раздела тег -t или -topic, для голосования -v или -vote");
                    }
                }
                case "exit" -> ctx.writeAndFlush("Соединение разорвано.").addListener(ChannelFutureListener.CLOSE);
                case "delete" -> {
                    String[] args = CmdClientParser.delete(line);
                    if(args!=null) {
                        String votePath = path + args[0] + "\\" + args[1] + ".vote";
                        File voteFile = new File(votePath);
                        if (voteFile.exists()) {
                            Vote vote = Vote.deserialize(votePath);
                            assert vote!=null;
                            if(vote.owner.equals(cData.getUsername())){
                                if (voteFile.delete()){
                                    ctx.writeAndFlush("Голосование удалено!");
                                    LOG.info("Клиент "+ctx.channel().remoteAddress()+ " удалил голосование "+args[0] + "\\" + args[1] + ".vote");
                                }
                                else{
                                    ctx.writeAndFlush("Не удалось удалить голосование!");
                                }
                            }
                            else{
                                ctx.writeAndFlush("Голосование может удалить только пользователь его создавший");
                            }
                        } else {
                            ctx.writeAndFlush("Такого голосования не существует!");
                        }
                    }
                    else {
                        ctx.writeAndFlush("Укажите название раздела и голосования, которое хотите удалить\nДля раздела тег -t или -topic, для голосования -v или -vote");
                    }
                }
                default -> ctx.writeAndFlush("Неизвестная команда");
            }
        }

    }
    public synchronized boolean createTopic(String name){
        File newDir = new File(path+name);
        return newDir.mkdir();
    }

}