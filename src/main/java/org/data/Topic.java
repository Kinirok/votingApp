package org.data;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


public class Topic {//Создал, а в итоге его использование оказалось не очень эффективным
    public final String name;
    public ArrayList<Vote> voteList;

    public Topic(String path, String name){
        File dirTopics = new File(path+name);
        this.name = name;
        this.voteList = new ArrayList<>();

        if(dirTopics.list() != null) {
            for(String vote: Objects.requireNonNull(dirTopics.list())){
                voteList.add(Vote.deserialize(path+name+"/"+vote));
            }
        }
    }
}
