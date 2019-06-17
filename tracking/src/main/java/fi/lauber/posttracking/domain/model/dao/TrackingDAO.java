package fi.lauber.posttracking.domain.model.dao;

import java.util.List;

import fi.lauber.posttracking.domain.model.Tracking;

public interface TrackingDAO extends GenericDAO<Tracking, Long> {

	public List<Tracking> getByEmail(String email);
	public Tracking getByTrackingCode(String trackingCode);
	public List<Tracking> getByState(int state);

}
