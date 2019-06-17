package fi.lauber.posttracking.thread;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.lauber.posttracking.domain.model.Tracking;
import fi.lauber.posttracking.util.EmailUtils;
import fi.lauber.posttracking.util.TrackingLogic;

@Component(value = "backendLogic")
public class BackendLogic {

	public static final Logger logger = Logger.getLogger(BackendLogic.class);
	
	@Autowired
	private TrackingLogic trackingLogic;
	
	public void updateTrackingStatuses() {
		//Tracking tr = trackingLogic.getByTrackingCode("ddd");
		//logger.info("email = " + tr.getEmail());
		List<Tracking> list = trackingLogic.getAllOpenObjects();
		List<Tracking> packetsToCheck = new ArrayList<Tracking>();
		for (Tracking tracking : list) {
			/*boolean runCheck = false;
			long minus24Hours = System.currentTimeMillis() - 1000*60*24;
			if (tracking.getState().equals(Tracking.NEW)) {
				/* Check once every 24 hours for new packets. /
				if (tracking.getLastTrackingCheck().getTime() < minus24Hours) {
					runCheck = true;
				}
			} else if (tracking.getState().equals(Tracking.VISIBLE)) {
				/* Check for every run if visible. /
				runCheck = true;
			} else if (tracking.getState().equals(Tracking.ARRIVED)) {
				/* Check once every 24 hours for new packets. /
				if (tracking.getLastTrackingCheck().getTime() < minus24Hours) {
					runCheck = true;
				}
			} else {
				throw new RuntimeException("Tracking object " + tracking.getId() + " shouldn't be included.");
			}
			if (runCheck) {
				packetsToCheck.add(tracking);
			}*/
			packetsToCheck.add(tracking);
		}
		logger.info("Updating statuses for " + packetsToCheck.size() + " packets (of totally " + list.size() + ").");
		int failed = 0;
		int succeeded = 0;
		for (Tracking tracking : packetsToCheck) {
			logger.debug("ettan");
			String body = null;
			try {
				logger.debug("Fetching data for tracking object " + tracking.getId() + ".");
				body = getPageContents(tracking.getTrackingCode());
				logger.debug("tvaan");
			} catch (Exception e) {
				logger.debug("Data fetching for tracking object " + tracking.getId() + " failed:",e);
				failed++;
			}
			logger.debug("trean");
			if (body != null) {
				if (body.indexOf("Virheellinen tunnus") > 0) {
					// Faulty tracking code. Close the tracking object.
					logger.info("Tracking object " + tracking.getId() + " goes to closed_failed (faulty tracking code).");
					tracking.setState(Tracking.CLOSED_FAILED);
					tracking.setLastTrackingCheck(new Date());
					trackingLogic.updateTrackingObject(tracking);
					EmailUtils.sendEmail("Moi,\n\n" + (tracking.getSellerName() != null ? tracking.getSellerName() + " ilmoittaa:\n\n" : "")
							+ "\n\nSeurantakoodisi " + tracking.getTrackingCode() + " on virheellinen"
							+ (tracking.getProductName() != null ? " (tuote " + tracking.getProductName() + ")." : ".")
							+ "\n\nTerveisin,\nSeuraapostia",
							tracking.getEmail(), "Virheellinen tunnus"
							+ (tracking.getSubjectSuffix() != null ? tracking.getSubjectSuffix() : ""));
					succeeded++;
				} else if (body.indexOf("hetyksen tietoja ei l") > 0 && body.indexOf("hetyksen tiedot eiv") > 0) {
					// The packet doesn't yet show up. If it was last updated more than 60 days ago,
					// then close the tracking object, otherwise do nothing.
					Calendar daysAgo60 = Calendar.getInstance();
					daysAgo60.add(Calendar.DATE,-60);
					if (tracking.getLastChanged().before(daysAgo60.getTime())) {
						logger.info("Tracking object " + tracking.getId() + " goes to closed_failed.");
						tracking.setState(Tracking.CLOSED_FAILED);
						trackingLogic.updateTrackingObject(tracking);
						EmailUtils.sendEmail("Moi,\n\n" + (tracking.getSellerName() != null ? tracking.getSellerName() + " ilmoittaa:\n\n" : "")
								+ "\n\nSeurantakoodisi " + tracking.getTrackingCode()
								+ (tracking.getProductName() != null ? " (tuote " + tracking.getProductName() + ")" : "")
								+ " ei ole vieläkään kirjautuneet seurantajärjestelmään (60 päivän jälkeen). Seuranta lopetetaan."
								+ "\n\nTerveisin,\nSeuraapostia",
								tracking.getEmail(), "Seuranta epäonnistui"
								+ (tracking.getSubjectSuffix() != null ? tracking.getSubjectSuffix() : ""));
					} else {
						logger.debug("Tracking object " + tracking.getId() + " doesn't yet show up.");
						tracking.setLastTrackingCheck(new Date());
						trackingLogic.updateTrackingObject(tracking);
					}
					succeeded++;
				} else {
					Map<String,Date> latestDateMap = new HashMap<String,Date>();
					String customerInfo = extractCustomerInfo(tracking,body,latestDateMap);
					Date latestDate = latestDateMap.get("latestDate");
					if (latestDate == null) {
						logger.debug("Parsing of tracking object " + tracking.getId() + " failed: " + body);
						// parsing failed
						failed++;
						continue;
					} else {
						boolean newInfoReceived = false;
						if (!latestDate.equals(tracking.getLastChanged())) {
							logger.info("Updated information received for tracking object " + tracking.getId());
							newInfoReceived = true;
						}
						tracking.setLastChanged(latestDate);
						tracking.setLastTrackingCheck(new Date());
						if (customerInfo.indexOf("hetys on luovutettu vastaanottajalle") > 0) {
							logger.info("Closing tracking object " + tracking.getId() + " because the customer fetched the packet.");
							tracking.setState(Tracking.CLOSED);
						} else if (customerInfo.indexOf("hetys on vastaanotettu kuljetuksesta") > 0) {
							logger.info("Closing tracking object " + tracking.getId() + " because the packet is delivered to the customer.");
							tracking.setState(Tracking.CLOSED);
						} else if (customerInfo.indexOf("ei ole noudettu") > 0 && customerInfo.indexOf("Palautettu l") > 0) {
							logger.info("Closing tracking object " + tracking.getId() + " because the packet was returned to provider.");
							tracking.setState(Tracking.CLOSED_RETURNED);
							//tracking.setState(Tracking.CLOSED);
						} else {
							if (customerInfo.indexOf("Saapunut toimipaikkaan/terminaaliin") > 0) {
								logger.info("Tracking object " + tracking.getId() + " has arrived.");
								tracking.setState(Tracking.ARRIVED);
							} else {
								tracking.setState(Tracking.VISIBLE);
							}
							String productName = (tracking.getProductName() != null ? "Tuote " + tracking.getProductName() + ")" : "");
							if (newInfoReceived) {
								logger.info("Latest date " + latestDate);
								logger.info(customerInfo);
								EmailUtils.sendEmail("Moi,\n\n" + (tracking.getSellerName() != null
										? tracking.getSellerName() + " ilmoittaa:\n\n" : "")
										+ "\n\n" + (tracking.getState().equals(Tracking.ARRIVED)
										? (tracking.getProductName() != null ? "Tuote " + tracking.getProductName() : "Pakettisi")
												+ " on nyt haettavissa postista." : "")
										+ "\n\n" + customerInfo
										+ "\n\nTerveisin,\nSeuraapostia",
										tracking.getEmail(), "Seurantatietoja"
										+ (tracking.getSubjectSuffix() != null ? tracking.getSubjectSuffix() : ""));
							} else if (tracking.getState().equals(Tracking.ARRIVED)) {
								Calendar daysAgo45 = Calendar.getInstance();
								daysAgo45.add(Calendar.DATE,-45);
								Calendar daysAgo5 = Calendar.getInstance();
								daysAgo5.add(Calendar.DATE,-5);
								if (tracking.getLastChanged().before(daysAgo45.getTime())) {
									/* Sometimes the post screws up and delivers the packet without updating the status,
									 * so close the packet if it has been in state "arrived" for more than 45 days.
									 * Note that this doesn't interfere with "returned to provider", because packets
									 * are normally returned before 45 days have gone by. */
									logger.info("Tracking object " + tracking.getId() + " goes to closed after being"
											+ " in state \"arrived\" for more than 45 days.");
									tracking.setState(Tracking.CLOSED);
									//trackingLogic.updateTrackingObject(tracking);
								} else if (tracking.getLastChanged().before(daysAgo5.getTime())
										&& (tracking.getLastReminder() == null/* || tracking.getLastReminder().before(daysAgo5.getTime())*/)) {
									/* If the packet has been in the state arrived for more that five
									 * days, then remind the customer about fetching the packet. */
									DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
									EmailUtils.sendEmail("Moi,\n\n" + (tracking.getSellerName() != null
											? tracking.getSellerName() + " ilmoittaa:\n\n" : "")
											+ "\n\n"
											+ (tracking.getProductName() != null ? "Tuote " + tracking.getProductName() : "Pakettisi")
											+ " on ollut haettavissa postista jo "
											+ dateFormat.format(latestDate) + " asti." + "\n\n" + customerInfo
											+ "\n\nTerveisin,\nSeuraapostia",
											tracking.getEmail(), "Noutomuistutus"
											+ (tracking.getSubjectSuffix() != null ? tracking.getSubjectSuffix() : ""));
									tracking.setLastReminder(new Date());
								}
							} else if (tracking.getState().equals(Tracking.VISIBLE)) {
								/* Sometimes the post screws up and delivers the packet without updating the status,
								 * so close the packet if it has been in state "visible" for more than 45 days.
								 * Note that this doesn't interfere with "returned to provider", because packets
								 * are normally returned before 45 days have gone by. */
								Calendar daysAgo45 = Calendar.getInstance();
								daysAgo45.add(Calendar.DATE,-45);
								if (tracking.getLastChanged().before(daysAgo45.getTime())) {
									logger.info("Tracking object " + tracking.getId() + " goes to closed after being"
											+ " in state \"visible\" for more than 45 days.");
									tracking.setState(Tracking.CLOSED);
									//trackingLogic.updateTrackingObject(tracking);
								}
							}
						}
						succeeded++;
						trackingLogic.updateTrackingObject(tracking);
					}
				}
			}
		}
		if (failed > 5) {
			logger.info("Number of failed = " + failed + " - informing operator (succeeded = " + succeeded + ").");
			EmailUtils.sendEmail("Too many failures in backend (" + failed + "). Backend still running (succeeded = " + succeeded + ").",
					"someemail@gmail.com", "Posttracker operation problem");
		}
	}
	
