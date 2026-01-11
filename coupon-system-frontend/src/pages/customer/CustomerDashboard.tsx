import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Button,
  Alert,
  Grid,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { customerApi } from '../../api/customerApi';
import { Coupon } from '../../types/coupon.types';
import { Navbar } from '../../components/common/Navbar';
import { Footer } from '../../components/common/Footer';
import { CouponCard } from '../../components/coupons/CouponCard';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';

const CustomerDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [purchasedCoupons, setPurchasedCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch purchased coupons on component mount
  useEffect(() => {
    const fetchPurchasedCoupons = async () => {
      try {
        setLoading(true);
        setError(null);
        const coupons = await customerApi.getPurchasedCoupons();
        setPurchasedCoupons(coupons);
      } catch (err) {
        setError('Failed to load your purchased coupons. Please try again later.');
        console.error('Error fetching purchased coupons:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchPurchasedCoupons();
  }, []);

  // Calculate statistics
  const totalCouponsPurchased = purchasedCoupons.length;
  const totalSpent = purchasedCoupons.reduce((sum, coupon) => sum + coupon.price, 0);
  const recentPurchases = purchasedCoupons.slice(0, 4);

  const handleBrowseCoupons = () => {
    navigate('/customer/coupons');
  };

  const handleViewAllPurchases = () => {
    navigate('/customer/purchases');
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <Navbar title="Customer Dashboard" />
        <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <LoadingSpinner message="Loading your dashboard..." />
        </Box>
        <Footer />
      </Box>
    );
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar title="Customer Dashboard" />

      <Box component="main" sx={{ flex: 1, py: 4, backgroundColor: '#f5f5f5' }}>
        <Container maxWidth="lg">
          {/* Welcome Section */}
          <Box sx={{ mb: 4 }}>
            <Typography
              variant="h4"
              component="h1"
              sx={{
                fontWeight: 700,
                mb: 1,
                color: '#1976d2',
              }}
            >
              Welcome, {user?.name}!
            </Typography>
            <Typography variant="body1" color="textSecondary">
              Here's an overview of your coupon purchases and activity.
            </Typography>
          </Box>

          {/* Error Alert */}
          {error && (
            <Box sx={{ mb: 3 }}>
              <Alert severity="error">{error}</Alert>
            </Box>
          )}

          {/* Quick Stats Section */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            {/* Total Coupons Purchased */}
            <Grid size={{ xs: 12, sm: 6, md: 4 }}>
              <Card
                sx={{
                  height: '100%',
                  backgroundColor: '#e3f2fd',
                  border: '1px solid #bbdefb',
                  boxShadow: 1,
                  transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <ShoppingCartIcon
                      sx={{
                        fontSize: 40,
                        color: '#1976d2',
                      }}
                    />
                    <Box>
                      <Typography color="textSecondary" variant="body2" sx={{ fontWeight: 500 }}>
                        Coupons Purchased
                      </Typography>
                      <Typography
                        variant="h5"
                        sx={{
                          fontWeight: 700,
                          color: '#1976d2',
                        }}
                      >
                        {totalCouponsPurchased}
                      </Typography>
                    </Box>
                  </Box>
                  <Typography variant="caption" color="textSecondary">
                    Total coupons in your collection
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            {/* Total Spent */}
            <Grid size={{ xs: 12, sm: 6, md: 4 }}>
              <Card
                sx={{
                  height: '100%',
                  backgroundColor: '#f3e5f5',
                  border: '1px solid #e1bee7',
                  boxShadow: 1,
                  transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <AttachMoneyIcon
                      sx={{
                        fontSize: 40,
                        color: '#7b1fa2',
                      }}
                    />
                    <Box>
                      <Typography color="textSecondary" variant="body2" sx={{ fontWeight: 500 }}>
                        Total Spent
                      </Typography>
                      <Typography
                        variant="h5"
                        sx={{
                          fontWeight: 700,
                          color: '#7b1fa2',
                        }}
                      >
                        ${totalSpent.toFixed(2)}
                      </Typography>
                    </Box>
                  </Box>
                  <Typography variant="caption" color="textSecondary">
                    Total investment in coupons
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            {/* Average Coupon Price */}
            <Grid size={{ xs: 12, sm: 6, md: 4 }}>
              <Card
                sx={{
                  height: '100%',
                  backgroundColor: '#fff3e0',
                  border: '1px solid #ffe0b2',
                  boxShadow: 1,
                  transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <LocalOfferIcon
                      sx={{
                        fontSize: 40,
                        color: '#f57c00',
                      }}
                    />
                    <Box>
                      <Typography color="textSecondary" variant="body2" sx={{ fontWeight: 500 }}>
                        Average Price
                      </Typography>
                      <Typography
                        variant="h5"
                        sx={{
                          fontWeight: 700,
                          color: '#f57c00',
                        }}
                      >
                        ${totalCouponsPurchased > 0 ? (totalSpent / totalCouponsPurchased).toFixed(2) : '0.00'}
                      </Typography>
                    </Box>
                  </Box>
                  <Typography variant="caption" color="textSecondary">
                    Per coupon average
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Recent Purchases Section */}
          <Box sx={{ mb: 4 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography
                variant="h5"
                component="h2"
                sx={{
                  fontWeight: 700,
                  color: '#1976d2',
                }}
              >
                Your Recent Purchases
              </Typography>
              {recentPurchases.length > 0 && (
                <Button
                  variant="text"
                  color="primary"
                  onClick={handleViewAllPurchases}
                  sx={{ textTransform: 'none', fontSize: '0.95rem' }}
                >
                  View All Purchases →
                </Button>
              )}
            </Box>

            {recentPurchases.length > 0 ? (
              <Grid container spacing={3}>
                {recentPurchases.map((coupon) => (
                  <Grid size={{ xs: 12, sm: 6, md: 3 }} key={coupon.id}>
                    <CouponCard
                      coupon={coupon}
                      showActions={false}
                    />
                  </Grid>
                ))}
              </Grid>
            ) : (
              <Card sx={{ backgroundColor: '#f5f5f5', border: '1px solid #e0e0e0' }}>
                <CardContent sx={{ textAlign: 'center', py: 4 }}>
                  <ShoppingCartIcon sx={{ fontSize: 48, color: '#bdbdbd', mb: 2 }} />
                  <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                    No Purchases Yet
                  </Typography>
                  <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                    Start exploring and purchasing coupons to see them here!
                  </Typography>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={handleBrowseCoupons}
                    sx={{ textTransform: 'none' }}
                  >
                    Browse Coupons
                  </Button>
                </CardContent>
              </Card>
            )}
          </Box>

          {/* Action Buttons */}
          {recentPurchases.length > 0 && (
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', mb: 4 }}>
              <Button
                variant="contained"
                color="primary"
                size="large"
                onClick={handleBrowseCoupons}
                sx={{
                  textTransform: 'none',
                  fontSize: '1rem',
                  px: 4,
                }}
              >
                Browse More Coupons
              </Button>
              <Button
                variant="outlined"
                color="primary"
                size="large"
                onClick={handleViewAllPurchases}
                sx={{
                  textTransform: 'none',
                  fontSize: '1rem',
                  px: 4,
                }}
              >
                View All Purchases
              </Button>
            </Box>
          )}
        </Container>
      </Box>

      <Footer />
    </Box>
  );
};

export default CustomerDashboard;
