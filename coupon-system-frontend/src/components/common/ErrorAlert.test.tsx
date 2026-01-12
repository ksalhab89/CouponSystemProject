import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ErrorAlert } from './ErrorAlert';

describe('ErrorAlert', () => {
  const mockOnClose = vi.fn();

  beforeEach(() => {
    mockOnClose.mockClear();
  });

  describe('Basic Rendering', () => {
    it('should render when open is true', () => {
      render(<ErrorAlert message="Test error" severity="error" open={true} onClose={mockOnClose} />);
      expect(screen.getByText('Test error')).toBeInTheDocument();
    });

    it('should not render when open is false', () => {
      render(<ErrorAlert message="Test error" severity="error" open={false} onClose={mockOnClose} />);
      expect(screen.queryByText('Test error')).not.toBeInTheDocument();
    });

    it('should render the message', () => {
      render(<ErrorAlert message="Something went wrong" severity="error" open={true} onClose={mockOnClose} />);
      expect(screen.getByText('Something went wrong')).toBeInTheDocument();
    });
  });

  describe('Severity Types', () => {
    it('should render with error severity', () => {
      const { container } = render(<ErrorAlert message="Error" severity="error" open={true} onClose={mockOnClose} />);
      const alert = container.querySelector('.MuiAlert-standardError');
      expect(alert).toBeInTheDocument();
    });

    it('should render with warning severity', () => {
      const { container } = render(<ErrorAlert message="Warning" severity="warning" open={true} onClose={mockOnClose} />);
      const alert = container.querySelector('.MuiAlert-standardWarning');
      expect(alert).toBeInTheDocument();
    });

    it('should render with info severity', () => {
      const { container } = render(<ErrorAlert message="Info" severity="info" open={true} onClose={mockOnClose} />);
      const alert = container.querySelector('.MuiAlert-standardInfo');
      expect(alert).toBeInTheDocument();
    });

    it('should render with success severity', () => {
      const { container } = render(<ErrorAlert message="Success" severity="success" open={true} onClose={mockOnClose} />);
      const alert = container.querySelector('.MuiAlert-standardSuccess');
      expect(alert).toBeInTheDocument();
    });
  });

  describe('Close Behavior', () => {
    it('should have a close button when open', () => {
      render(<ErrorAlert message="Test" severity="info" open={true} onClose={mockOnClose} />);
      const closeButton = screen.getByRole('button', { name: /close/i });
      expect(closeButton).toBeInTheDocument();
    });
  });

  describe('Different Messages', () => {
    it('should render short message', () => {
      render(<ErrorAlert message="Error" severity="error" open={true} onClose={mockOnClose} />);
      expect(screen.getByText('Error')).toBeInTheDocument();
    });

    it('should render long message', () => {
      const longMessage = 'This is a very long error message that explains in detail what went wrong and how to fix it.';
      render(<ErrorAlert message={longMessage} severity="error" open={true} onClose={mockOnClose} />);
      expect(screen.getByText(longMessage)).toBeInTheDocument();
    });

    it('should render message with special characters', () => {
      render(<ErrorAlert message="Error: 404 - Not Found!" severity="error" open={true} onClose={mockOnClose} />);
      expect(screen.getByText(/Error: 404/)).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have alert role for screen readers', () => {
      render(<ErrorAlert message="Test" severity="info" open={true} onClose={mockOnClose} />);
      const alert = screen.getByRole('alert');
      expect(alert).toBeInTheDocument();
    });

    // Note: MUI Snackbar/Alert components have their own accessibility testing
    // Framework-level accessibility is tested by Material-UI team
  });
});
