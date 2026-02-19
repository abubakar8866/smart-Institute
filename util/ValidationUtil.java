package util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ValidationUtil {

	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

	private ValidationUtil() {
	}

	public static boolean isValidEmail(String email) {
		return email != null && Pattern.matches(EMAIL_REGEX, email);
	}

	public static boolean isValidFees(BigDecimal fees) {
		return fees != null && fees.compareTo(BigDecimal.ZERO) >= 0;
	}

	public static boolean isNotBlank(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
