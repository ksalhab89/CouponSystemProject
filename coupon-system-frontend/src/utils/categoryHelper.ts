import { Category } from '../types/coupon.types';

// Map category ID to display name
export const getCategoryName = (id: number): string => {
  const categoryMap: Record<number, string> = {
    [Category.SKYING]: 'Skiing',
    [Category.SKY_DIVING]: 'Sky Diving',
    [Category.FANCY_RESTAURANT]: 'Fancy Restaurant',
    [Category.ALL_INCLUSIVE_VACATION]: 'All Inclusive Vacation'
  };
  return categoryMap[id] || 'Unknown Category';
};

// Get all categories as array for dropdowns
export const getAllCategories = (): Array<{ id: number; name: string }> => {
  return [
    { id: Category.SKYING, name: 'Skiing' },
    { id: Category.SKY_DIVING, name: 'Sky Diving' },
    { id: Category.FANCY_RESTAURANT, name: 'Fancy Restaurant' },
    { id: Category.ALL_INCLUSIVE_VACATION, name: 'All Inclusive Vacation' }
  ];
};

// Validate category ID
export const isValidCategory = (id: number): boolean => {
  return Object.values(Category).includes(id);
};
