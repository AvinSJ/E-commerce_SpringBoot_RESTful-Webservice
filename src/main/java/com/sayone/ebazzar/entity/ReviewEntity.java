package com.sayone.ebazzar.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
public class ReviewEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @UpdateTimestamp
    private LocalDateTime updatedTime;

    @CreationTimestamp
    private LocalDateTime createTime;

    private Integer rating;
    private String description;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "productId")
    private ProductEntity productEntity;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity userEntity;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "review_id")
    private List<VoteEntity> vote=new ArrayList<>();

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public ReviewEntity() {
    }

//    public ReviewEntity(ProductEntity productEntity, UserEntity userEntity, Integer rating, String description) {
//        this.productEntity = productEntity;
//        this.userEntity = userEntity;
//        this.rating = rating;
//        this.description = description;
//    }

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public void setProductEntity(ProductEntity productEntity) {
        this.productEntity = productEntity;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public List<VoteEntity> getVote() {
        return vote;
    }

    public void setVote(List<VoteEntity> vote) {
        this.vote = vote;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }
}
