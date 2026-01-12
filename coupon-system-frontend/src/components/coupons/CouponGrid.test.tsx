import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CouponGrid } from './CouponGrid';
import { createMockCoupon } from '../../../tests/mocks/factories';
import { Category } from '../../types/coupon.types';

describe('CouponGrid', () => {
  const mockOnPurchase = vi.fn();
  const mockOnEdit = vi.fn();
  const mockOnDelete = vi.fn();

  beforeEach(() => {
    mockOnPurchase.mockClear();
    mockOnEdit.mockClear();
    mockOnDelete.mockClear();
  });

  const mockCoupons = [
    createMockCoupon({
      id: 1,
      title: 'Ski Pass',
      CATEGORY: Category.SKYING,
      price: 100,
      amount: 10,
    }),
    createMockCoupon({
      id: 2,
      title: 'Sky Diving Adventure',
      CATEGORY: Category.SKY_DIVING,
      price: 200,
      amount: 5,
    }),
    createMockCoupon({
      id: 3,
      title: 'Restaurant Dinner',
      CATEGORY: Category.FANCY_RESTAURANT,
      price: 75,
      amount: 20,
    }),
  ];

  describe('Loading State', () => {
    it('should show loading spinner when loading', () => {
      render(
        <CouponGrid
          coupons={[]}
          loading={true}
          showActions={false}
        />
      );

      expect(screen.getByText(/loading coupons/i)).toBeInTheDocument();
    });

    it('should not show coupons when loading', () => {
      render(
        <CouponGrid
          coupons={mockCoupons}
          loading={true}
          showActions={false}
        />
      );

      expect(screen.queryByText('Ski Pass')).not.toBeInTheDocument();
    });

    it('should not show empty state when loading', () => {
      render(
        <CouponGrid
          coupons={[]}
          loading={true}
          showActions={false}
        />
      );

      expect(screen.queryByText(/no coupons found/i)).not.toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should show empty message when no coupons', () => {
      render(
        <CouponGrid
          coupons={[]}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.getByText(/no coupons found/i)).toBeInTheDocument();
    });

    it('should not show loading spinner in empty state', () => {
      render(
        <CouponGrid
          coupons={[]}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.queryByText(/loading coupons/i)).not.toBeInTheDocument();
    });
  });

  describe('Rendering Coupons', () => {
    it('should render all coupons', () => {
      render(
        <CouponGrid
          coupons={mockCoupons}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.getByText('Ski Pass')).toBeInTheDocument();
      expect(screen.getByText('Sky Diving Adventure')).toBeInTheDocument();
      expect(screen.getByText('Restaurant Dinner')).toBeInTheDocument();
    });

    it('should render single coupon', () => {
      render(
        <CouponGrid
          coupons={[mockCoupons[0]]}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.getByText('Ski Pass')).toBeInTheDocument();
      expect(screen.queryByText('Sky Diving Adventure')).not.toBeInTheDocument();
    });

    it('should render coupon prices', () => {
      render(
        <CouponGrid
          coupons={mockCoupons}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.getByText('$100.00')).toBeInTheDocument();
      expect(screen.getByText('$200.00')).toBeInTheDocument();
      expect(screen.getByText('$75.00')).toBeInTheDocument();
    });

    it('should render coupon amounts', () => {
      render(
        <CouponGrid
          coupons={mockCoupons}
          loading={false}
          showActions={false}
        />
      );

      // Check for "Available" label
      const availableLabels = screen.getAllByText('Available');
      expect(availableLabels).toHaveLength(3);

      // Check for the amount numbers (in h6 elements)
      expect(screen.getByRole('heading', { name: '10', level: 6 })).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: '5', level: 6 })).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: '20', level: 6 })).toBeInTheDocument();
    });
  });

  describe('Action Handlers', () => {
    it('should call onPurchase when purchase button clicked', async () => {
      const user = userEvent.setup();
      render(
        <CouponGrid
          coupons={[mockCoupons[0]]}
          loading={false}
          showActions={true}
          onPurchase={mockOnPurchase}
        />
      );

      const purchaseButton = screen.getByRole('button', { name: /purchase/i });
      await user.click(purchaseButton);

      expect(mockOnPurchase).toHaveBeenCalledTimes(1);
      expect(mockOnPurchase).toHaveBeenCalledWith(mockCoupons[0]);
    });

    it('should call onEdit when edit button clicked', async () => {
      const user = userEvent.setup();
      render(
        <CouponGrid
          coupons={[mockCoupons[0]]}
          loading={false}
          showActions={true}
          onEdit={mockOnEdit}
        />
      );

      const editButton = screen.getByRole('button', { name: /edit/i });
      await user.click(editButton);

      expect(mockOnEdit).toHaveBeenCalledTimes(1);
      expect(mockOnEdit).toHaveBeenCalledWith(mockCoupons[0]);
    });

    it('should call onDelete when delete button clicked', async () => {
      const user = userEvent.setup();
      render(
        <CouponGrid
          coupons={[mockCoupons[0]]}
          loading={false}
          showActions={true}
          onDelete={mockOnDelete}
        />
      );

      const deleteButton = screen.getByRole('button', { name: /delete/i });
      await user.click(deleteButton);

      expect(mockOnDelete).toHaveBeenCalledTimes(1);
      expect(mockOnDelete).toHaveBeenCalledWith(mockCoupons[0].id);
    });

    it('should not show actions when showActions is false', () => {
      render(
        <CouponGrid
          coupons={[mockCoupons[0]]}
          loading={false}
          showActions={false}
          onPurchase={mockOnPurchase}
        />
      );

      expect(screen.queryByRole('button', { name: /purchase/i })).not.toBeInTheDocument();
    });

    it('should not show purchase button when onPurchase not provided', () => {
      render(
        <CouponGrid
          coupons={[mockCoupons[0]]}
          loading={false}
          showActions={true}
        />
      );

      expect(screen.queryByRole('button', { name: /purchase/i })).not.toBeInTheDocument();
    });

    it('should not show edit button when onEdit not provided', () => {
      render(
        <CouponGrid
          coupons={[mockCoupons[0]]}
          loading={false}
          showActions={true}
        />
      );

      expect(screen.queryByRole('button', { name: /edit/i })).not.toBeInTheDocument();
    });

    it('should not show delete button when onDelete not provided', () => {
      render(
        <CouponGrid
          coupons={[mockCoupons[0]]}
          loading={false}
          showActions={true}
        />
      );

      expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument();
    });
  });

  describe('Multiple Coupons', () => {
    it('should handle multiple coupon interactions independently', async () => {
      const user = userEvent.setup();
      render(
        <CouponGrid
          coupons={mockCoupons}
          loading={false}
          showActions={true}
          onPurchase={mockOnPurchase}
        />
      );

      const purchaseButtons = screen.getAllByRole('button', { name: /purchase/i });

      // Click first coupon's purchase button
      await user.click(purchaseButtons[0]);
      expect(mockOnPurchase).toHaveBeenCalledWith(mockCoupons[0]);

      // Click second coupon's purchase button
      await user.click(purchaseButtons[1]);
      expect(mockOnPurchase).toHaveBeenCalledWith(mockCoupons[1]);

      expect(mockOnPurchase).toHaveBeenCalledTimes(2);
    });

    it('should render out-of-stock coupons correctly', () => {
      const outOfStockCoupon = createMockCoupon({
        id: 4,
        title: 'Sold Out Coupon',
        amount: 0,
      });

      render(
        <CouponGrid
          coupons={[outOfStockCoupon]}
          loading={false}
          showActions={true}
          onPurchase={mockOnPurchase}
        />
      );

      expect(screen.getByText(/sold out coupon/i)).toBeInTheDocument();
      expect(screen.getByText(/out of stock/i)).toBeInTheDocument();
    });

    it('should render mix of available and out-of-stock coupons', () => {
      const mixedCoupons = [
        mockCoupons[0], // Available
        createMockCoupon({ id: 4, title: 'Out of Stock Coupon', amount: 0 }), // Out of stock
        mockCoupons[1], // Available
      ];

      render(
        <CouponGrid
          coupons={mixedCoupons}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.getByText('Ski Pass')).toBeInTheDocument();
      expect(screen.getByText('Out of Stock Coupon')).toBeInTheDocument();
      expect(screen.getByText('Sky Diving Adventure')).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: '10', level: 6 })).toBeInTheDocument();
    });
  });

  describe('Integration', () => {
    it('should display all coupon information correctly', () => {
      const detailedCoupon = createMockCoupon({
        id: 5,
        title: 'Luxury Vacation',
        description: 'An amazing vacation package',
        CATEGORY: Category.ALL_INCLUSIVE_VACATION,
        price: 1500,
        amount: 3,
      });

      render(
        <CouponGrid
          coupons={[detailedCoupon]}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.getByText('Luxury Vacation')).toBeInTheDocument();
      expect(screen.getByText('$1500.00')).toBeInTheDocument();
      expect(screen.getByText('Available')).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: '3', level: 6 })).toBeInTheDocument();
      expect(screen.getByText('All Inclusive Vacation')).toBeInTheDocument();
    });

    it('should transition from loading to showing coupons', () => {
      const { rerender } = render(
        <CouponGrid
          coupons={[]}
          loading={true}
          showActions={false}
        />
      );

      expect(screen.getByText(/loading coupons/i)).toBeInTheDocument();

      rerender(
        <CouponGrid
          coupons={mockCoupons}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.queryByText(/loading coupons/i)).not.toBeInTheDocument();
      expect(screen.getByText('Ski Pass')).toBeInTheDocument();
    });

    it('should transition from loading to empty state', () => {
      const { rerender } = render(
        <CouponGrid
          coupons={[]}
          loading={true}
          showActions={false}
        />
      );

      expect(screen.getByText(/loading coupons/i)).toBeInTheDocument();

      rerender(
        <CouponGrid
          coupons={[]}
          loading={false}
          showActions={false}
        />
      );

      expect(screen.queryByText(/loading coupons/i)).not.toBeInTheDocument();
      expect(screen.getByText(/no coupons found/i)).toBeInTheDocument();
    });
  });
});
