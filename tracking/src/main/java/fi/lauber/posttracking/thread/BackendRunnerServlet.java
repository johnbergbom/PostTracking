package fi.lauber.posttracking.thread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import fi.lauber.posttracking.util.EmailUtils;

//TODO: figure out why this stops working after some time (24 hours or so. The problem might be
//in the timer somehow. Perhaps try using the timer.schedule(TimerTask task, long delay, long period)
//fixed-delay execution instead, in case that works better. I noticed also that gpsreceiver uses
//a mutex in the method corresponding to scheduleBackendRunner(). That can also be tried. In addition
//it uses "timer = new Timer()" instead of "timer = new Timer(true)" in its startTimer method. Most
//likely this is wrongly done in gpsreceiver, but you never know... Perhaps this could also be tried
//here.

// TODO: When we are assured that the run does no longer bail out, then fix the following things:
// 1.) Återställ i BackendLogic så att tracking görs olika ofta beroende på i vilket state de är
// 2.) Återställ log4j.xml till dess tidigare värde
// 3.) Ta bort test-debugutskrifterna i BackendLogic

/**
 * The backend runner can be started by hand with the following command:
 * At sellstar.fi: lynx http://localhost:8080/posttracking/backendrunner/startrun
 */
public class BackendRunnerServlet extends HttpServlet {

	private static final long serialVersionUID = 7132223979686178009L;

	public static final Logger logger = Logger.getLogger(BackendRunnerServlet.class);

	private Timer timer;
	private BackendLogic backendLogic;
	
	public void init() throws ServletException {
		logger.info("Initializing back end servlet");
		try {
			ApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
			backendLogic = (BackendLogic) context.getBean("backendLogic");
			//timer = new Timer(true);
			timer = new Timer();
			scheduleBackendRunner(1000*3600*3);// Run the back end stuff once every 6 hours.
		} catch (Exception e) {
			logger.error("Initializing failed: ", e);
		}
	}
	
	private void scheduleBackendRunner(long delayUntilStart) {
		if (timer != null) {
			// Run the back end stuff once every 6 hours.
			try {
				timer.schedule(new BackendRunnerTimerTask(), delayUntilStart);
			} catch (Exception e) {
				logger.error("Error in scheduleBackendRunner: ", e);
			}
		} else {
			logger.debug("Timer canceled, not scheduling.");
		}
	}
	
	/* TODO: This method is just copy'n'pasted from SellStar's Test.java - refactor! */
	public static String getStackTrace(Throwable t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		t.printStackTrace(ps);
		ps.close();
		return new String(baos.toByteArray());
	}
	
	public class BackendRunnerTimerTask extends TimerTask {

		@Override
		public void run() {
			try {
				logger.info("Running backend timer task.");
				logger.info("Calling backendLogic from servlet");
				backendLogic.updateTrackingStatuses();
				logger.info("Returned to servlet after calling backendLogic");
			} catch (Throwable e) {
				System.out.println("Error in BackendRunnerServlet.");
				logger.error("Posttracking: exception in back end: ", e);
				logger.info("Informing operator about the problem.");
				//Now and then email sending bails out. It might work if we wait a little before trying again.
				logger.debug("Sleeping for three minutes.");
				try { Thread.sleep(1000*60*3); } catch (Exception e2) { }
				logger.debug("Woke up from sleep.");
				try {
					EmailUtils.sendEmail("Exception in posttracking.BackendRunnerTimerTask:\n\n"
							+ getStackTrace(e), "someemail@gmail.com", "Posttracker error");
				} catch (Throwable e2) {
					System.out.println("Fatal error.");
					logger.fatal("Couldn't send error info to operator: ", e2);
				}
			} finally {
				logger.debug("Innan scheduling");
				scheduleBackendRunner(1000*3600*3);// Run the back end stuff once every 6 hours.
				logger.debug("Efter scheduling");
			}
		}
		
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info("Post request received");
		String command = req.getPathInfo();
		logger.info("command = " + command);
		if (command == null || !command.equals("/startrun")) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			logger.info("Starting run by operator command.");
			startTimer();
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info("Get request received");
		doPost(req,resp);
	}

	private void startTimer() {
		stopTimer();
		//timer = new Timer(true);
		timer = new Timer();
		scheduleBackendRunner(0); // Start run right away
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
		}
		timer = null;
	}

	public void destroy() {
		logger.debug("Destroying timer");
		stopTimer();
		logger.debug("Timer destroyed");
	}
	
}
