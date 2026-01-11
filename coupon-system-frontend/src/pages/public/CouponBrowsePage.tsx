import React, { useState, useEffect } from 'react';
import { Container, Typography, Box, Grid } from '@mui/material';
import Navbar from '../../components/common/Navbar';
import Footer from '../../components/common/Footer';
import { CouponFilter } from '../../components/coupons/CouponFilter';
import { CouponCard } from '../../components/coupons/CouponCard';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { Coupon } from '../../types/coupon.types';
import { publicApi } from '../../api/publicApi';

interface FilterState {
  category: number | '';
  maxPrice: number | '';
}

const CouponBrowsePage: React.FC = () => {
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [filteredCoupons, setFilteredCoupons] = useState<Coupon[]>([]);
  const [filters, setFilters] = useState<FilterState>({
    category: '',
    maxPrice: '',
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [showError, setShowError] = useState<boolean>(false);

  // Fetch coupons based on current filters
  useEffect(() => {
    const fetchCoupons = async () => {
      try {
        setLoading(true);
        setError(null);
        setShowError(false);

        let data: Coupon[] = [];

        // Fetch based on filters
        if (filters.category && filters.maxPrice) {
          // If both filters are active, fetch by category and filter by price in memory
          data = await publicApi.getCouponsByCategory(filters.category as number);
          data = data.filter((coupon) => coupon.price <= (filters.maxPrice as number));
        } else if (filters.category) {
          // Fetch by category only
          data = await publicApi.getCouponsByCategory(filters.category as number);
        } else if (filters.maxPrice) {
          // Fetch by max price only
          data = await publicApi.getCouponsByMaxPrice(filters.maxPrice as number);
        } else {
          // Fetch all coupons
          data = await publicApi.getAllCoupons();
        }

        setCoupons(data);
        setFilteredCoupons(data);
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to load coupons. Please try again later.';
        setError(errorMessage);
        setShowError(true);
        setCoupons([]);
        setFilteredCoupons([]);
      } finally {
        setLoading(false);
      }
    };

    fetchCoupons();
  }, [filters]);

  // Handle filter changes from CouponFilter component
  const handleFilterChange = (newFilters: FilterState) => {
    setFilters(newFilters);
  };

  // Handle coupon purchase action
  const handlePurchaseCoupon = (couponId: number) => {
    // TODO: Implement purchase logic
    console.log(`Purchasing coupon with ID: ${couponId}`);
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
      }}
    >
      {/* Navbar */}
      <Navbar title="Coupon System" />

      {/* Error Alert */}
      {error && (
        <ErrorAlert
          message={error}
          severity="error"
          open={showError}
          onClose={() => setShowError(false)}
        />
      )}

      {/* Main Content */}
      <Box sx={{ flex: 1, py: 4 }}>
        <Container maxWidth="lg">
          {/* Page Title */}
          <Typography
            variant="h3"
            component="h1"
            sx={{
              mb: 4,
              fontWeight: 700,
              color: 'primary.main',
            }}
          >
            Browse Coupons
          </Typography>

          {/* Coupon Filter */}
          <CouponFilter onFilterChange={handleFilterChange} />

          {/* Loading State */}
          {loading && (
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '400px',
              }}
            >
              <LoadingSpinner message="Loading coupons..." />
            </Box>
          )}

          {/* Coupons Grid */}
          {!loading && filteredCoupons.length > 0 && (
            <>
              <Typography
                variant="body1"
                sx={{
                  mb: 2,
                  color: 'text.secondary',
                }}
              >
                Found {filteredCoupons.length} coupon{filteredCoupons.length !== 1 ? 's' : ''}
              </Typography>
              <Grid container spacing={3}>
                {filteredCoupons.map((coupon) => (
                  <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={coupon.id}>
                    <CouponCard
                      coupon={coupon}
                      onPurchase={() => handlePurchaseCoupon(coupon.id)}
                      showActions={true}
                    />
                  </Grid>
                ))}
              </Grid>
            </>
          )}

          {/* No Results State */}
          {!loading && filteredCoupons.length === 0 && (
            <Box
              sx={{
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '400px',
                backgroundColor: '#f5f5f5',
                borderRadius: 2,
                p: 3,
              }}
            >
              <Typography variant="h5" sx={{ mb: 1, color: 'text.secondary' }}>
                No coupons found
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {filters.category || filters.maxPrice
                  ? 'Try adjusting your filters to find more coupons.'
                  : 'Check back later for available coupons.'}
              </Typography>
            </Box>
          )}
        </Container>
      </Box>

      {/* Footer */}
      <Footer copyrightText="Coupon System" />
    </Box>
  );
};

export default CouponBrowsePage;
