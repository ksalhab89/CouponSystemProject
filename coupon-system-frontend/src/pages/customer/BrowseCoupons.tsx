import React, { useState, useEffect } from 'react';
import {
  Container,
  Card,
  CardContent,
  CardActions,
  CardMedia,
  Button,
  TextField,
  Box,
  Typography,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Paper,
  Alert,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { customerApi } from '../../api/customerApi';
import { Coupon, Category } from '../../types/coupon.types';

const categoryLabels: { [key: number]: string } = {
  [Category.SKYING]: 'Skiing',
  [Category.SKY_DIVING]: 'Sky Diving',
  [Category.FANCY_RESTAURANT]: 'Fancy Restaurant',
  [Category.ALL_INCLUSIVE_VACATION]: 'All Inclusive Vacation',
};

const BrowseCoupons: React.FC = () => {
  // State management
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [filteredCoupons, setFilteredCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [purchasing, setPurchasing] = useState<number | null>(null);

  // Filter state
  const [filters, setFilters] = useState({
    category: '',
    maxPrice: '',
    searchTitle: '',
  });

  // Alert state
  const [alert, setAlert] = useState<{
    open: boolean;
    message: string;
    severity: 'error' | 'warning' | 'info' | 'success';
  }>({
    open: false,
    message: '',
    severity: 'info',
  });

  // Fetch available coupons on mount
  useEffect(() => {
    fetchAvailableCoupons();
  }, []);

  // Apply filters whenever filter state or coupons change
  useEffect(() => {
    applyFilters();
  }, [coupons, filters]);

  const fetchAvailableCoupons = async () => {
    try {
      setLoading(true);
      const data = await customerApi.getAvailableCoupons();
      setCoupons(data);
    } catch (error: any) {
      const errorMessage =
        error.response?.data?.message || 'Failed to load available coupons';
      setAlert({
        open: true,
        message: errorMessage,
        severity: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...coupons];

    // Filter by search title
    if (filters.searchTitle.trim()) {
      filtered = filtered.filter((coupon) =>
        coupon.title.toLowerCase().includes(filters.searchTitle.toLowerCase())
      );
    }

    // Filter by category
    if (filters.category) {
      filtered = filtered.filter(
        (coupon) => coupon.CATEGORY === parseInt(filters.category)
      );
    }

    // Filter by max price
    if (filters.maxPrice) {
      filtered = filtered.filter(
        (coupon) => coupon.price <= parseFloat(filters.maxPrice)
      );
    }

    setFilteredCoupons(filtered);
  };

  const handlePurchase = async (couponId: number) => {
    try {
      setPurchasing(couponId);
      await customerApi.purchaseCoupon(couponId);

      setAlert({
        open: true,
        message: 'Coupon purchased successfully!',
        severity: 'success',
      });

      // Refresh the available coupons
      await fetchAvailableCoupons();
    } catch (error: any) {
      const errorMessage =
        error.response?.data?.message || 'Failed to purchase coupon';
      setAlert({
        open: true,
        message: errorMessage,
        severity: 'error',
      });
    } finally {
      setPurchasing(null);
    }
  };

  const handleFilterChange = (field: string, value: any) => {
    setFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleResetFilters = () => {
    setFilters({
      category: '',
      maxPrice: '',
      searchTitle: '',
    });
  };

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5',
      }}
    >
      {/* Main Content */}
      <Container maxWidth="lg" sx={{ py: 4, flex: 1 }}>
        {/* Page Title */}
        <Typography
          variant="h4"
          component="h1"
          sx={{
            mb: 4,
            fontWeight: 'bold',
            color: '#333',
          }}
        >
          Browse Coupons to Purchase
        </Typography>

        {/* Alert Messages */}
        <ErrorAlert
          message={alert.message}
          severity={alert.severity}
          open={alert.open}
          onClose={() => setAlert({ ...alert, open: false })}
        />

        {/* Filter Section */}
        <Paper
          elevation={1}
          sx={{
            p: 3,
            mb: 4,
            backgroundColor: '#fff',
          }}
        >
          <Typography variant="h6" sx={{ mb: 3, fontWeight: 'bold' }}>
            Filter Coupons
          </Typography>

          <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 2 }}>
            {/* Search by Title */}
            <TextField
              fullWidth
              label="Search by Title"
              variant="outlined"
              size="small"
              value={filters.searchTitle}
              onChange={(e) =>
                handleFilterChange('searchTitle', e.target.value)
              }
              placeholder="Enter coupon title..."
            />

            {/* Filter by Category */}
            <FormControl fullWidth size="small">
              <InputLabel>Category</InputLabel>
              <Select
                value={filters.category}
                label="Category"
                onChange={(e) =>
                  handleFilterChange('category', e.target.value)
                }
              >
                <MenuItem value="">All Categories</MenuItem>
                {Object.entries(Category).map(([key, value]) => (
                  <MenuItem key={key} value={value.toString()}>
                    {categoryLabels[value as number]}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Filter by Max Price */}
            <TextField
              fullWidth
              label="Max Price"
              variant="outlined"
              size="small"
              type="number"
              inputProps={{ step: '0.01', min: '0' }}
              value={filters.maxPrice}
              onChange={(e) =>
                handleFilterChange('maxPrice', e.target.value)
              }
              placeholder="e.g., 100"
            />

            {/* Reset Button */}
            <Button
              fullWidth
              variant="outlined"
              onClick={handleResetFilters}
              sx={{ height: '40px' }}
            >
              Reset Filters
            </Button>
          </Box>
        </Paper>

        {/* Loading State */}
        {loading ? (
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              minHeight: '300px',
            }}
          >
            <CircularProgress />
          </Box>
        ) : filteredCoupons.length === 0 ? (
          <Alert severity="info" sx={{ mb: 4 }}>
            {coupons.length === 0
              ? 'No available coupons to purchase at the moment.'
              : 'No coupons match your filters. Try adjusting your search criteria.'}
          </Alert>
        ) : (
          <>
            {/* Results Count */}
            <Typography variant="body2" sx={{ mb: 3, color: '#666' }}>
              Showing {filteredCoupons.length} of {coupons.length} available
              coupons
            </Typography>

            {/* Coupon Grid */}
            <Box
              sx={{
                display: 'grid',
                gridTemplateColumns: {
                  xs: '1fr',
                  sm: 'repeat(2, 1fr)',
                  md: 'repeat(3, 1fr)',
                  lg: 'repeat(4, 1fr)',
                },
                gap: 3,
              }}
            >
              {filteredCoupons.map((coupon) => (
                <Box key={coupon.id} data-testid="coupon-card">
                  <Card
                    sx={{
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      boxShadow: 1,
                      '&:hover': {
                        boxShadow: 4,
                        transform: 'translateY(-4px)',
                      },
                      transition:
                        'box-shadow 0.3s ease, transform 0.3s ease',
                    }}
                  >
                    {/* Coupon Image */}
                    {coupon.image && (
                      <CardMedia
                        component="img"
                        height="200"
                        image={coupon.image}
                        alt={coupon.title}
                        sx={{ objectFit: 'cover' }}
                      />
                    )}

                    {/* Coupon Details */}
                    <CardContent sx={{ flexGrow: 1 }}>
                      <Typography
                        variant="h6"
                        component="div"
                        sx={{
                          mb: 1,
                          fontWeight: 'bold',
                          color: '#333',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                        title={coupon.title}
                      >
                        {coupon.title}
                      </Typography>

                      <Typography
                        variant="body2"
                        color="textSecondary"
                        sx={{
                          mb: 2,
                          display: '-webkit-box',
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: 'vertical',
                          overflow: 'hidden',
                        }}
                      >
                        {coupon.description}
                      </Typography>

                      {/* Category Badge */}
                      <Typography
                        variant="caption"
                        sx={{
                          display: 'inline-block',
                          backgroundColor: '#e3f2fd',
                          color: '#1976d2',
                          px: 1.5,
                          py: 0.5,
                          borderRadius: 1,
                          mb: 1,
                          fontWeight: 'medium',
                        }}
                      >
                        {categoryLabels[coupon.CATEGORY] || 'Unknown'}
                      </Typography>

                      {/* Price and Amount */}
                      <Box sx={{ mt: 2 }}>
                        <Typography
                          variant="h5"
                          sx={{
                            color: '#d32f2f',
                            fontWeight: 'bold',
                            mb: 0.5,
                          }}
                        >
                          ${coupon.price.toFixed(2)}
                        </Typography>
                        <Typography variant="caption" color="textSecondary">
                          {coupon.amount} available
                        </Typography>
                      </Box>

                      {/* Validity Period */}
                      <Typography
                        variant="caption"
                        sx={{
                          display: 'block',
                          mt: 1.5,
                          color: '#666',
                        }}
                      >
                        Valid: {new Date(coupon.startDate).toLocaleDateString()} -{' '}
                        {new Date(coupon.endDate).toLocaleDateString()}
                      </Typography>
                    </CardContent>

                    {/* Purchase Button */}
                    <CardActions>
                      <Button
                        fullWidth
                        variant="contained"
                        color="primary"
                        startIcon={<ShoppingCartIcon />}
                        onClick={() => handlePurchase(coupon.id)}
                        disabled={
                          purchasing === coupon.id ||
                          coupon.amount <= 0
                        }
                        sx={{
                          py: 1,
                        }}
                      >
                        {purchasing === coupon.id ? (
                          <>
                            <CircularProgress size={20} sx={{ mr: 1 }} />
                            Purchasing...
                          </>
                        ) : coupon.amount <= 0 ? (
                          'Out of Stock'
                        ) : (
                          'Purchase'
                        )}
                      </Button>
                    </CardActions>
                  </Card>
                </Box>
              ))}
            </Box>
          </>
        )}
      </Container>

      {/* Footer */}
      <Box
        component="footer"
        sx={{
          backgroundColor: '#333',
          color: '#fff',
          py: 3,
          mt: 4,
          textAlign: 'center',
        }}
      >
        <Container maxWidth="lg">
          <Typography variant="body2">
            &copy; 2026 Coupon System. All rights reserved.
          </Typography>
        </Container>
      </Box>
    </div>
  );
};

export default BrowseCoupons;
