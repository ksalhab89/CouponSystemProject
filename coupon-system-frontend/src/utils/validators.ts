// Email validation
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Password validation (at least 8 characters)
export const isValidPassword = (password: string): boolean => {
  return password.length >= 8;
};

// Price validation (must be positive)
export const isValidPrice = (price: number): boolean => {
  return price > 0;
};

// Amount validation (must be non-negative integer)
export const isValidAmount = (amount: number): boolean => {
  return Number.isInteger(amount) && amount >= 0;
};

// Date range validation (start < end, end > today)
export const isValidDateRange = (startDate: string, endDate: string): boolean => {
  const start = new Date(startDate);
  const end = new Date(endDate);
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  return start < end && end >= today;
};

// Required field validation
export const isRequired = (value: string | number): boolean => {
  if (typeof value === 'string') {
    return value.trim().length > 0;
  }
  return value !== null && value !== undefined;
};

// Validation error messages
export const getValidationError = (field: string, value: any): string | null => {
  switch (field) {
    case 'email':
      if (!isRequired(value)) return 'Email is required';
      if (!isValidEmail(value)) return 'Invalid email format';
      break;
    case 'password':
      if (!isRequired(value)) return 'Password is required';
      if (!isValidPassword(value)) return 'Password must be at least 8 characters';
      break;
    case 'price':
      if (!isRequired(value)) return 'Price is required';
      if (!isValidPrice(value)) return 'Price must be greater than 0';
      break;
    case 'amount':
      if (!isRequired(value)) return 'Amount is required';
      if (!isValidAmount(value)) return 'Amount must be a non-negative integer';
      break;
    default:
      if (!isRequired(value)) return `${field} is required`;
  }
  return null;
};
