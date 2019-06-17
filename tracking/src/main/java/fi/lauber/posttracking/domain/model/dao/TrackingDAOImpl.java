package fi.lauber.posttracking.domain.model.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import fi.lauber.posttracking.domain.model.Tracking;

public class TrackingDAOImpl extends GenericDAOImpl<Tracking, Long> implements TrackingDAO {

	@Override
	public Class<Tracking> getEntityClass() {
		return Tracking.class;
	}

	public List<Tracking> getByEmail(String email) {
		DetachedCriteria criteria = DetachedCriteria.forClass(this.getEntityClass());
		criteria.add(Restrictions.eq("email", email));
		return this.getHibernateTemplate().findByCriteria(criteria);
	}

	public Tracking getByTrackingCode(String trackingCode) {
		DetachedCriteria criteria = DetachedCriteria.forClass(this.getEntityClass());
		criteria.add(Restrictions.eq("trackingCode", trackingCode));
		List<Tracking> list = this.getHibernateTemplate().findByCriteria(criteria);
		if (list == null || list.size() == 0) {
			return null;
		} 
		/* There will never be more than one tracking object here, because trackingCode
		 * is unique in the database. */
		return list.get(0);
	}
	
	public List<Tracking> getByState(int state) {
		DetachedCriteria criteria = DetachedCriteria.forClass(this.getEntityClass());
		criteria.add(Restrictions.eq("state", state));
		return this.getHibernateTemplate().findByCriteria(criteria);
	}

}
