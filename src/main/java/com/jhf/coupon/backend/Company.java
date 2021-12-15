package com.jhf.coupon.backend;

import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Company {
	private int id;
	private String name;
	private String email;
	private String password;
	private ArrayList<Coupon> coupon;

}
