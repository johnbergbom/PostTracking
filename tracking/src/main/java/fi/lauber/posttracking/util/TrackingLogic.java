package fi.lauber.posttracking.util;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.log4j.Logger;

import fi.lauber.posttracking.commandobject.InfoObject;
import fi.lauber.posttracking.domain.model.Tracking;
import fi.lauber.posttracking.domain.model.dao.TrackingDAO;

@Component(value = "trackingLogic")
public class TrackingLogic {

	public static final Logger logger = Logger.getLogger(TrackingLogic.class);

	@Autowired
	private TrackingDAO trackingDAO;
	
	public Tracking getByTrackingCode(String trackingCode) {
		return trackingDAO.getByTrackingCode(trackingCode);
	}
	
	public void storeNewTrackingObject(InfoObject infoObj) {
		Tracking tracking = new Tracking();
		tracking.setEmail(infoObj.getEmailAddress());
		tracking.setTrackingCode(infoObj.getTrackingCode());
		tracking.setState(Tracking.NEW);
		tracking.setLastChanged(new Date());
		tracking.setLastTrackingCheck(new Date());
		tracking.setSubjectSuffix(infoObj.getSubjectSuffix());
		tracking.setSellerName(infoObj.getSellerName());
		tracking.setProductName(infoObj.getProductName());
		trackingDAO.save(tracking);
	}

	public void updateTrackingObject(Tracking tracking) {
		trackingDAO.update(tracking);
	}

	public List<Tracking> getAllOpenObjects() {
		List<Tracking> list = trackingDAO.getByState(Tracking.NEW);
		list.addAll(trackingDAO.getByState(Tracking.VISIBLE));
		list.addAll(trackingDAO.getByState(Tracking.ARRIVED));
		return list;
	}
}