	private String getPageContents(String trackingCode) throws Exception {
		logger.debug("a");
		HttpClient client = new DefaultHttpClient();
		logger.debug("b");
		String urlGetStr = "http://www.verkkoposti.com/e3/TrackinternetServlet"
			+ "?lang=fi&LOTUS_hae=Hae&LOTUS_side=1&LOTUS_trackId=" + trackingCode + "&LOTUS_hae=Hae";
		logger.debug("c: " + urlGetStr);
		HttpGet getMethod = new HttpGet(urlGetStr);
		logger.debug("d");
		ResponseHandler<String> respHandler = new BasicResponseHandler();
		logger.debug("e");
		String body = client.execute(getMethod,respHandler);
		logger.debug("f.body = " + body);
		return body;
	}
	
	private String escapeHTML2String(String text) {
		String returnText = text.replaceAll("&aring;", "å").replaceAll("&Aring;", "Å");
		returnText = returnText.replaceAll("&auml;", "ä").replaceAll("&Auml;", "Ä");
		returnText = returnText.replaceAll("&ouml;", "ö").replaceAll("&Ouml;", "Ö");
		returnText = returnText.replaceAll("&euro;", "€").replaceAll("&pound;", "£");
		returnText = returnText.replaceAll("&nbsp;", " ").replaceAll("<sup>", "");
		returnText = returnText.replaceAll("</sup>", "");
		return returnText;
	}

