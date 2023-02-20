package de.zent.restaurantworker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication implements CommandLineRunner {

	private static Logger logger = LoggerFactory.getLogger(WorkerApplication.class);

	private WorkerReviewsQueue reviewsQueue;
	private ReviewHandler reviewHandler;
	private ExecutorService executor;

	public WorkerApplication(WorkerReviewsQueue queue, ReviewHandler reviewHandler) {
		this.reviewsQueue = queue;
		this.reviewHandler = reviewHandler;
		this.executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Start receiving from reviewsQueue");
		reviewsQueue.startReceiving(executor, reviewHandler);
	}

	public static void main(String[] args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

}
