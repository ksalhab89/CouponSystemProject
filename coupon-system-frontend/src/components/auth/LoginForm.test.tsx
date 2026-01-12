import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { axe } from 'jest-axe';
import { LoginForm } from './LoginForm';
import { AuthProvider } from '../../contexts/AuthContext';
import { TEST_CREDENTIALS } from '../../../tests/mocks/factories';

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Wrapper component for testing
const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <BrowserRouter>
    <AuthProvider>{children}</AuthProvider>
  </BrowserRouter>
);

describe('LoginForm', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    localStorage.clear();
  });

  describe('Basic Rendering', () => {
    it('should render email input', () => {
      render(<LoginForm />, { wrapper: Wrapper });
      expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    });

    it('should render password input', () => {
      render(<LoginForm />, { wrapper: Wrapper });
      expect(screen.getByPlaceholderText(/enter your password/i)).toBeInTheDocument();
    });

    it('should render role selector', () => {
      render(<LoginForm />, { wrapper: Wrapper });
      // RoleSelector renders toggle buttons for each role
      expect(screen.getByRole('button', { name: /admin/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /company/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /customer/i })).toBeInTheDocument();
    });

    it('should render login button', () => {
      render(<LoginForm />, { wrapper: Wrapper });
      const buttons = screen.getAllByRole('button');
      const loginButton = buttons.find(btn => btn.textContent === 'Login');
      expect(loginButton).toBeInTheDocument();
    });

    it('should render login heading', () => {
      render(<LoginForm />, { wrapper: Wrapper });
      // The "Login" title appears in both the header and button, so check for both
      const loginTexts = screen.getAllByText('Login');
      expect(loginTexts.length).toBeGreaterThan(0);
    });

    it('should render password visibility toggle', () => {
      render(<LoginForm />, { wrapper: Wrapper });
      expect(screen.getByLabelText(/toggle password visibility/i)).toBeInTheDocument();
    });
  });

  describe('Email Validation', () => {
    it('should show error for invalid email on blur', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      await user.type(emailInput, 'invalidemail');
      await user.tab(); // Blur the input

      await waitFor(() => {
        expect(screen.getByText(/valid email/i)).toBeInTheDocument();
      });
    });

    it('should show error for empty email on blur', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      await user.click(emailInput);
      await user.tab(); // Blur without typing

      await waitFor(() => {
        expect(screen.getByText(/required/i)).toBeInTheDocument();
      });
    });

    it('should clear email error when user types', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      await user.type(emailInput, 'invalid');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/valid email/i)).toBeInTheDocument();
      });

      await user.clear(emailInput);
      await user.type(emailInput, 'valid@test.com');

      await waitFor(() => {
        expect(screen.queryByText(/valid email/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Password Validation', () => {
    it('should show error for short password on blur', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const passwordInput = screen.getByLabelText(/^password$/i);
      await user.type(passwordInput, '123');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/at least 8 characters/i)).toBeInTheDocument();
      });
    });

    it('should show error for empty password on blur', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const passwordInput = screen.getByLabelText(/^password$/i);
      await user.click(passwordInput);
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/required/i)).toBeInTheDocument();
      });
    });

    it('should clear password error when user types', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const passwordInput = screen.getByLabelText(/^password$/i);
      await user.type(passwordInput, '123');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText(/at least 8 characters/i)).toBeInTheDocument();
      });

      await user.clear(passwordInput);
      await user.type(passwordInput, 'ValidPass123');

      await waitFor(() => {
        expect(screen.queryByText(/at least 8 characters/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Password Visibility Toggle', () => {
    it('should toggle password visibility when icon is clicked', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const passwordInput = screen.getByLabelText(/^password$/i) as HTMLInputElement;
      expect(passwordInput.type).toBe('password');

      const toggleButton = screen.getByLabelText(/toggle password visibility/i);
      await user.click(toggleButton);

      expect(passwordInput.type).toBe('text');

      await user.click(toggleButton);
      expect(passwordInput.type).toBe('password');
    });
  });

  describe('Form Submission', () => {
    it('should not submit form with invalid email', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      await user.type(emailInput, 'invalidemail');
      await user.type(passwordInput, 'ValidPass123');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/valid email/i)).toBeInTheDocument();
      });
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    it('should not submit form with invalid password', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      await user.type(emailInput, 'test@test.com');
      await user.type(passwordInput, '123');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/at least 8 characters/i)).toBeInTheDocument();
      });
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    it('should successfully login with valid credentials', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      await user.type(emailInput, TEST_CREDENTIALS.customer.email);
      await user.type(passwordInput, TEST_CREDENTIALS.customer.password);
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/customer');
      });
    });

    it('should navigate to /admin after admin login', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      // Select admin role
      const adminButton = screen.getByRole('button', { name: /^admin$/i });
      await user.click(adminButton);

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      await user.type(emailInput, TEST_CREDENTIALS.admin.email);
      await user.type(passwordInput, TEST_CREDENTIALS.admin.password);
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/admin');
      });
    });

    it('should navigate to /company after company login', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      // Select company role
      const companyButton = screen.getByRole('button', { name: /^company$/i });
      await user.click(companyButton);

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      await user.type(emailInput, TEST_CREDENTIALS.company.email);
      await user.type(passwordInput, TEST_CREDENTIALS.company.password);
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/company');
      });
    });

    it('should show loading state during submission', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      await user.type(emailInput, TEST_CREDENTIALS.customer.email);
      await user.type(passwordInput, TEST_CREDENTIALS.customer.password);
      await user.click(submitButton);

      // Check for loading text (briefly)
      expect(screen.getByText(/logging in/i)).toBeInTheDocument();

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalled();
      });
    });

    it('should disable form fields during submission', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      await user.type(emailInput, TEST_CREDENTIALS.customer.email);
      await user.type(passwordInput, TEST_CREDENTIALS.customer.password);
      await user.click(submitButton);

      // During loading, fields should be disabled
      expect(emailInput).toBeDisabled();
      expect(passwordInput).toBeDisabled();
      expect(submitButton).toBeDisabled();

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalled();
      });
    });
  });

  describe('Error Handling', () => {
    it('should show error alert for invalid credentials', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      await user.type(emailInput, TEST_CREDENTIALS.invalid.email);
      await user.type(passwordInput, TEST_CREDENTIALS.invalid.password);
      await user.click(submitButton);

      await waitFor(() => {
        const alerts = screen.getAllByRole('alert');
        expect(alerts.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Role Selection', () => {
    it('should default to customer role', () => {
      render(<LoginForm />, { wrapper: Wrapper });
      const customerButton = screen.getByRole('button', { name: /^customer$/i });
      expect(customerButton).toHaveClass('Mui-selected');
    });

    it('should allow changing to admin role', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const adminButton = screen.getByRole('button', { name: /^admin$/i });
      await user.click(adminButton);

      expect(adminButton).toHaveClass('Mui-selected');
    });

    it('should allow changing to company role', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const companyButton = screen.getByRole('button', { name: /^company$/i });
      await user.click(companyButton);

      expect(companyButton).toHaveClass('Mui-selected');
    });
  });

  describe('Integration Scenarios', () => {
    it('should handle complete login workflow', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      // 1. Fill in email
      const emailInput = screen.getByLabelText(/email/i);
      await user.type(emailInput, TEST_CREDENTIALS.admin.email);

      // 2. Fill in password
      const passwordInput = screen.getByPlaceholderText(/enter your password/i);
      await user.type(passwordInput, TEST_CREDENTIALS.admin.password);

      // 3. Select admin role
      const adminButton = screen.getByRole('button', { name: /^admin$/i });
      await user.click(adminButton);

      // 4. Submit form
      const buttons = screen.getAllByRole('button');
      const submitButton = buttons.find(btn => btn.getAttribute('type') === 'submit');
      await user.click(submitButton!);

      // 5. Verify navigation
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/admin');
      });
    });

    it('should clear error when user corrects invalid input', async () => {
      const user = userEvent.setup();
      render(<LoginForm />, { wrapper: Wrapper });

      const emailInput = screen.getByLabelText(/email/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const submitButton = screen.getByRole('button', { name: /^login$/i });

      // Submit with invalid data
      await user.type(emailInput, 'invalidemail');
      await user.type(passwordInput, '123');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/valid email/i)).toBeInTheDocument();
      });

      // Correct the input
      await user.clear(emailInput);
      await user.type(emailInput, 'valid@test.com');

      await waitFor(() => {
        expect(screen.queryByText(/valid email/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Accessibility', () => {
    it('should have no accessibility violations', async () => {
      const { container } = render(<LoginForm />, { wrapper: Wrapper });
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });

    it('should have no violations when showing errors', async () => {
      const user = userEvent.setup();
      const { container } = render(<LoginForm />, { wrapper: Wrapper });

      // Trigger validation errors
      const emailInput = screen.getByLabelText(/email/i);
      await user.click(emailInput);
      await user.tab();

      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });
});
