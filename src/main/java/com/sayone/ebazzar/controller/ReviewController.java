package com.sayone.ebazzar.controller;

import com.sayone.ebazzar.Job.SendEmailJob;
import com.sayone.ebazzar.common.RestResources;
import com.sayone.ebazzar.dto.UserDto;
import com.sayone.ebazzar.entity.ReviewEntity;
import com.sayone.ebazzar.entity.UserEntity;
import com.sayone.ebazzar.entity.VoteEntity;
import com.sayone.ebazzar.exception.ErrorMessages;
import com.sayone.ebazzar.exception.RequestException;
import com.sayone.ebazzar.model.Vote;
import com.sayone.ebazzar.model.request.ReviewRequestModel;
import com.sayone.ebazzar.model.response.ReviewResponseModel;
import com.sayone.ebazzar.repository.ReviewRepository;
import com.sayone.ebazzar.repository.UserRepository;
import com.sayone.ebazzar.repository.VoteRepository;
import com.sayone.ebazzar.service.ReviewService;
import com.sayone.ebazzar.service.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(RestResources.REVIEW_ROOT)
public class ReviewController {

    @Autowired
    ReviewService reviewService;

    @Autowired
    UserService userService;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    Scheduler scheduler;




    // http://localhost:8080/reviews
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")})
    @PostMapping
    public ResponseEntity<ReviewResponseModel> createReview(@RequestBody ReviewRequestModel reviewRequestModel) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDto user = userService.getUser(auth.getName());

        if (reviewRequestModel.getProductId() == null)
            throw new RequestException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessages());

        return new ResponseEntity(reviewService.createReview(reviewRequestModel, user.getUserId(),user), HttpStatus.CREATED);
    }

    // http://localhost:8080/reviews/update
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")})
    @PutMapping(path = RestResources.UPDATE_RATING_BY_ID)
    public ResponseEntity<ReviewResponseModel> updateRating(@RequestBody ReviewRequestModel reviewRequestModel) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDto user = userService.getUser(auth.getName());

        return new ResponseEntity<>(reviewService.updateReview(reviewRequestModel, user.getUserId()), HttpStatus.OK);
    }

    // http://localhost:8080/reviews/all
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")})
    @GetMapping(path = RestResources.GET_ALL_REVIEWS)
    public ResponseEntity<List<ReviewResponseModel>> getAllReview() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDto user = userService.getUser(auth.getName());
        List<ReviewResponseModel> reviewResponseModels = reviewService.findReviewsByUser(user.getUserId());
        if (reviewResponseModels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(reviewResponseModels, HttpStatus.OK);
        }
    }

    // http://localhost:8080/reviews/all/1
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")})
    @GetMapping(path = RestResources.GET_RATING_FOR_PRODUCT)
    public ResponseEntity<ReviewResponseModel> getRatingUsingPid(@PathVariable Long pid) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDto user = userService.getUser(auth.getName());

        ReviewResponseModel reviewResponseModel = reviewService.findReviewForProduct(pid, user.getUserId());
        if (reviewResponseModel == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(reviewResponseModel, HttpStatus.OK);
        }
    }

    // http://localhost:8080/reviews/delete?pid=1
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")})
    @DeleteMapping(path = RestResources.DELETE_REVIEW)
    public ResponseEntity<?> deleteRating(@RequestParam(value = "pid") Long productId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDto user = userService.getUser(auth.getName());

        reviewService.deleteReview(productId, user.getUserId());
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping("/vote")
    @ApiImplicitParams({@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")})
    public String vote(@RequestParam Long review_id, @RequestParam Vote vote) throws SchedulerException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDto user = userService.getUser(auth.getName());
        UserEntity userValue = new UserEntity();
        BeanUtils.copyProperties(user, userValue);

        ReviewEntity reviewEntity = reviewRepository.findByReviewId(review_id);



        VoteEntity value = new VoteEntity();
        value.setVote(vote.getVote());
        value.setReview_id(reviewEntity);
        value.setUserEntity(userValue);


        List<VoteEntity> userIdExists = voteRepository.findByUserID(userValue.getUserId(), reviewEntity.getReviewId());
        if (userIdExists.isEmpty()) {
            value = voteRepository.save(value);
            reviewEntity.setVote(List.of(value));


            JobDetail jobDetail = buildJobDetail(user,value);
            Trigger trigger = buildtrigger(jobDetail);


            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
            return "Vote Successful";
        } else {
            return "User Already Voted";
        }


    }

    private JobDetail buildJobDetail(UserDto user, VoteEntity save){
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email",user.getEmail());
        jobDataMap.put("review_Id",save.getReview_id().getReviewId());
        jobDataMap.put("vote",save.getVote());


        return JobBuilder.newJob().ofType(SendEmailJob.class)
                .storeDurably()
                .withIdentity("Qrtz_Job_Detail")
                .withDescription("Send Email To The Voter")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    public Trigger buildtrigger(JobDetail job){
        return TriggerBuilder.newTrigger().forJob(job)
                .withIdentity("Qrtz_Trigger")
                .withDescription("Email-Trigger")
                .startAt(Date.from(Instant.now().plusSeconds(300)))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
