package com.jhf.coupon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Program {
	private static final Logger logger = LoggerFactory.getLogger(Program.class);

	public static void main(String[] args) {
		try {
			Test.testAll();
		} catch (Exception e) {
			logger.error("Application error occurred", e);
		}
	}
}
