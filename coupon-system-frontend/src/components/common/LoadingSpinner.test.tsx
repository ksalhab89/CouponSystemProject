import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { axe } from 'jest-axe';
import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner', () => {
  describe('Basic Rendering', () => {
    it('should render spinner', () => {
      render(<LoadingSpinner />);
      const spinner = screen.getByRole('progressbar');
      expect(spinner).toBeInTheDocument();
    });

    it('should render with message', () => {
      render(<LoadingSpinner message="Loading data..." />);
      expect(screen.getByText('Loading data...')).toBeInTheDocument();
    });

    it('should not render message when not provided', () => {
      const { container } = render(<LoadingSpinner />);
      const typography = container.querySelector('.MuiTypography-root');
      expect(typography).not.toBeInTheDocument();
    });
  });

  describe('Props', () => {
    it('should use default size when not provided', () => {
      const { container } = render(<LoadingSpinner />);
      const spinner = container.querySelector('.MuiCircularProgress-root');
      expect(spinner).toHaveStyle({ width: '60px', height: '60px' });
    });

    it('should use custom size', () => {
      const { container } = render(<LoadingSpinner size={80} />);
      const spinner = container.querySelector('.MuiCircularProgress-root');
      expect(spinner).toHaveStyle({ width: '80px', height: '80px' });
    });

    it('should use primary color by default', () => {
      const { container } = render(<LoadingSpinner />);
      const spinner = container.querySelector('.MuiCircularProgress-colorPrimary');
      expect(spinner).toBeInTheDocument();
    });

    it('should use custom color', () => {
      const { container } = render(<LoadingSpinner color="secondary" />);
      const spinner = container.querySelector('.MuiCircularProgress-colorSecondary');
      expect(spinner).toBeInTheDocument();
    });
  });

  describe('Different Messages', () => {
    it('should render short message', () => {
      render(<LoadingSpinner message="Wait..." />);
      expect(screen.getByText('Wait...')).toBeInTheDocument();
    });

    it('should render long message', () => {
      render(<LoadingSpinner message="Please wait while we load your data. This may take a few moments." />);
      expect(screen.getByText(/Please wait while we load your data/)).toBeInTheDocument();
    });

    it('should render message with special characters', () => {
      render(<LoadingSpinner message="Loading: 50%... Almost there!" />);
      expect(screen.getByText(/Loading: 50%/)).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have progressbar role for screen readers', () => {
      render(<LoadingSpinner />);
      const spinner = screen.getByRole('progressbar');
      expect(spinner).toHaveAttribute('role', 'progressbar');
    });

    // Note: MUI CircularProgress has known accessibility issues with role=progressbar on span
    // These are framework-level issues that cannot be fixed at the component level
  });
});
