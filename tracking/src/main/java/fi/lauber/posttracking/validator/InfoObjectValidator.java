package fi.lauber.posttracking.validator;

import javax.mail.internet.InternetAddress;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import fi.lauber.posttracking.commandobject.InfoObject;

public class InfoObjectValidator implements Validator {

	public boolean supports(Class clazz) {
    	return InfoObject.class.isAssignableFrom(clazz);
	}

	//TODO: this is just copy'n'pasted from ContactInfoChecker - refactor!
	private static boolean correctEmailAddress(String emailAddress) {
		try {
			InternetAddress address = new InternetAddress(emailAddress,true);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public void validate(Object obj, Errors errors) {
	    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "trackingCode", "trackingCodeIsRequired");
	    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailAddress", "emailAddressIsRequired");
	    InfoObject infoObject = (InfoObject) obj;
	    if (!correctEmailAddress(infoObject.getEmailAddress())) {
	    	errors.rejectValue("emailAddress","illegalEmailAddress");
	    }
	    if (infoObject.getTrackingCode() != null
	    		&& infoObject.getTrackingCode().length() > infoObject.getTrackingCode().replaceAll(" ","").length()) {
	    	errors.rejectValue("trackingCode","trackingCodeCannotContainWhitespace");
	    }
	    if (infoObject.getTrackingCode() != null && infoObject.getTrackingCode().length() > 64) {
	    	errors.rejectValue("trackingCode","trackingCodeTooLong");
	    }
	    if (infoObject.getEmailAddress() != null && infoObject.getEmailAddress().length() > 64) {
	    	errors.rejectValue("emailAddress","emailAddressTooLong");
	    }
	    if (infoObject.getSubjectSuffix() != null && infoObject.getSubjectSuffix().length() > 64) {
	    	errors.rejectValue("subjectSuffix","subjectSuffixTooLong");
	    }
	    if (infoObject.getSellerName() != null && infoObject.getSellerName().length() > 64) {
	    	errors.rejectValue("sellerName","sellerNameTooLong");
	    }
	    if (infoObject.getProductName() != null && infoObject.getProductName().length() > 256) {
	    	errors.rejectValue("productName","productNameTooLong");
	    }
	}

}
