package io.pipeline.prediction.jvm;

import static org.junit.Assert.*;

import java.util.concurrent.Future;

import org.junit.Test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

public class UserItemBatchPredictionCollapserTest {
  @Test
  public void testCollapser() {
    HystrixRequestContext context = HystrixRequestContext.initializeContext();

    try {
      // TODO:  use real values here
  	  Future<Object> f1 = new UserItemBatchPredictionCollapser("collapsed_recommendation_user_item_prediction", "my_namespace", "my_version", 100, 10, 10, -1.0d, "21619", "10006").queue();
  	  Future<Object> f2 = new UserItemBatchPredictionCollapser("collapsed_recommendation_user_item_prediction", "my_namespace", "my_version", 100, 10, 10, -1.0d, "21619", "10006").queue();
  	  Future<Object> f3 = new UserItemBatchPredictionCollapser("collapsed_recommendation_user_item_prediction", "my_namespace", "my_version", 100, 10, 10, -1.0d, "21619", "10006").queue();
  	  Future<Object> f4 = new UserItemBatchPredictionCollapser("collapsed_recommendation_user_item_prediction", "my_namespace", "my_version", 100, 10, 10, -1.0d, "21619", "10006").queue();
  	  
      assertEquals(0.891, (Double)f1.get(), 0.01);
      assertEquals(0.891, (Double)f2.get(), 0.01);
      assertEquals(0.891, (Double)f3.get(), 0.01);
      assertEquals(0.891, (Double)f4.get(), 0.01);

      int numExecuted = HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size();

      System.err.println("num executed: " + numExecuted);

      // assert that the batch command 'GetValueForKey' was in fact executed and that it executed only 
      // once or twice (due to non-determinism of scheduler since this example uses the real timer)
      if (numExecuted > 2) {
        fail("some of the commands should have been collapsed");
      }

      System.err.println("HystrixRequestLog.getCurrentRequest().getAllExecutedCommands(): " + HystrixRequestLog.getCurrentRequest().getAllExecutedCommands());
      int numLogs = 0;
      
      for (Object command: HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().toArray()) {     
  	    HystrixCommand<java.util.List<Double>> hystrixCommand = (HystrixCommand<java.util.List<Double>>)command;
	    numLogs++;
    
        // assert the command is the one we're expecting
        assertEquals("collapsed_recommendation_user_item_prediction", hystrixCommand.getCommandKey().name());

        System.err.println(hystrixCommand.getCommandKey().name() + " => command.getExecutionEvents(): " + hystrixCommand.getExecutionEvents());

        // confirm that it was a COLLAPSED command execution
        assertTrue(hystrixCommand.getExecutionEvents().contains(HystrixEventType.COLLAPSED));
        assertTrue(hystrixCommand.getExecutionEvents().contains(HystrixEventType.SUCCESS));
      }

      assertEquals(numExecuted, numLogs);
    } catch(Exception exc) {
    	exc.printStackTrace();
    } 
    finally {
      context.shutdown();
    }
  }  
}