package fi.lauber.posttracking.commandobject;

public class InfoObject {

	private String trackingCode;
	private String emailAddress;
	private Boolean machine;
	private String subjectSuffix;
	private String sellerName;
	private String productName;

	public void setTrackingCode(String trackingCode) {
		this.trackingCode = trackingCode;
	}

	public String getTrackingCode() {
		return trackingCode;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setMachine(Boolean machine) {
		this.machine = machine;
	}

	/*public Boolean isMachine() {
		return machine;
	}*/

	public Boolean getMachine() {
		return machine;
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

}
