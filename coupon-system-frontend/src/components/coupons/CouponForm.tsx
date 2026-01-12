import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  FormHelperText,
  Paper,
  Typography,
  Stack,
  Grid,
  SelectChangeEvent,
} from '@mui/material';
import { Coupon } from '../../types/coupon.types';
import { getAllCategories } from '../../utils/categoryHelper';
import {
  isRequired,
  isValidPrice,
  isValidAmount,
  isValidDateRange,
  getValidationError,
} from '../../utils/validators';

interface CouponFormProps {
  coupon?: Coupon;
  onSubmit: (formData: Omit<Coupon, 'id' | 'companyID'>) => void;
  onCancel: () => void;
}

interface FormState {
  title: string;
  description: string;
  CATEGORY: number | '';
  startDate: string;
  endDate: string;
  amount: number | '';
  price: number | '';
  image: string;
}

interface FormErrors {
  title?: string;
  description?: string;
  CATEGORY?: string;
  startDate?: string;
  endDate?: string;
  amount?: string;
  price?: string;
  image?: string;
  dateRange?: string;
}

export const CouponForm: React.FC<CouponFormProps> = ({
  coupon,
  onSubmit,
  onCancel,
}) => {
  const [formData, setFormData] = useState<FormState>({
    title: coupon?.title || '',
    description: coupon?.description || '',
    CATEGORY: coupon?.CATEGORY || '',
    startDate: coupon?.startDate || '',
    endDate: coupon?.endDate || '',
    amount: coupon?.amount || '',
    price: coupon?.price || '',
    image: coupon?.image || '',
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [touched, setTouched] = useState<Set<keyof FormState>>(new Set());

  const categories = getAllCategories();
  const isEditMode = !!coupon;

  const validateField = (field: keyof FormState, value: any): string | null => {
    switch (field) {
      case 'title':
        if (!isRequired(value)) return 'Title is required';
        if (value.length < 3) return 'Title must be at least 3 characters';
        break;
      case 'description':
        if (!isRequired(value)) return 'Description is required';
        if (value.length < 10) return 'Description must be at least 10 characters';
        break;
      case 'CATEGORY':
        if (!isRequired(value)) return 'Category is required';
        break;
      case 'startDate':
        if (!isRequired(value)) return 'Start date is required';
        break;
      case 'endDate':
        if (!isRequired(value)) return 'End date is required';
        break;
      case 'amount':
        if (!isRequired(value)) return 'Amount is required';
        const amountNum = Number(value);
        if (!isValidAmount(amountNum)) {
          return 'Amount must be a non-negative integer';
        }
        break;
      case 'price':
        if (!isRequired(value)) return 'Price is required';
        const priceNum = Number(value);
        if (!isValidPrice(priceNum)) return 'Price must be greater than 0';
        break;
      case 'image':
        if (!isRequired(value)) return 'Image URL is required';
        try {
          new URL(value);
        } catch {
          return 'Image must be a valid URL';
        }
        break;
    }
    return null;
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};
    let isValid = true;

    // Validate all fields
    (Object.keys(formData) as Array<keyof FormState>).forEach((field) => {
      const error = validateField(field, formData[field]);
      if (error) {
        newErrors[field] = error;
        isValid = false;
      }
    });

    // Validate date range
    if (formData.startDate && formData.endDate) {
      if (!isValidDateRange(formData.startDate, formData.endDate)) {
        newErrors.dateRange =
          'End date must be after start date and not in the past';
        isValid = false;
      }
    }

    setErrors(newErrors);
    return isValid;
  };

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const { name, value } = event.target;
    const field = name as keyof FormState;

    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));

    // Clear error when user starts typing
    if (touched.has(field)) {
      const error = validateField(field, value);
      setErrors((prev) => ({
        ...prev,
        [field]: error || undefined,
      }));
    }
  };

  const handleSelectChange = (event: SelectChangeEvent<number>) => {
    const { name, value } = event.target;
    const field = name as keyof FormState;

    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));

    // Clear error when user makes a selection
    if (touched.has(field)) {
      const error = validateField(field, value);
      setErrors((prev) => ({
        ...prev,
        [field]: error || undefined,
      }));
    }
  };

  const handleBlur = (
    event: React.FocusEvent<HTMLInputElement | { name?: string }>
  ) => {
    const { name } = event.target as HTMLInputElement;
    const field = name as keyof FormState;

    setTouched((prev) => new Set(prev).add(field));

    const error = validateField(field, formData[field]);
    setErrors((prev) => ({
      ...prev,
      [field]: error || undefined,
    }));
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateForm()) {
      // Mark all fields as touched to show validation errors
      setTouched(new Set(Object.keys(formData) as Array<keyof FormState>));
      return;
    }

    // Format dates to ISO string if they're date inputs
    const formattedData = {
      title: formData.title,
      description: formData.description,
      CATEGORY: Number(formData.CATEGORY),
      startDate:
        typeof formData.startDate === 'string'
          ? new Date(formData.startDate).toISOString()
          : formData.startDate,
      endDate:
        typeof formData.endDate === 'string'
          ? new Date(formData.endDate).toISOString()
          : formData.endDate,
      amount: Number(formData.amount),
      price: Number(formData.price),
      image: formData.image,
    };

    onSubmit(formattedData);
  };

  return (
    <Paper
      elevation={3}
      sx={{
        p: 4,
        maxWidth: '900px',
        mx: 'auto',
        borderRadius: '12px',
      }}
    >
      <Typography
        variant="h4"
        sx={{
          mb: 3,
          fontWeight: 600,
          color: 'text.primary',
        }}
      >
        {isEditMode ? 'Edit Coupon' : 'Create New Coupon'}
      </Typography>

      <Box component="form" onSubmit={handleSubmit}>
        <Grid container spacing={3}>
          {/* Title */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.has('title') && !!errors.title}
              helperText={touched.has('title') && errors.title}
              placeholder="Enter coupon title"
              variant="outlined"
              size="small"
            />
          </Grid>

          {/* Category */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <FormControl
              fullWidth
              error={touched.has('CATEGORY') && !!errors.CATEGORY}
              size="small"
            >
              <InputLabel id="category-label">Category</InputLabel>
              <Select
                labelId="category-label"
                id="category-select"
                name="CATEGORY"
                value={formData.CATEGORY}
                onChange={handleSelectChange}
                onBlur={handleBlur}
                label="Category"
              >
                <MenuItem value="">
                  <em>Select a category</em>
                </MenuItem>
                {categories.map((cat) => (
                  <MenuItem key={cat.id} value={cat.id}>
                    {cat.name}
                  </MenuItem>
                ))}
              </Select>
              {touched.has('CATEGORY') && errors.CATEGORY && (
                <FormHelperText>{errors.CATEGORY}</FormHelperText>
              )}
            </FormControl>
          </Grid>

          {/* Description */}
          <Grid size={12}>
            <TextField
              fullWidth
              label="Description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.has('description') && !!errors.description}
              helperText={touched.has('description') && errors.description}
              placeholder="Enter coupon description"
              multiline
              rows={4}
              variant="outlined"
              size="small"
            />
          </Grid>

          {/* Start Date */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Start Date"
              name="startDate"
              type="date"
              value={formData.startDate.split('T')[0] || ''}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.has('startDate') && !!errors.startDate}
              helperText={touched.has('startDate') && errors.startDate}
              InputLabelProps={{
                shrink: true,
              }}
              variant="outlined"
              size="small"
            />
          </Grid>

          {/* End Date */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="End Date"
              name="endDate"
              type="date"
              value={formData.endDate.split('T')[0] || ''}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.has('endDate') && !!errors.endDate}
              helperText={touched.has('endDate') && errors.endDate}
              InputLabelProps={{
                shrink: true,
              }}
              variant="outlined"
              size="small"
            />
          </Grid>

          {/* Date Range Error */}
          {errors.dateRange && (
            <Grid size={12}>
              <FormHelperText error>{errors.dateRange}</FormHelperText>
            </Grid>
          )}

          {/* Amount */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Amount"
              name="amount"
              type="number"
              value={formData.amount}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.has('amount') && !!errors.amount}
              helperText={touched.has('amount') && errors.amount}
              placeholder="0"
              inputProps={{ min: 0, step: 1 }}
              variant="outlined"
              size="small"
            />
          </Grid>

          {/* Price */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Price"
              name="price"
              type="number"
              value={formData.price}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.has('price') && !!errors.price}
              helperText={touched.has('price') && errors.price}
              placeholder="0.00"
              inputProps={{ min: 0, step: 0.01 }}
              variant="outlined"
              size="small"
            />
          </Grid>

          {/* Image URL */}
          <Grid size={12}>
            <TextField
              fullWidth
              label="Image URL"
              name="image"
              value={formData.image}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.has('image') && !!errors.image}
              helperText={touched.has('image') && errors.image}
              placeholder="https://example.com/image.jpg"
              variant="outlined"
              size="small"
            />
          </Grid>

          {/* Action Buttons */}
          <Grid size={12}>
            <Stack direction="row" spacing={2} justifyContent="flex-end">
              <Button
                variant="outlined"
                color="inherit"
                onClick={onCancel}
                sx={{
                  px: 3,
                  textTransform: 'capitalize',
                  fontWeight: 500,
                }}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="contained"
                color="primary"
                sx={{
                  px: 3,
                  textTransform: 'capitalize',
                  fontWeight: 500,
                }}
              >
                {isEditMode ? 'Update Coupon' : 'Create Coupon'}
              </Button>
            </Stack>
          </Grid>
        </Grid>
      </Box>
    </Paper>
  );
};
