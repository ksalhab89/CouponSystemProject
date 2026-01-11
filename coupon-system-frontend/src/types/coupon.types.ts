export enum Category {
  SKYING = 10,
  SKY_DIVING = 20,
  FANCY_RESTAURANT = 30,
  ALL_INCLUSIVE_VACATION = 40
}

export interface Coupon {
  id: number;
  companyID: number;
  CATEGORY: number; // Category enum value
  title: string;
  description: string;
  startDate: string; // ISO date string
  endDate: string;
  amount: number;
  price: number;
  image: string;
}

export interface Company {
  id: number;
  name: string;
  email: string;
}

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}
