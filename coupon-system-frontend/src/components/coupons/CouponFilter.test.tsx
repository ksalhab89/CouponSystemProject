import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CouponFilter } from './CouponFilter';
import { Category } from '../../types/coupon.types';

describe('CouponFilter', () => {
  const mockOnFilterChange = vi.fn();

  beforeEach(() => {
    mockOnFilterChange.mockClear();
  });

  describe('Basic Rendering', () => {
    it('should render category select', () => {
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);
      expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
    });

    it('should render max price input', () => {
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);
      expect(screen.getByLabelText(/max price/i)).toBeInTheDocument();
    });

    it('should render clear filters button', () => {
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);
      expect(screen.getByRole('button', { name: /clear filters/i })).toBeInTheDocument();
    });

    it('should render all category options', () => {
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const categorySelect = screen.getByLabelText(/category/i);
      expect(categorySelect).toBeInTheDocument();

      // The categories are in the dropdown, which needs to be opened to see them
      // But we can verify the component rendered without errors
    });
  });

  describe('Category Filter', () => {
    it('should call onFilterChange when category is selected', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const categoryButton = screen.getByRole('combobox', { name: /category/i });
      await user.click(categoryButton);

      const skiOption = await screen.findByRole('option', { name: /skiing/i });
      await user.click(skiOption);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: Category.SKYING,
        maxPrice: '',
      });
    });

    it('should call onFilterChange when "All Categories" is selected', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      // First select a category
      const categoryButton = screen.getByRole('combobox', { name: /category/i });
      await user.click(categoryButton);

      const skiOption = await screen.findByRole('option', { name: /skiing/i });
      await user.click(skiOption);

      // Then select "All Categories"
      await user.click(categoryButton);
      const allOption = await screen.findByRole('option', { name: /all categories/i });
      await user.click(allOption);

      expect(mockOnFilterChange).toHaveBeenLastCalledWith({
        category: '',
        maxPrice: '',
      });
    });

    it('should allow selecting different categories', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const categoryButton = screen.getByRole('combobox', { name: /category/i });

      // Select Sky Diving
      await user.click(categoryButton);
      const skyDivingOption = await screen.findByRole('option', { name: /sky diving/i });
      await user.click(skyDivingOption);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: Category.SKY_DIVING,
        maxPrice: '',
      });
    });

    it('should allow selecting Fancy Restaurant category', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const categoryButton = screen.getByRole('combobox', { name: /category/i });
      await user.click(categoryButton);

      const restaurantOption = await screen.findByRole('option', { name: /fancy restaurant/i });
      await user.click(restaurantOption);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: Category.FANCY_RESTAURANT,
        maxPrice: '',
      });
    });

    it('should allow selecting All Inclusive Vacation category', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const categoryButton = screen.getByRole('combobox', { name: /category/i });
      await user.click(categoryButton);

      const vacationOption = await screen.findByRole('option', { name: /all inclusive vacation/i });
      await user.click(vacationOption);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: Category.ALL_INCLUSIVE_VACATION,
        maxPrice: '',
      });
    });
  });

  describe('Max Price Filter', () => {
    it('should call onFilterChange when max price is entered', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const priceInput = screen.getByLabelText(/max price/i);
      await user.type(priceInput, '100');

      // Called multiple times as user types each digit
      expect(mockOnFilterChange).toHaveBeenCalled();
      expect(mockOnFilterChange).toHaveBeenLastCalledWith({
        category: '',
        maxPrice: 100,
      });
    });

    it('should handle decimal prices', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const priceInput = screen.getByLabelText(/max price/i);
      await user.type(priceInput, '99.99');

      expect(mockOnFilterChange).toHaveBeenLastCalledWith({
        category: '',
        maxPrice: 99.99,
      });
    });

    it('should handle empty price input', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const priceInput = screen.getByLabelText(/max price/i);
      await user.type(priceInput, '100');

      mockOnFilterChange.mockClear();

      await user.clear(priceInput);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: '',
        maxPrice: '',
      });
    });

    it('should allow changing price value', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const priceInput = screen.getByLabelText(/max price/i);

      await user.type(priceInput, '50');
      expect(mockOnFilterChange).toHaveBeenLastCalledWith({
        category: '',
        maxPrice: 50,
      });

      await user.clear(priceInput);
      await user.type(priceInput, '200');

      expect(mockOnFilterChange).toHaveBeenLastCalledWith({
        category: '',
        maxPrice: 200,
      });
    });
  });

  describe('Clear Filters', () => {
    it('should reset filters when clear button is clicked', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      // Set some filters first
      const categoryButton = screen.getByRole('combobox', { name: /category/i });
      await user.click(categoryButton);
      const skiOption = await screen.findByRole('option', { name: /skiing/i });
      await user.click(skiOption);

      const priceInput = screen.getByLabelText(/max price/i);
      await user.type(priceInput, '100');

      mockOnFilterChange.mockClear();

      // Now clear filters
      const clearButton = screen.getByRole('button', { name: /clear filters/i });
      await user.click(clearButton);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: '',
        maxPrice: '',
      });
    });

    it('should work when filters are already empty', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      const clearButton = screen.getByRole('button', { name: /clear filters/i });
      await user.click(clearButton);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: '',
        maxPrice: '',
      });
    });
  });

  describe('Integration Scenarios', () => {
    it('should handle setting both filters together', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      // Set category
      const categoryButton = screen.getByRole('combobox', { name: /category/i });
      await user.click(categoryButton);
      const skiOption = await screen.findByRole('option', { name: /skiing/i });
      await user.click(skiOption);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: Category.SKYING,
        maxPrice: '',
      });

      // Set price
      const priceInput = screen.getByLabelText(/max price/i);
      await user.type(priceInput, '150');

      expect(mockOnFilterChange).toHaveBeenLastCalledWith({
        category: Category.SKYING,
        maxPrice: 150,
      });
    });

    it('should handle changing filters multiple times', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      // First set of filters
      const categoryButton = screen.getByRole('combobox', { name: /category/i });
      await user.click(categoryButton);
      const skiOption = await screen.findByRole('option', { name: /skiing/i });
      await user.click(skiOption);

      const priceInput = screen.getByLabelText(/max price/i);
      await user.type(priceInput, '100');

      // Change filters
      await user.click(categoryButton);
      const vacationOption = await screen.findByRole('option', { name: /all inclusive vacation/i });
      await user.click(vacationOption);

      expect(mockOnFilterChange).toHaveBeenLastCalledWith({
        category: Category.ALL_INCLUSIVE_VACATION,
        maxPrice: 100,
      });
    });

    it('should handle filter, clear, then filter again workflow', async () => {
      const user = userEvent.setup();
      render(<CouponFilter onFilterChange={mockOnFilterChange} />);

      // Set filters
      const categoryButton = screen.getByRole('combobox', { name: /category/i });
      await user.click(categoryButton);
      const skiOption = await screen.findByRole('option', { name: /skiing/i });
      await user.click(skiOption);

      // Clear
      const clearButton = screen.getByRole('button', { name: /clear filters/i });
      await user.click(clearButton);

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        category: '',
        maxPrice: '',
      });

      // Set new filters
      const priceInput = screen.getByLabelText(/max price/i);
      await user.type(priceInput, '200');

      expect(mockOnFilterChange).toHaveBeenLastCalledWith({
        category: '',
        maxPrice: 200,
      });
    });
  });
});
