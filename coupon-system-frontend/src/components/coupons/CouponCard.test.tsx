import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { axe } from 'jest-axe';
import { CouponCard } from './CouponCard';
import { Category } from '../../types/coupon.types';
import { createMockCoupon, SPECIAL_COUPONS } from '../../../tests/mocks/factories';

describe('CouponCard', () => {
  const mockCoupon = createMockCoupon({
    id: 1,
    title: 'Test Coupon',
    description: 'Test description',
    price: 99.99,
    amount: 10,
    CATEGORY: Category.SKYING,
    startDate: '2026-01-01',
    endDate: '2026-12-31',
  });

  describe('Basic Rendering', () => {
    it('should render coupon title', () => {
      render(<CouponCard coupon={mockCoupon} />);
      expect(screen.getByText('Test Coupon')).toBeInTheDocument();
    });

    it('should render coupon description', () => {
      render(<CouponCard coupon={mockCoupon} />);
      expect(screen.getByText('Test description')).toBeInTheDocument();
    });

    it('should render formatted price', () => {
      render(<CouponCard coupon={mockCoupon} />);
      expect(screen.getByText('$99.99')).toBeInTheDocument();
    });

    it('should render coupon amount', () => {
      render(<CouponCard coupon={mockCoupon} />);
      expect(screen.getByText('10')).toBeInTheDocument();
    });

    it('should render category name', () => {
      render(<CouponCard coupon={mockCoupon} />);
      expect(screen.getByText('Skiing')).toBeInTheDocument();
    });

    it('should render formatted start date', () => {
      render(<CouponCard coupon={mockCoupon} />);
      expect(screen.getByText(/Jan 01, 2026/)).toBeInTheDocument();
    });

    it('should render formatted end date', () => {
      render(<CouponCard coupon={mockCoupon} />);
      expect(screen.getByText(/Dec 31, 2026/)).toBeInTheDocument();
    });

    it('should render coupon image with correct alt text', () => {
      render(<CouponCard coupon={mockCoupon} />);
      const image = screen.getByAltText('Test Coupon');
      expect(image).toBeInTheDocument();
      expect(image).toHaveAttribute('src', mockCoupon.image);
    });
  });

  describe('Out of Stock State', () => {
    it('should show "Out of Stock" overlay when amount is 0', () => {
      const outOfStockCoupon = { ...mockCoupon, amount: 0 };
      render(<CouponCard coupon={outOfStockCoupon} />);
      expect(screen.getByText('Out of Stock')).toBeInTheDocument();
    });

    it('should not show "Out of Stock" overlay when amount is greater than 0', () => {
      render(<CouponCard coupon={mockCoupon} />);
      expect(screen.queryByText('Out of Stock')).not.toBeInTheDocument();
    });

    it('should disable purchase button when out of stock', () => {
      const outOfStockCoupon = { ...mockCoupon, amount: 0 };
      const onPurchase = vi.fn();
      render(<CouponCard coupon={outOfStockCoupon} onPurchase={onPurchase} />);

      const purchaseButton = screen.getByRole('button', { name: /purchase/i });
      expect(purchaseButton).toBeDisabled();
    });

    it('should disable edit button when out of stock', () => {
      const outOfStockCoupon = { ...mockCoupon, amount: 0 };
      const onEdit = vi.fn();
      render(<CouponCard coupon={outOfStockCoupon} onEdit={onEdit} />);

      const editButton = screen.getByRole('button', { name: /edit/i });
      expect(editButton).toBeDisabled();
    });

    it('should not disable delete button when out of stock', () => {
      const outOfStockCoupon = { ...mockCoupon, amount: 0 };
      const onDelete = vi.fn();
      render(<CouponCard coupon={outOfStockCoupon} onDelete={onDelete} />);

      const deleteButton = screen.getByRole('button', { name: /delete/i });
      expect(deleteButton).not.toBeDisabled();
    });
  });

  describe('Action Buttons', () => {
    describe('Purchase Button', () => {
      it('should render purchase button when onPurchase is provided', () => {
        const onPurchase = vi.fn();
        render(<CouponCard coupon={mockCoupon} onPurchase={onPurchase} />);
        expect(screen.getByRole('button', { name: /purchase/i })).toBeInTheDocument();
      });

      it('should not render purchase button when onPurchase is not provided', () => {
        render(<CouponCard coupon={mockCoupon} />);
        expect(screen.queryByRole('button', { name: /purchase/i })).not.toBeInTheDocument();
      });

      it('should call onPurchase when purchase button is clicked', async () => {
        const user = userEvent.setup();
        const onPurchase = vi.fn();
        render(<CouponCard coupon={mockCoupon} onPurchase={onPurchase} />);

        const purchaseButton = screen.getByRole('button', { name: /purchase/i });
        await user.click(purchaseButton);

        expect(onPurchase).toHaveBeenCalledTimes(1);
      });
    });

    describe('Edit Button', () => {
      it('should render edit button when onEdit is provided', () => {
        const onEdit = vi.fn();
        render(<CouponCard coupon={mockCoupon} onEdit={onEdit} />);
        expect(screen.getByRole('button', { name: /edit/i })).toBeInTheDocument();
      });

      it('should not render edit button when onEdit is not provided', () => {
        render(<CouponCard coupon={mockCoupon} />);
        expect(screen.queryByRole('button', { name: /edit/i })).not.toBeInTheDocument();
      });

      it('should call onEdit when edit button is clicked', async () => {
        const user = userEvent.setup();
        const onEdit = vi.fn();
        render(<CouponCard coupon={mockCoupon} onEdit={onEdit} />);

        const editButton = screen.getByRole('button', { name: /edit/i });
        await user.click(editButton);

        expect(onEdit).toHaveBeenCalledTimes(1);
      });
    });

    describe('Delete Button', () => {
      it('should render delete button when onDelete is provided', () => {
        const onDelete = vi.fn();
        render(<CouponCard coupon={mockCoupon} onDelete={onDelete} />);
        expect(screen.getByRole('button', { name: /delete/i })).toBeInTheDocument();
      });

      it('should not render delete button when onDelete is not provided', () => {
        render(<CouponCard coupon={mockCoupon} />);
        expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument();
      });

      it('should call onDelete when delete button is clicked', async () => {
        const user = userEvent.setup();
        const onDelete = vi.fn();
        render(<CouponCard coupon={mockCoupon} onDelete={onDelete} />);

        const deleteButton = screen.getByRole('button', { name: /delete/i });
        await user.click(deleteButton);

        expect(onDelete).toHaveBeenCalledTimes(1);
      });

      it('should still work when coupon is out of stock', async () => {
        const user = userEvent.setup();
        const outOfStockCoupon = { ...mockCoupon, amount: 0 };
        const onDelete = vi.fn();
        render(<CouponCard coupon={outOfStockCoupon} onDelete={onDelete} />);

        const deleteButton = screen.getByRole('button', { name: /delete/i });
        await user.click(deleteButton);

        expect(onDelete).toHaveBeenCalledTimes(1);
      });
    });

    describe('showActions Prop', () => {
      it('should render actions by default', () => {
        const onPurchase = vi.fn();
        render(<CouponCard coupon={mockCoupon} onPurchase={onPurchase} />);
        expect(screen.getByRole('button', { name: /purchase/i })).toBeInTheDocument();
      });

      it('should hide all actions when showActions is false', () => {
        const onPurchase = vi.fn();
        const onEdit = vi.fn();
        const onDelete = vi.fn();
        render(
          <CouponCard
            coupon={mockCoupon}
            onPurchase={onPurchase}
            onEdit={onEdit}
            onDelete={onDelete}
            showActions={false}
          />
        );

        expect(screen.queryByRole('button', { name: /purchase/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /edit/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument();
      });
    });
  });

  describe('Different Categories', () => {
    it('should render Skiing category', () => {
      const skiCoupon = { ...mockCoupon, CATEGORY: Category.SKYING };
      render(<CouponCard coupon={skiCoupon} />);
      expect(screen.getByText('Skiing')).toBeInTheDocument();
    });

    it('should render Sky Diving category', () => {
      const skyDivingCoupon = { ...mockCoupon, CATEGORY: Category.SKY_DIVING };
      render(<CouponCard coupon={skyDivingCoupon} />);
      expect(screen.getByText('Sky Diving')).toBeInTheDocument();
    });

    it('should render Fancy Restaurant category', () => {
      const restaurantCoupon = { ...mockCoupon, CATEGORY: Category.FANCY_RESTAURANT };
      render(<CouponCard coupon={restaurantCoupon} />);
      expect(screen.getByText('Fancy Restaurant')).toBeInTheDocument();
    });

    it('should render All Inclusive Vacation category', () => {
      const vacationCoupon = { ...mockCoupon, CATEGORY: Category.ALL_INCLUSIVE_VACATION };
      render(<CouponCard coupon={vacationCoupon} />);
      expect(screen.getByText('All Inclusive Vacation')).toBeInTheDocument();
    });
  });

  describe('Price Formatting', () => {
    it('should format integer prices with two decimals', () => {
      const coupon = { ...mockCoupon, price: 50 };
      render(<CouponCard coupon={coupon} />);
      expect(screen.getByText('$50.00')).toBeInTheDocument();
    });

    it('should format prices with one decimal', () => {
      const coupon = { ...mockCoupon, price: 99.9 };
      render(<CouponCard coupon={coupon} />);
      expect(screen.getByText('$99.90')).toBeInTheDocument();
    });

    it('should handle expensive prices', () => {
      const coupon = { ...mockCoupon, price: 999.99 };
      render(<CouponCard coupon={coupon} />);
      expect(screen.getByText('$999.99')).toBeInTheDocument();
    });

    it('should handle cheap prices', () => {
      const coupon = { ...mockCoupon, price: 9.99 };
      render(<CouponCard coupon={coupon} />);
      expect(screen.getByText('$9.99')).toBeInTheDocument();
    });
  });

  describe('Date Formatting', () => {
    it('should format dates correctly', () => {
      const coupon = {
        ...mockCoupon,
        startDate: '2026-02-14',
        endDate: '2026-03-15',
      };
      render(<CouponCard coupon={coupon} />);

      expect(screen.getByText(/Feb 14, 2026/)).toBeInTheDocument();
      expect(screen.getByText(/Mar 15, 2026/)).toBeInTheDocument();
    });

    it('should handle different year dates', () => {
      const coupon = {
        ...mockCoupon,
        startDate: '2025-01-01',
        endDate: '2027-12-31',
      };
      render(<CouponCard coupon={coupon} />);

      expect(screen.getByText(/Jan 01, 2025/)).toBeInTheDocument();
      expect(screen.getByText(/Dec 31, 2027/)).toBeInTheDocument();
    });
  });

  describe('Special Coupons', () => {
    it('should handle out of stock coupon', () => {
      render(<CouponCard coupon={SPECIAL_COUPONS.outOfStock} />);
      expect(screen.getByText('Out of Stock')).toBeInTheDocument();
    });

    it('should handle expensive coupon', () => {
      render(<CouponCard coupon={SPECIAL_COUPONS.expensive} />);
      expect(screen.getByText('$999.99')).toBeInTheDocument();
    });

    it('should handle cheap coupon', () => {
      render(<CouponCard coupon={SPECIAL_COUPONS.cheap} />);
      expect(screen.getByText('$9.99')).toBeInTheDocument();
    });
  });

  describe('Integration Scenarios', () => {
    it('should render complete card with all actions', () => {
      const onPurchase = vi.fn();
      const onEdit = vi.fn();
      const onDelete = vi.fn();

      render(
        <CouponCard
          coupon={mockCoupon}
          onPurchase={onPurchase}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      );

      expect(screen.getByText('Test Coupon')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /purchase/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /edit/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /delete/i })).toBeInTheDocument();
    });

    it('should handle customer view (purchase only)', () => {
      const onPurchase = vi.fn();

      render(<CouponCard coupon={mockCoupon} onPurchase={onPurchase} />);

      expect(screen.getByRole('button', { name: /purchase/i })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /edit/i })).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument();
    });

    it('should handle company view (edit and delete)', () => {
      const onEdit = vi.fn();
      const onDelete = vi.fn();

      render(<CouponCard coupon={mockCoupon} onEdit={onEdit} onDelete={onDelete} />);

      expect(screen.queryByRole('button', { name: /purchase/i })).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: /edit/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /delete/i })).toBeInTheDocument();
    });

    it('should handle public view (no actions)', () => {
      render(<CouponCard coupon={mockCoupon} showActions={false} />);

      expect(screen.getByText('Test Coupon')).toBeInTheDocument();
      expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have no accessibility violations', async () => {
      const { container } = render(<CouponCard coupon={mockCoupon} showActions={false} />);
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });

    it('should have no violations with actions', async () => {
      const onPurchase = vi.fn();
      const { container} = render(<CouponCard coupon={mockCoupon} onPurchase={onPurchase} />);
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });
});
