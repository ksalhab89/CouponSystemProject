import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Company } from '../../types/coupon.types';
import { isValidEmail, isValidPassword } from '../../utils/validators';

export interface CompanyFormProps {
  company?: Company;
  onSubmit: (data: { name: string; email: string; password?: string }) => void;
  onCancel: () => void;
  loading?: boolean;
}

interface FormErrors {
  name?: string;
  email?: string;
  password?: string;
}

export const CompanyForm: React.FC<CompanyFormProps> = ({
  company,
  onSubmit,
  onCancel,
  loading = false,
}) => {
  const isEditMode = !!company;

  const [formData, setFormData] = useState({
    name: company?.name || '',
    email: company?.email || '',
    password: '',
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [touched, setTouched] = useState({
    name: false,
    email: false,
    password: false,
  });

  useEffect(() => {
    if (company) {
      setFormData({
        name: company.name,
        email: company.email,
        password: '',
      });
    }
  }, [company]);

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    // Name validation
    if (!formData.name.trim()) {
      newErrors.name = 'Name is required';
    }

    // Email validation
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!isValidEmail(formData.email)) {
      newErrors.email = 'Invalid email format';
    }

    // Password validation
    if (!isEditMode) {
      // Create mode - password is required
      if (!formData.password) {
        newErrors.password = 'Password is required';
      } else if (!isValidPassword(formData.password)) {
        newErrors.password = 'Password must be at least 8 characters';
      }
    } else {
      // Edit mode - password is optional
      if (formData.password && !isValidPassword(formData.password)) {
        newErrors.password = 'Password must be at least 8 characters';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error when user starts typing
    if (errors[name as keyof FormErrors]) {
      setErrors((prev) => ({
        ...prev,
        [name]: undefined,
      }));
    }
  };

  const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    const { name } = e.target;
    setTouched((prev) => ({
      ...prev,
      [name]: true,
    }));
  };

  const handleSubmit = () => {
    if (validateForm()) {
      const submitData: any = {
        name: formData.name,
        email: formData.email,
      };

      // Only include password if it's provided
      if (formData.password) {
        submitData.password = formData.password;
      }

      onSubmit(submitData);
    }
  };

  const dialogTitle = isEditMode
    ? `Edit Company: ${company?.name}`
    : 'Add New Company';

  return (
    <Dialog
      open={true}
      onClose={onCancel}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
        },
      }}
    >
      <DialogTitle sx={{ fontSize: '1.25rem', fontWeight: 600 }}>
        {dialogTitle}
      </DialogTitle>

      <DialogContent sx={{ pt: 2 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {/* Name Field */}
          <TextField
            label="Company Name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.name && !!errors.name}
            helperText={touched.name && errors.name}
            fullWidth
            disabled={loading}
            variant="outlined"
            autoFocus
          />

          {/* Email Field */}
          <TextField
            label="Email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.email && !!errors.email}
            helperText={touched.email && errors.email}
            fullWidth
            disabled={loading}
            variant="outlined"
          />

          {/* Password Field */}
          <Box>
            <TextField
              label="Password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.password && !!errors.password}
              helperText={touched.password && errors.password}
              fullWidth
              disabled={loading}
              variant="outlined"
              placeholder={
                isEditMode ? 'Leave blank to keep current password' : ''
              }
            />
            {isEditMode && (
              <Alert
                severity="info"
                sx={{
                  mt: 1,
                  fontSize: '0.875rem',
                  py: 1,
                  '& .MuiAlert-icon': {
                    fontSize: '1rem',
                  },
                }}
              >
                Leave blank to keep current password
              </Alert>
            )}
            {!isEditMode && (
              <Alert
                severity="info"
                sx={{
                  mt: 1,
                  fontSize: '0.875rem',
                  py: 1,
                  '& .MuiAlert-icon': {
                    fontSize: '1rem',
                  },
                }}
              >
                Password must be at least 8 characters
              </Alert>
            )}
          </Box>
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button
          onClick={onCancel}
          disabled={loading}
          sx={{
            textTransform: 'none',
            fontWeight: 500,
          }}
        >
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          color="primary"
          disabled={loading}
          sx={{
            textTransform: 'none',
            fontWeight: 500,
            minWidth: '100px',
          }}
        >
          {loading ? <CircularProgress size={24} /> : isEditMode ? 'Update' : 'Create'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
