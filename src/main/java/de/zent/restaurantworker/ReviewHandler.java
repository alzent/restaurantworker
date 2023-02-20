package de.zent.restaurantworker;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReviewHandler {

	private static Logger logger = LoggerFactory.getLogger(ReviewHandler.class);
	
	private final EmailAlerter alerter;
	
	public ReviewHandler(EmailAlerter alerter) {
        this.alerter = alerter;
    }
	
	public void handle(Review review) throws IOException {
		
		logger.info("ReviewHandler.handle(review) called");
		
	}
}
