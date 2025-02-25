package org.client;

import org.data.Vote;

public class ClientData {
    private String username;
    public boolean isVoting;
    public boolean isCreatingVote;
    public Integer creatingStage = 0;
    public Integer numAns = 0;
    public Vote newVote;
    public String topicName;
    public String voteName;
    public ClientData() {
        this.username = null;
        this.isCreatingVote = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
