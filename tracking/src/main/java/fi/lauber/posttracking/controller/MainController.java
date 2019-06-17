package fi.lauber.posttracking.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import fi.lauber.posttracking.commandobject.InfoObject;
import fi.lauber.posttracking.domain.model.Tracking;
import fi.lauber.posttracking.util.EmailUtils;
import fi.lauber.posttracking.util.TrackingLogic;
import fi.lauber.posttracking.validator.InfoObjectValidator;

/**
 * TODO: fixa separat build för production (mvn -Dprod clean package använder en annan datasource).
 * 
 * TODO: Fixa disclaimereita på sidan: "Emme vastaa mistään",
 * "Mikäli ilmoittamamme tieto on ristiriidassa www.posti.fi:n tietojen kanssa, niin www.posti.fi:n tarjoamaa tietoa tulee noudattaa.",
 * "Tämä palvelu tarjoaa vaan tietoja lähetyksen etenemisestä emmekä vastaa kyselyihin pakettiin liittyen. Kaikki kyselyt liittyen
 * tilaukseenne tulee osoittaa myyjääsi eikä meihin."
 * 
 * @author john
 *
 */
@Controller("mainController")
@RequestMapping("/")
public class MainController {

	public static final Logger logger = Logger.getLogger(MainController.class);
	
	@NotNull
	@Autowired
	private TrackingLogic trackingLogic;
	
	//@RequestMapping
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showMain(@RequestParam(required = false) String emailAddress,
			@RequestParam(required = false) String trackingCode,
			@RequestParam(required = false) String information) {
		logger.debug("Posttracking main: " + emailAddress + ", " + trackingCode);
		ModelAndView mav = new ModelAndView("index");
		//mav.addObject("title", "menu.title");
		InfoObject infoObj = new InfoObject();
		infoObj.setEmailAddress(emailAddress);
		infoObj.setTrackingCode(trackingCode);
		mav.addObject("infoObj", infoObj);
		mav.addObject("information", information);
		return mav;
	}
	
	private String errors2String(BindingResult result) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("globalErrors=");
		List globalErrors = result.getGlobalErrors();
		if (globalErrors != null) {
			for (int i = 0; i < globalErrors.size(); i++) {
				ObjectError objectError = (ObjectError) globalErrors.get(i);
				strBuf.append(objectError.getCode());
				Object[] args = objectError.getArguments();
				if (args != null && args.length > 0) {
					strBuf.append("[");
					for (int j = 0; j < args.length; j++) {
						strBuf.append(args[j] + (j < (args.length-1) ? "$" : ""));
					}
					strBuf.append("]");
				}
				if (i < (globalErrors.size()-1)) {
					strBuf.append("$");
				}
			}
		}
		strBuf.append("$");
		List fieldErrors = result.getFieldErrors();
		strBuf.append("fieldErrors=");
		if (fieldErrors != null) {
			for (int i = 0; i < fieldErrors.size(); i++) {
				FieldError fieldError = (FieldError) fieldErrors.get(i);
				strBuf.append(fieldError.getField() + ":");
				strBuf.append(fieldError.getCode());
				Object[] args = fieldError.getArguments();
				if (args != null && args.length > 0) {
					strBuf.append("[");
					for (int j = 0; j < args.length; j++) {
						strBuf.append(args[j] + (j < (args.length-1) ? "$" : ""));
					}
					strBuf.append("]");
				}
				if (i < (fieldErrors.size()-1)) {
					strBuf.append("$");
				}
			}
		}
		strBuf.append("$");
		return strBuf.toString();
	}

	//@RequestMapping()
	//@RequestMapping(method = { RequestMethod.POST, RequestMethod.GET }, value = { "submit" } )
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView submit(@ModelAttribute("infoObj") InfoObject infoObj,
			BindingResult result, HttpServletRequest req) {
		logger.info("New tracking request received (trackingCode = " + infoObj.getTrackingCode()
				+ ", emailAddress = " + infoObj.getEmailAddress()
				+ ", machine = " + infoObj.getMachine() + ")");
		//ModelAndView mav = new ModelAndView("redirect:/tracking/?emailAddress=aaa");
		//return mav;
		new InfoObjectValidator().validate(infoObj, result);
		if (!result.hasErrors()) {
			/* Check if the tracking code already exists in the database. */
			Tracking tr = trackingLogic.getByTrackingCode(infoObj.getTrackingCode());
			if (tr != null) {
				// For some reason the email address doesn't show when using a global error, so
				// instead we use a field level error for this one.
				//result.reject("packetAlreadyTrackedBy", new Object[]{ tr.getEmail() }, null);
				result.rejectValue("trackingCode", "packetAlreadyTrackedBy", new Object[]{ tr.getEmail() }, null);
			}
		}
		// If there are no errors and it's not a machine that made the tracking request, then
		// send an email to the customer telling that tracking has started.
		if (result.hasErrors() || ((infoObj.getMachine() == null || !infoObj.getMachine()) && !sendEmail(infoObj, result))) {
			logger.info("Tracking request contained errors.");
			if ((infoObj.getMachine() != null && infoObj.getMachine())) {
				ModelAndView mav = new ModelAndView("machineError","infoObj",infoObj);
				mav.addObject("information", errors2String(result));
				return mav;
			} else {
				return new ModelAndView("index","infoObj",infoObj);
			}
		} else {
			logger.info("Adding new tracking for trackingCode " + infoObj.getTrackingCode()
					+ " and email address " + infoObj.getEmailAddress());
			trackingLogic.storeNewTrackingObject(infoObj);
			if ((infoObj.getMachine() != null && infoObj.getMachine())) {
				return new ModelAndView("machineOk");
			} else {
				ModelAndView mav = new ModelAndView("redirect:/tracking/");
				mav.addObject("information", "trackingRequestProcessedSuccessfully");
				return mav;
			}
		}
	}
	
	//TODO: change this so that new tracking objects should just be added to the database and then there is a
	//separate thread that takes care of the email sending, so that we won't need to display any error to the
	//user in case sending of the initial email fails.
	private boolean sendEmail(InfoObject infoObj, Errors errors) {
		try {
			EmailUtils.sendEmail("Moi,\n\n" + (infoObj.getSellerName() != null ? infoObj.getSellerName() + " ilmoittaa:\n\n" : "")
					+ "Seurataan tuotetta "
					+ (infoObj.getProductName() != null ? infoObj.getProductName() + " " : "")
					+ "jonka seurantakoodi on " + infoObj.getTrackingCode() + "."
					+ " Saat ilmoituksen heti kun lähetyksen tiedot ovat kirjautuneet seurantajärjestelmään.\n\n"
					+ "Terveisin,\nSeuraapostia",
					infoObj.getEmailAddress(), "Seuranta on aloitettu"
					+ (infoObj.getSubjectSuffix() != null && !infoObj.getSubjectSuffix().trim().equals("")
							? infoObj.getSubjectSuffix() : ""));
			return true;
		} catch (Exception e) {
			logger.info("Couldn't send confirmation email to customer, asking the customer to try later.");
			logger.debug("Couldn't send confirmation email to customer, asking the customer to try later: ", e);
			errors.reject("couldNotSendConfirmationEmail");
			return false;
		}
	}
	
}
