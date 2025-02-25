package org.data;

import java.io.*;
import java.util.ArrayList;

public class Vote implements Serializable {

    public String owner;
    public String name;
    public String desc;
    public ArrayList<String> answers;
    public ArrayList<ArrayList<String>> voteCount;//списки голосовавших за каждый вариант
    public final static String extension = ".vote";


    public Vote(String owner){
        this.owner = owner;
        answers = new ArrayList<>();
        voteCount = new ArrayList<>();
    }
    public Vote(String owner, String name,String desc,ArrayList<String> answers,ArrayList<ArrayList<String>> voteCount){
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.answers = answers;
        this.voteCount = voteCount;
    }
    public void serializeAndWrite(String path){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path+name+ extension)))
        {
            oos.writeObject(this);
        }
        catch(Exception ex){
            System.out.println("Не удалось записать объект "+name);
        }
    }
    public static Vote deserialize(String path){
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path)))
        {
            return (Vote)ois.readObject();
        }
        catch(Exception ex){
            System.out.println("Не удалось прочитать "+path);
            return null;
        }

    }
    public String toString(){
        StringBuilder out = new StringBuilder("Создатель голосования: " + owner);
        out.append("\nНазвание голосования: ").append(name);
        out.append("\nОписание: ").append(desc);
        for(int i = 0; i < answers.size(); ++i){
            out.append("\n").append(String.valueOf(i+1)).append(": ").append(answers.get(i)).append("(").append(voteCount.get(i).size()).append(")");
        }
        return out.toString();
    }

}
