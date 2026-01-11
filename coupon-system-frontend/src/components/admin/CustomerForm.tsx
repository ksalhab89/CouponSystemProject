import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Stack,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Customer } from '../../types/coupon.types';
import { CustomerCreateRequest, CustomerUpdateRequest } from '../../types/api.types';
import {
  isValidEmail,
  isValidPassword,
  isRequired,
  getValidationError,
} from '../../utils/validators';

interface CustomerFormProps {
  customer?: Customer; // Optional - if provided, form is in edit mode
  onSubmit: (data: CustomerCreateRequest | CustomerUpdateRequest) => Promise<void>;
  onCancel: () => void;
  open?: boolean;
  title?: string;
  isLoading?: boolean;
}

interface FormData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

interface FormErrors {
  firstName?: string;
  lastName?: string;
  email?: string;
  password?: string;
}

const CustomerForm: React.FC<CustomerFormProps> = ({
  customer,
  onSubmit,
  onCancel,
  open = true,
  title,
  isLoading = false,
}) => {
  const [formData, setFormData] = useState<FormData>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isEditMode = !!customer;
  const defaultTitle = isEditMode ? 'Edit Customer' : 'Create Customer';

  // Initialize form with customer data if in edit mode
  useEffect(() => {
    if (customer) {
      setFormData({
        firstName: customer.firstName,
        lastName: customer.lastName,
        email: customer.email,
        password: '',
      });
    } else {
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
      });
    }
    setErrors({});
    setTouched({});
    setSubmitError(null);
  }, [customer, open]);

  // Validate individual field
  const validateField = (name: string, value: string): string | undefined => {
    switch (name) {
      case 'firstName':
      case 'lastName':
        if (!isRequired(value)) {
          return `${name === 'firstName' ? 'First' : 'Last'} name is required`;
        }
        if (value.length < 2) {
          return `${name === 'firstName' ? 'First' : 'Last'} name must be at least 2 characters`;
        }
        if (value.length > 50) {
          return `${name === 'firstName' ? 'First' : 'Last'} name must not exceed 50 characters`;
        }
        break;

      case 'email':
        if (!isRequired(value)) {
          return 'Email is required';
        }
        if (!isValidEmail(value)) {
          return 'Invalid email format';
        }
        break;

      case 'password':
        // Password is required only in create mode
        if (!isEditMode && !isRequired(value)) {
          return 'Password is required';
        }
        // If password is provided (in edit mode), validate it
        if (isEditMode && value && !isValidPassword(value)) {
          return 'Password must be at least 8 characters';
        }
        // In create mode, validate password
        if (!isEditMode && !isValidPassword(value)) {
          return 'Password must be at least 8 characters';
        }
        break;

      default:
        break;
    }
    return undefined;
  };

  // Handle field change
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear submit error on field change
    if (submitError) {
      setSubmitError(null);
    }

    // Validate field if it has been touched
    if (touched[name]) {
      const error = validateField(name, value);
      setErrors((prev) => ({
        ...prev,
        [name]: error,
      }));
    }
  };

  // Handle field blur
  const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setTouched((prev) => ({
      ...prev,
      [name]: true,
    }));

    const error = validateField(name, value);
    setErrors((prev) => ({
      ...prev,
      [name]: error,
    }));
  };

  // Validate entire form
  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    Object.keys(formData).forEach((key) => {
      // Skip password validation in edit mode if empty
      if (isEditMode && key === 'password' && !formData[key as keyof FormData]) {
        return;
      }

      const error = validateField(key, formData[key as keyof FormData]);
      if (error) {
        newErrors[key as keyof FormErrors] = error;
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // Mark all fields as touched
    setTouched({
      firstName: true,
      lastName: true,
      email: true,
      password: true,
    });

    // Validate form
    if (!validateForm()) {
      return;
    }

    try {
      setIsSubmitting(true);
      setSubmitError(null);

      let submitData: CustomerCreateRequest | CustomerUpdateRequest;

      if (isEditMode) {
        // Edit mode: password is optional
        const updateData: CustomerUpdateRequest = {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
        };

        // Only include password if it's not empty
        if (formData.password) {
          updateData.password = formData.password;
        }

        submitData = updateData;
      } else {
        // Create mode: all fields required
        submitData = {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          password: formData.password,
        };
      }

      await onSubmit(submitData);
    } catch (error) {
      setSubmitError(
        error instanceof Error ? error.message : 'An error occurred while submitting the form'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onClose={onCancel} maxWidth="sm" fullWidth>
      <DialogTitle>{title || defaultTitle}</DialogTitle>

      <form onSubmit={handleSubmit}>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            {/* Submit Error Alert */}
            {submitError && <Alert severity="error">{submitError}</Alert>}

            {/* First Name Field */}
            <TextField
              fullWidth
              label="First Name"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.firstName && !!errors.firstName}
              helperText={touched.firstName && errors.firstName}
              disabled={isSubmitting || isLoading}
              placeholder="Enter customer first name"
              variant="outlined"
            />

            {/* Last Name Field */}
            <TextField
              fullWidth
              label="Last Name"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.lastName && !!errors.lastName}
              helperText={touched.lastName && errors.lastName}
              disabled={isSubmitting || isLoading}
              placeholder="Enter customer last name"
              variant="outlined"
            />

            {/* Email Field */}
            <TextField
              fullWidth
              label="Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.email && !!errors.email}
              helperText={touched.email && errors.email}
              disabled={isSubmitting || isLoading}
              placeholder="Enter email address"
              variant="outlined"
            />

            {/* Password Field */}
            <TextField
              fullWidth
              label={isEditMode ? 'Password (leave empty to keep current)' : 'Password'}
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.password && !!errors.password}
              helperText={
                touched.password && errors.password
                  ? errors.password
                  : isEditMode
                  ? 'Leave empty to keep current password'
                  : 'Minimum 8 characters'
              }
              disabled={isSubmitting || isLoading}
              placeholder={isEditMode ? 'Leave empty to keep current password' : 'Enter password'}
              variant="outlined"
            />

            {/* Form Info Message */}
            {isEditMode && (
              <Typography variant="caption" sx={{ color: 'text.secondary', mt: 1 }}>
                Password is optional. If you want to change the password, enter a new one (minimum 8
                characters).
              </Typography>
            )}
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2, gap: 1 }}>
          <Button onClick={onCancel} disabled={isSubmitting || isLoading}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={isSubmitting || isLoading}
            sx={{ minWidth: 100 }}
          >
            {isSubmitting || isLoading ? (
              <Stack direction="row" alignItems="center" gap={1}>
                <CircularProgress size={20} />
                {isEditMode ? 'Updating' : 'Creating'}
              </Stack>
            ) : isEditMode ? (
              'Update'
            ) : (
              'Create'
            )}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default CustomerForm;
