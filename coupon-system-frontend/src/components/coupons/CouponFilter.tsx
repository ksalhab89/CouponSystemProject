import React, { useState } from 'react';
import {
  Select,
  MenuItem,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Box,
  Grid,
  SelectChangeEvent,
} from '@mui/material';
import { getAllCategories } from '../../utils/categoryHelper';

interface FilterState {
  category: number | '';
  maxPrice: number | '';
}

interface CouponFilterProps {
  onFilterChange: (filters: { category: number | ''; maxPrice: number | '' }) => void;
}

export const CouponFilter: React.FC<CouponFilterProps> = ({ onFilterChange }) => {
  const [filters, setFilters] = useState<FilterState>({
    category: '',
    maxPrice: '',
  });

  const categories = getAllCategories();

  const handleCategoryChange = (e: SelectChangeEvent<number | ''>) => {
    const newCategory = e.target.value as number | '';
    const updatedFilters: FilterState = { ...filters, category: newCategory };
    setFilters(updatedFilters);
    onFilterChange(updatedFilters);
  };

  const handleMaxPriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    const newMaxPrice: number | '' = value === '' ? '' : parseFloat(value);
    const updatedFilters: FilterState = { ...filters, maxPrice: newMaxPrice };
    setFilters(updatedFilters);
    onFilterChange(updatedFilters);
  };

  const handleClearFilters = () => {
    const clearedFilters: FilterState = {
      category: '',
      maxPrice: '',
    };
    setFilters(clearedFilters);
    onFilterChange(clearedFilters);
  };

  return (
    <Box sx={{ mb: 3, p: 2, backgroundColor: '#f5f5f5', borderRadius: 1 }}>
      <Grid container spacing={2} alignItems="flex-end">
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <FormControl fullWidth>
            <InputLabel id="category-select-label">Category</InputLabel>
            <Select
              labelId="category-select-label"
              id="category-select"
              value={filters.category}
              label="Category"
              onChange={handleCategoryChange}
            >
              <MenuItem value="">
                <em>All Categories</em>
              </MenuItem>
              {categories.map((category) => (
                <MenuItem key={category.id} value={category.id}>
                  {category.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <TextField
            fullWidth
            label="Max Price"
            type="number"
            value={filters.maxPrice}
            onChange={handleMaxPriceChange}
            placeholder="Enter max price"
            inputProps={{ step: '0.01', min: '0' }}
          />
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Button
            fullWidth
            variant="outlined"
            color="primary"
            onClick={handleClearFilters}
            sx={{ height: '56px' }}
          >
            Clear Filters
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
};
