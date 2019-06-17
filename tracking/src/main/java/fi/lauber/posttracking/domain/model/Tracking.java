package fi.lauber.posttracking.domain.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * This class defines objects to track.
 */
@Entity
@Table(name = "tracking")
@SequenceGenerator(allocationSize = 1, name = "TrackingSeq", sequenceName = "tracking_id_seq")
public class Tracking {

	public static int NEW = 1;
	public static int VISIBLE = 2;
	public static int ARRIVED = 3;
	public static int CLOSED = 4;
	public static int CLOSED_RETURNED = 5;
	public static int CLOSED_FAILED = 99;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TrackingSeq")
	private Long id;
	
	@Column(nullable = false, name = "tracking_code", unique=true, length=64)
	private String trackingCode;
	
	@Column(nullable = false, name = "email", length=64)
	private String email;
	
	@Column(nullable = false, name = "state")
	private Integer state;
	
	/**
	 * Tells when the latest information update came at the post's home page.
	 */
	@Column(nullable = false, name = "last_changed")
	private Date lastChanged;
	
	@Column(nullable = true, name = "subject_suffix", length=64)
	private String subjectSuffix;
	
	@Column(nullable = true, name = "seller_name", length=64)
	private String sellerName;
	
	@Column(nullable = true, name = "product_name", length=256)
	private String productName;
	
	/**
	 * Tells when the customer was last reminded about fetching his packet.
	 */
	@Column(nullable = true, name = "last_reminder")
	private Date lastReminder;
	
	/**
	 * Tells when the packet was last checked at the post's homepage.
	 */
	@Column(nullable = false, name = "last_track_check")
	private Date lastTrackingCheck;
	
	public Tracking() {
	}
	
	public Tracking(String trackingCode, String email) {
		this.trackingCode = trackingCode;
		this.email = email;
	}
	
	public Long getId() {
		return id;
	}

	public void setTrackingCode(String trackingCode) {
		this.trackingCode = trackingCode;
	}

	public String getTrackingCode() {
		return trackingCode;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getState() {
		return state;
	}

	public void setLastChanged(Date lastChanged) {
		this.lastChanged = lastChanged;
	}

	public Date getLastChanged() {
		return lastChanged;
	}

	public void setSubjectSuffix(String subjectSuffix) {
		this.subjectSuffix = subjectSuffix;
	}

	public String getSubjectSuffix() {
		return subjectSuffix;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}

	public String getSellerName() {
		return sellerName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public void setLastReminder(Date lastReminder) {
		this.lastReminder = lastReminder;
	}

	public Date getLastReminder() {
		return lastReminder;
	}

	public void setLastTrackingCheck(Date lastTrackingCheck) {
		this.lastTrackingCheck = lastTrackingCheck;
	}

	public Date getLastTrackingCheck() {
		return lastTrackingCheck;
	}

}
