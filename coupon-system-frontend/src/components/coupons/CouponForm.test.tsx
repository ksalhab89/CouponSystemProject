import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { axe } from 'jest-axe';
import userEvent from '@testing-library/user-event';
import { CouponForm } from './CouponForm';
import { Category } from '../../types/coupon.types';
import { createMockCoupon } from '../../../tests/mocks/factories';

describe('CouponForm', () => {
  const mockOnSubmit = vi.fn();
  const mockOnCancel = vi.fn();

  beforeEach(() => {
    mockOnSubmit.mockClear();
    mockOnCancel.mockClear();
  });

  const validFormData = {
    title: 'Test Coupon',
    description: 'This is a test description with more than 10 characters',
    CATEGORY: Category.SKYING,
    startDate: '2026-02-01',
    endDate: '2026-03-01',
    amount: 50,
    price: 99.99,
    image: 'https://example.com/image.jpg',
  };

  describe('Basic Rendering - Create Mode', () => {
    it('should render form title for create mode', () => {
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);
      expect(screen.getByText('Create New Coupon')).toBeInTheDocument();
    });

    it('should render all form fields', () => {
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/start date/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/end date/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^amount$/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^price$/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/image url/i)).toBeInTheDocument();
    });

    it('should render submit button with correct text', () => {
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);
      expect(screen.getByRole('button', { name: /create coupon/i })).toBeInTheDocument();
    });

    it('should render cancel button', () => {
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);
      expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
    });

    it('should render all category options', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const categorySelect = screen.getByLabelText(/category/i);
      await user.click(categorySelect);

      expect(await screen.findByText('Skiing')).toBeInTheDocument();
      expect(await screen.findByText('Sky Diving')).toBeInTheDocument();
      expect(await screen.findByText('Fancy Restaurant')).toBeInTheDocument();
      expect(await screen.findByText('All Inclusive Vacation')).toBeInTheDocument();
    });

    it('should have empty fields initially', () => {
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const titleInput = screen.getByLabelText(/title/i) as HTMLInputElement;
      const descriptionInput = screen.getByLabelText(/description/i) as HTMLInputElement;

      expect(titleInput.value).toBe('');
      expect(descriptionInput.value).toBe('');
    });
  });

  describe('Basic Rendering - Edit Mode', () => {
    const existingCoupon = createMockCoupon({
      id: 1,
      title: 'Existing Coupon',
      description: 'Existing description',
      CATEGORY: Category.SKY_DIVING,
      startDate: '2026-01-15',
      endDate: '2026-02-15',
      amount: 25,
      price: 149.99,
      image: 'https://example.com/existing.jpg',
    });

    it('should render form title for edit mode', () => {
      render(
        <CouponForm
          coupon={existingCoupon}
          onSubmit={mockOnSubmit}
          onCancel={mockOnCancel}
        />
      );
      expect(screen.getByText('Edit Coupon')).toBeInTheDocument();
    });

    it('should render submit button with correct text for edit mode', () => {
      render(
        <CouponForm
          coupon={existingCoupon}
          onSubmit={mockOnSubmit}
          onCancel={mockOnCancel}
        />
      );
      expect(screen.getByRole('button', { name: /update coupon/i })).toBeInTheDocument();
    });

    it('should pre-fill form with existing coupon data', () => {
      render(
        <CouponForm
          coupon={existingCoupon}
          onSubmit={mockOnSubmit}
          onCancel={mockOnCancel}
        />
      );

      const titleInput = screen.getByLabelText(/title/i) as HTMLInputElement;
      const descriptionInput = screen.getByLabelText(/description/i) as HTMLInputElement;
      const amountInput = screen.getByLabelText(/^amount$/i) as HTMLInputElement;
      const priceInput = screen.getByLabelText(/^price$/i) as HTMLInputElement;

      expect(titleInput.value).toBe('Existing Coupon');
      expect(descriptionInput.value).toBe('Existing description');
      expect(amountInput.value).toBe('25');
      expect(priceInput.value).toBe('149.99');
    });
  });

  describe('Title Field Validation', () => {
    it('should show error when title is empty on blur', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const titleInput = screen.getByLabelText(/title/i);
      await user.click(titleInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/title is required/i)).toBeInTheDocument();
      });
    });

    it('should show error when title is too short', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'AB');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/title must be at least 3 characters/i)).toBeInTheDocument();
      });
    });

    it('should clear error when valid title is entered', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const titleInput = screen.getByLabelText(/title/i);
      await user.click(titleInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/title is required/i)).toBeInTheDocument();
      });

      await user.type(titleInput, 'Valid Title');

      await waitFor(() => {
        expect(screen.queryByText(/title is required/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Description Field Validation', () => {
    it('should show error when description is empty on blur', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.click(descriptionInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/description is required/i)).toBeInTheDocument();
      });
    });

    it('should show error when description is too short', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'Short');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/description must be at least 10 characters/i)).toBeInTheDocument();
      });
    });

    it('should accept valid description', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'This is a valid description with more than 10 chars');

      expect(screen.queryByText(/description must be at least 10 characters/i)).not.toBeInTheDocument();
    });
  });

  describe('Category Field Validation', () => {
    it('should show error when category is not selected on blur', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const categorySelect = screen.getByLabelText(/category/i);
      await user.click(categorySelect);
      await user.keyboard('{Escape}');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/category is required/i)).toBeInTheDocument();
      });
    });

    it('should clear error when category is selected', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const categorySelect = screen.getByLabelText(/category/i);
      await user.click(categorySelect);
      await user.keyboard('{Escape}');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/category is required/i)).toBeInTheDocument();
      });

      await user.click(categorySelect);
      const skiOption = await screen.findByText('Skiing');
      await user.click(skiOption);

      await waitFor(() => {
        expect(screen.queryByText(/category is required/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Amount Field Validation', () => {
    it('should show error when amount is empty on blur', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const amountInput = screen.getByLabelText(/^amount$/i);
      await user.click(amountInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/amount is required/i)).toBeInTheDocument();
      });
    });

    it('should show error for negative amount', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const amountInput = screen.getByLabelText(/^amount$/i);
      await user.type(amountInput, '-5');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/amount must be a non-negative integer/i)).toBeInTheDocument();
      });
    });

    it('should accept zero amount', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const amountInput = screen.getByLabelText(/^amount$/i);
      await user.type(amountInput, '0');
      await user.tab();

      await waitFor(() => {
        expect(screen.queryByText(/amount must be a non-negative integer/i)).not.toBeInTheDocument();
      });
    });

    it('should accept positive amount', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const amountInput = screen.getByLabelText(/^amount$/i);
      await user.type(amountInput, '100');

      expect(screen.queryByText(/amount must be a non-negative integer/i)).not.toBeInTheDocument();
    });
  });

  describe('Price Field Validation', () => {
    it('should show error when price is empty on blur', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const priceInput = screen.getByLabelText(/^price$/i);
      await user.click(priceInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/price is required/i)).toBeInTheDocument();
      });
    });

    it('should show error for zero price', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const priceInput = screen.getByLabelText(/^price$/i);
      await user.type(priceInput, '0');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/price must be greater than 0/i)).toBeInTheDocument();
      });
    });

    it('should show error for negative price', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const priceInput = screen.getByLabelText(/^price$/i);
      await user.type(priceInput, '-10');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/price must be greater than 0/i)).toBeInTheDocument();
      });
    });

    it('should accept valid decimal price', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const priceInput = screen.getByLabelText(/^price$/i);
      await user.type(priceInput, '99.99');

      expect(screen.queryByText(/price must be greater than 0/i)).not.toBeInTheDocument();
    });
  });

  describe('Image URL Field Validation', () => {
    it('should show error when image URL is empty on blur', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const imageInput = screen.getByLabelText(/image url/i);
      await user.click(imageInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/image url is required/i)).toBeInTheDocument();
      });
    });

    it('should show error for invalid URL', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const imageInput = screen.getByLabelText(/image url/i);
      await user.type(imageInput, 'not-a-valid-url');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/image must be a valid url/i)).toBeInTheDocument();
      });
    });

    it('should accept valid URL', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const imageInput = screen.getByLabelText(/image url/i);
      await user.type(imageInput, 'https://example.com/image.jpg');

      expect(screen.queryByText(/image must be a valid url/i)).not.toBeInTheDocument();
    });
  });

  describe('Date Field Validation', () => {
    it('should show error when start date is empty on blur', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const startDateInput = screen.getByLabelText(/start date/i);
      await user.click(startDateInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/start date is required/i)).toBeInTheDocument();
      });
    });

    it('should show error when end date is empty on blur', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const endDateInput = screen.getByLabelText(/end date/i);
      await user.click(endDateInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/end date is required/i)).toBeInTheDocument();
      });
    });
  });

  describe('Date Range Validation', () => {
    it('should show error when end date is before start date', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const startDateInput = screen.getByLabelText(/start date/i);
      const endDateInput = screen.getByLabelText(/end date/i);

      await user.type(startDateInput, '2026-03-01');
      await user.type(endDateInput, '2026-02-01');

      const submitButton = screen.getByRole('button', { name: /create coupon/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/end date must be after start date/i)).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  describe('Form Submission', () => {
    it('should not submit with invalid form', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const submitButton = screen.getByRole('button', { name: /create coupon/i });
      await user.click(submitButton);

      // Form should not submit (validation prevents it)
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

  });

  describe('Cancel Button', () => {
    it('should call onCancel when cancel button is clicked', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      expect(mockOnCancel).toHaveBeenCalledTimes(1);
    });

    it('should not submit when cancel is clicked', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  describe('Integration Scenarios', () => {
    it('should handle form validation errors and correction flow', async () => {
      const user = userEvent.setup();
      render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      // 1. Touch title field and blur to trigger validation
      const titleInput = screen.getByLabelText(/title/i);
      await user.click(titleInput);
      await user.tab(); // Blur

      // 2. See error
      await waitFor(() => {
        expect(screen.getByText(/title is required/i)).toBeInTheDocument();
      });

      // 3. Correct error
      await user.type(titleInput, 'Valid Title');

      // 4. Error should clear
      await waitFor(() => {
        expect(screen.queryByText(/title is required/i)).not.toBeInTheDocument();
      });
    });

    it('should handle edit mode workflow', async () => {
      const existingCoupon = createMockCoupon({
        id: 1,
        title: 'Old Title',
        description: 'Old description here',
        CATEGORY: Category.SKYING,
        startDate: '2026-01-01',
        endDate: '2026-02-01',
        amount: 10,
        price: 50,
        image: 'https://old.com/image.jpg',
      });

      const user = userEvent.setup();
      render(
        <CouponForm
          coupon={existingCoupon}
          onSubmit={mockOnSubmit}
          onCancel={mockOnCancel}
        />
      );

      // 1. Verify edit mode
      expect(screen.getByText('Edit Coupon')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /update coupon/i })).toBeInTheDocument();

      // 2. Verify fields are pre-filled
      const titleInput = screen.getByLabelText(/title/i) as HTMLInputElement;
      expect(titleInput.value).toBe('Old Title');

      // 3. Update field is working
      await user.clear(titleInput);
      await user.type(titleInput, 'New Title');
      expect(titleInput.value).toBe('New Title');
    });
  });

  describe('Accessibility', () => {
    it('should have no accessibility violations', async () => {
      const { container } = render(<CouponForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });
});
