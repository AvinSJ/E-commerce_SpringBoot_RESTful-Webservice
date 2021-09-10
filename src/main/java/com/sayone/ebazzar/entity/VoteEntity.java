package com.sayone.ebazzar.entity;
import javax.persistence.*;

@Entity
@Table(name="vote")
public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String vote;

    public ReviewEntity getReview_id() {
        return review_id;
    }


    public void setReview_id(ReviewEntity review_id) {
        this.review_id = review_id;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "review_id")
    private ReviewEntity review_id;

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity userEntity;

    public String getVote() {
        return vote;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }


}
