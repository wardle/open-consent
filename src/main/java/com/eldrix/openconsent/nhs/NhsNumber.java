/**
 * NhsNumber helper functions
 * package: miscellaneous NHS-specific utility classes
 */
package com.eldrix.openconsent.nhs;

/**
 * Utility class for NhsNumber functions
 * 
 * At present, this class only provides a static utility function to validate an individual NHS Number.
 * 
 *
 */
public class NhsNumber {
	
	/**
	 * Validate an NHS number
	 * 
	 * see <a href="http://www.govtalk.gov.uk/gdsc/html/noframes/NHSnumber-2-0-Release.htm">govtalk</a> and
	 * <a href="http://www.connectingforhealth.nhs.uk/systemsandservices/nsts/docs/tech_nn_check_digit.pdf">Connecting for Health</a> websites.
	 *
	 * Note: This does not check for repeated (and supposedly invalid) NHS numbers such as 4444444444 and 6666666666
	 * This is only an issue for NHS number generation and not the validation we have here.
	 *  
	 * @param nnn String representing the NHS number (a 10 digit numeric only string)
	 * @return boolean - whether number is valid or not
	 */
	public static boolean validate(String nnn) {
		if (nnn!=null && nnn.length()==10) {
			int nni[] = new int[10];
			int sum=0, cd=0;
			for (int i=0; i<10; i++) {
				char c = nnn.charAt(i);
				if (Character.isDigit(c)==false) return false;
				nni[i] = Integer.parseInt(String.valueOf(nnn.charAt(i)));	// nni[i] = nnn.charAt(i) - '0';	// is probably more efficient
				if (i<9) sum += nni[i] * (10-i);
			}
			cd = 11 - (sum % 11);
			if (cd==11) cd=0;
			if (cd!=10 & cd==nni[9]) return true;
		}
		return false;
	}
}
