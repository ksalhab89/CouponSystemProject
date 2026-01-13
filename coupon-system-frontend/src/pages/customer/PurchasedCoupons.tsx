import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  CircularProgress,
  Stack,
  Grid,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ShoppingBagIcon from '@mui/icons-material/ShoppingBag';

import { Coupon } from '../../types/coupon.types';
import { customerApi } from '../../api/customerApi';
import { CouponFilter } from '../../components/coupons/CouponFilter';
import { CouponCard } from '../../components/coupons/CouponCard';
import { CouponDetails } from '../../components/coupons/CouponDetails';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { Footer } from '../../components/common/Footer';

interface FilterState {
  category: number | '';
  maxPrice: number | '';
}

const PurchasedCoupons: React.FC = () => {
  const navigate = useNavigate();

  // State management
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [filteredCoupons, setFilteredCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [filters, setFilters] = useState<FilterState>({
    category: '',
    maxPrice: '',
  });
  const [selectedCoupon, setSelectedCoupon] = useState<Coupon | null>(null);
  const [detailsOpen, setDetailsOpen] = useState<boolean>(false);

  // Fetch purchased coupons on mount
  useEffect(() => {
    fetchPurchasedCoupons();
  }, []);

  // Apply filters whenever filters or coupons change
  useEffect(() => {
    applyFilters();
  }, [coupons, filters]);

  const fetchPurchasedCoupons = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await customerApi.getPurchasedCoupons();
      setCoupons(data);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load purchased coupons';
      setError(errorMessage);
      console.error('Error fetching purchased coupons:', err);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...coupons];

    // Apply category filter
    if (filters.category !== '') {
      filtered = filtered.filter((coupon) => coupon.CATEGORY === filters.category);
    }

    // Apply max price filter
    if (filters.maxPrice !== '') {
      filtered = filtered.filter((coupon) => coupon.price <= filters.maxPrice);
    }

    setFilteredCoupons(filtered);
  };

  const handleFilterChange = (newFilters: FilterState) => {
    setFilters(newFilters);
  };

  const handleCouponClick = (coupon: Coupon) => {
    setSelectedCoupon(coupon);
    setDetailsOpen(true);
  };

  const handleDetailsClose = () => {
    setDetailsOpen(false);
    setSelectedCoupon(null);
  };

  const handleBrowseCoupons = () => {
    navigate('/customer/browse');
  };

  const handleErrorClose = () => {
    setError('');
  };

  // Render empty state
  if (!loading && coupons.length === 0) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <Box component="main" sx={{ flexGrow: 1, py: 8 }}>
          <Container maxWidth="lg">
            <Box
              sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                textAlign: 'center',
                minHeight: '400px',
              }}
            >
              <ShoppingBagIcon
                sx={{
                  fontSize: '80px',
                  color: 'primary.light',
                  marginBottom: 3,
                  opacity: 0.5,
                }}
              />

              <Typography
                variant="h4"
                component="h1"
                sx={{
                  fontWeight: 700,
                  marginBottom: 2,
                  color: 'text.primary',
                }}
              >
                My Purchased Coupons
              </Typography>

              <Typography
                variant="body1"
                sx={{
                  color: 'text.secondary',
                  marginBottom: 4,
                  maxWidth: '500px',
                }}
              >
                You haven't purchased any coupons yet. Start exploring our amazing
                offers and get the best deals!
              </Typography>

              <Button
                variant="contained"
                color="primary"
                size="large"
                onClick={handleBrowseCoupons}
                sx={{
                  paddingX: 4,
                  paddingY: 1.5,
                  fontSize: '1rem',
                }}
              >
                Browse Available Coupons
              </Button>
            </Box>
          </Container>
        </Box>
        <Footer />
      </Box>
    );
  }

  // Render loading state
  if (loading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
        }}
      >
        <LoadingSpinner message="Loading your purchased coupons..." />
      </Box>
    );
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* Main Content */}
      <Box component="main" sx={{ flexGrow: 1, py: 4 }}>
        <Container maxWidth="lg">
          {/* Page Header */}
          <Box sx={{ mb: 4 }}>
            <Typography
              variant="h4"
              component="h1"
              sx={{
                fontWeight: 700,
                marginBottom: 1,
                color: 'text.primary',
              }}
            >
              My Purchased Coupons
            </Typography>

            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ mb: 3 }}
            >
              {filteredCoupons.length} {filteredCoupons.length === 1 ? 'coupon' : 'coupons'} found
            </Typography>
          </Box>

          {/* Error Alert */}
          {error && (
            <ErrorAlert
              message={error}
              severity="error"
              open={!!error}
              onClose={handleErrorClose}
            />
          )}

          {/* Filter Component */}
          <CouponFilter onFilterChange={handleFilterChange} />

          {/* No Results State */}
          {filteredCoupons.length === 0 ? (
            <Box
              sx={{
                textAlign: 'center',
                py: 8,
                borderRadius: 2,
                backgroundColor: '#f9f9f9',
              }}
            >
              <ShoppingBagIcon
                sx={{
                  fontSize: '64px',
                  color: 'primary.light',
                  marginBottom: 2,
                  opacity: 0.5,
                }}
              />

              <Typography
                variant="h6"
                sx={{
                  color: 'text.secondary',
                  marginBottom: 2,
                }}
              >
                No coupons match your filters
              </Typography>

              <Button
                variant="outlined"
                color="primary"
                onClick={() => {
                  setFilters({
                    category: '',
                    maxPrice: '',
                  });
                }}
              >
                Clear Filters
              </Button>
            </Box>
          ) : (
            /* Coupons Grid */
            <Grid container spacing={3}>
              {filteredCoupons.map((coupon) => (
                <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={coupon.id}>
                  <Box
                    data-testid="coupon-card"
                    onClick={() => handleCouponClick(coupon)}
                    sx={{ cursor: 'pointer' }}
                  >
                    <CouponCard
                      coupon={coupon}
                      showActions={false}
                    />
                  </Box>
                </Grid>
              ))}
            </Grid>
          )}
        </Container>
      </Box>

      {/* Coupon Details Modal */}
      <CouponDetails
        coupon={selectedCoupon}
        open={detailsOpen}
        onClose={handleDetailsClose}
      />

      {/* Footer */}
      <Footer />
    </Box>
  );
};

export default PurchasedCoupons;
