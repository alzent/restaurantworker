package de.zent.restaurantworker;

import java.io.IOException;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class EmailAlerter {

	
	public EmailAlerter(Environment env) {
		
		
	}
	
	
	public void alert(Review review, double sentimentScore) throws IOException {
		
		
		
	}
}