	private String removeBold(String text) {
		return text.replaceAll("<b>", "").replaceAll("</b>", "");
	}

	private String bold2NewLine(String text) {
		return text.replaceAll("<b>", "\n").replaceAll("</b>", "\n");
	}

	private String extractRowInfo(String row, Map<String,Date> latestDateMap) {
		StringBuffer strBuf = new StringBuffer();
		String refinedRow = escapeHTML2String(row.trim());
		if (refinedRow.indexOf("Hae seuraava l") >= 0) {
			return "";
		}
		if (refinedRow.startsWith("Palvelu") || refinedRow.startsWith("Lisäpalvelut") || refinedRow.startsWith("Paino") || refinedRow.startsWith("Tilavuus") || refinedRow.startsWith("Lähetystunnus")) {
			strBuf.append(removeBold(refinedRow));
			if (refinedRow.startsWith("Lähetystunnus")) {
				strBuf.append("\n");
			}
		} else {
			Date latestDate = (Date) latestDateMap.get("latestDate");
			DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy,' klo 'HH:mm");
			try {
				Date date = dateFormat.parse(refinedRow);
				if (latestDate == null || date.after(latestDate)) {
					latestDateMap.put("latestDate", date);
				}
			} catch (Exception e) {
				System.out.println("Parse error for " + refinedRow);
			}
			strBuf.append(bold2NewLine(refinedRow) + "\n");
		}
		return strBuf.toString();
	}

	public static String trimSpacesAtEachLine(String text) {
		return text.trim().replaceAll("\n( )+", "\n").replaceAll("\n(\t)+", "\n");
	}

	private String extractCustomerInfo(Tracking tracking, String body, Map<String,Date> latestDateMap) {
		int pos = 0;
		String row = "";
		String fieldStartTag = "<p class=\"resulttext\">";
		String fieldStopTag = "</p>";
		StringBuffer strBuf = new StringBuffer();
		do {
			int index = body.indexOf(fieldStartTag,pos);
			if (index < 0) {
				row = null;
			} else {
				//int length = index + field.length();
				//String str = body.substring(index + field.length());
				int endIndex = body.indexOf(fieldStopTag,index + fieldStartTag.length());
				if (endIndex < 0) {
					row = null;
				} else {
					row = body.substring(index + fieldStartTag.length(),endIndex);
				}
				pos = endIndex + fieldStopTag.length();
			}
			if (row != null) {
				strBuf.append(extractRowInfo(row,latestDateMap) + "\n");
			}
		} while (row != null);
		return trimSpacesAtEachLine(strBuf.toString());
	}
	
}
