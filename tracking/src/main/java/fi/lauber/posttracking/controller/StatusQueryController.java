package fi.lauber.posttracking.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import fi.lauber.posttracking.commandobject.InfoObject;
import fi.lauber.posttracking.domain.model.Tracking;
import fi.lauber.posttracking.util.TrackingLogic;

@Controller("statusQueryController")
@RequestMapping("/statusQuery")
public class StatusQueryController {

	public static final Logger logger = Logger.getLogger(StatusQueryController.class);
	
	@Autowired
	private TrackingLogic trackingLogic;
	
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView submit(@ModelAttribute("infoObj") InfoObject infoObj,
			BindingResult result, HttpServletRequest req) {
		logger.info("New status query request received (" + infoObj.getTrackingCode() + ").");
		Tracking tr = trackingLogic.getByTrackingCode(infoObj.getTrackingCode());
		if (tr == null) {
			ModelAndView mav = new ModelAndView("machineError","infoObj",infoObj);
			mav.addObject("information", "tracking-code-not-found-in-db");
			return mav;
		} else {
			ModelAndView mav = new ModelAndView("machineOk");
			mav.addObject("information", tr.getState());
			return mav;
		}
	}
	
}
