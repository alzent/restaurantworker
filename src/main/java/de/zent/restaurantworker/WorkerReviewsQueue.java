package de.zent.restaurantworker;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

@Service
public class WorkerReviewsQueue {

	private static Logger logger = LoggerFactory.getLogger(WorkerReviewsQueue.class);
	
	private QueueClient receiveClient;
	private ObjectMapper mapper;
	private CountDownLatch latch;

	public WorkerReviewsQueue(Environment env) throws ServiceBusException, InterruptedException {
		String connectionString = Objects.requireNonNull(env.getProperty("azure.serviceBus.connectionString"));
		String queueName = Objects.requireNonNull(env.getProperty("azure.serviceBus.reviewsQueueName"));
		this.receiveClient = new QueueClient(new ConnectionStringBuilder(connectionString, queueName),
				ReceiveMode.PEEKLOCK);

		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new JodaModule());
		this.mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
		this.mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

		this.latch = new CountDownLatch(1);
	}

	public void startReceiving(ExecutorService executor, ReviewHandler reviewHandler) throws Exception {
		
		IMessageHandler handler = new IMessageHandler() {
			public CompletableFuture<Void> onMessageAsync(IMessage message) {
				if (message != null) {
					String messageBody = new String(message.getBody());
					try {
						logger.info("Received message: {}", messageBody);
						Review review = mapper.readValue(messageBody, Review.class);
						reviewHandler.handle(review);
					} catch (IOException ex) {
						logger.error("Error processing message: {}", messageBody, ex);
					}
				}
				return CompletableFuture.completedFuture(null);
			}

			public void notifyException(Throwable exception, ExceptionPhase exceptionPhase) {
				logger.error("Error processing message: {}-{}", exceptionPhase, exception.getMessage(), exception);
			}
		};

		MessageHandlerOptions options = new MessageHandlerOptions(1, true, Duration.ofMinutes(1));

		receiveClient.registerMessageHandler(handler, options, executor);

		// block indefinitely
		latch.await();
	}


}
