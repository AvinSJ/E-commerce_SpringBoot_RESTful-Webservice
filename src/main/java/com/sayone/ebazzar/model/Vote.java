package com.sayone.ebazzar.model;

public enum Vote {

    Upvote("upvote"),
    Downvote("downvote");

    Vote(String vote) {
        this.vote=vote;
    }

    public String getVote() {
        return vote;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }

    String vote;
}
