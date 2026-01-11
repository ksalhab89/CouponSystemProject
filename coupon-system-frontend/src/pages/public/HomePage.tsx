import React, { useEffect, useState } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  Stack,
  useMediaQuery,
  useTheme,
  Chip,
  Link,
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import DescriptionIcon from '@mui/icons-material/Description';
import Navbar from '../../components/common/Navbar';
import Footer from '../../components/common/Footer';
import { CouponGrid } from '../../components/coupons/CouponGrid';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { publicApi, HealthStatus } from '../../api/publicApi';
import { Coupon } from '../../types/coupon.types';

const HomePage: React.FC = () => {
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [alertOpen, setAlertOpen] = useState(false);
  const [healthStatus, setHealthStatus] = useState<HealthStatus | null>(null);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  useEffect(() => {
    const fetchCoupons = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await publicApi.getAllCoupons();
        // Take only the first 6 coupons for featured section
        setCoupons(data.slice(0, 6));
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to load coupons';
        setError(errorMessage);
        setAlertOpen(true);
      } finally {
        setLoading(false);
      }
    };

    const fetchHealthStatus = async () => {
      try {
        const health = await publicApi.getHealthStatus();
        setHealthStatus(health);
      } catch (err) {
        console.error('Failed to fetch health status:', err);
      }
    };

    fetchCoupons();
    fetchHealthStatus();
  }, []);

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

      {/* Main Content */}
      <Box sx={{ flex: 1 }}>
        {/* Hero Section */}
        <Box
          sx={{
            background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
            color: 'white',
            py: { xs: 8, md: 12 },
            textAlign: 'center',
            mb: { xs: 6, md: 10 },
          }}
        >
          <Container maxWidth="md">
            <Stack spacing={3} alignItems="center">
              {/* Hero Title */}
              <Typography
                variant="h1"
                sx={{
                  fontSize: { xs: '2.5rem', md: '3.5rem' },
                  fontWeight: 700,
                  lineHeight: 1.2,
                }}
              >
                Discover Amazing Deals
              </Typography>

              {/* Hero Subtitle */}
              <Typography
                variant="h5"
                sx={{
                  fontSize: { xs: '1rem', md: '1.3rem' },
                  fontWeight: 300,
                  maxWidth: '600px',
                  opacity: 0.95,
                }}
              >
                Browse thousands of exclusive coupons from top brands and save big on your
                favorite products and services
              </Typography>

              {/* CTA Button */}
              <Button
                variant="contained"
                size="large"
                component={RouterLink}
                to="/browse"
                endIcon={<ArrowForwardIcon />}
                sx={{
                  backgroundColor: 'white',
                  color: theme.palette.primary.main,
                  fontWeight: 700,
                  px: 4,
                  py: 1.5,
                  fontSize: '1rem',
                  '&:hover': {
                    backgroundColor: '#f0f0f0',
                  },
                  mt: 2,
                }}
              >
                Browse All Coupons
              </Button>
            </Stack>
          </Container>
        </Box>

        {/* Featured Coupons Section */}
        <Container maxWidth="lg" sx={{ pb: { xs: 6, md: 10 } }}>
          <Stack spacing={4}>
            {/* Section Header */}
            <Box>
              <Typography
                variant="h2"
                sx={{
                  fontSize: { xs: '2rem', md: '2.5rem' },
                  fontWeight: 700,
                  mb: 2,
                }}
              >
                Featured Coupons
              </Typography>
              <Typography
                variant="body1"
                color="textSecondary"
                sx={{
                  fontSize: '1.1rem',
                  maxWidth: '600px',
                }}
              >
                Check out our handpicked selection of the best deals available right now
              </Typography>
            </Box>

            {/* Loading State */}
            {loading && (
              <Box sx={{ py: 8 }}>
                <LoadingSpinner message="Loading featured coupons..." />
              </Box>
            )}

            {/* Coupons Grid */}
            {!loading && (
              <CouponGrid
                coupons={coupons}
                showActions={false}
                loading={false}
              />
            )}

            {/* View All Button */}
            {!loading && coupons.length > 0 && (
              <Box sx={{ textAlign: 'center', mt: 4 }}>
                <Button
                  variant="outlined"
                  size="large"
                  component={RouterLink}
                  to="/browse"
                  sx={{
                    px: 6,
                    py: 1.5,
                    fontSize: '1rem',
                  }}
                >
                  View All Coupons
                </Button>
              </Box>
            )}
          </Stack>
        </Container>
      </Box>

      {/* Error Alert */}
      <ErrorAlert
        message={error || ''}
        severity="error"
        open={alertOpen}
        onClose={() => setAlertOpen(false)}
      />

      {/* System Status Bar */}
      <Box
        sx={{
          backgroundColor: '#f8f9fa',
          borderTop: '1px solid #e0e0e0',
          py: 2,
        }}
      >
        <Container maxWidth="lg">
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            alignItems="center"
            justifyContent="space-between"
          >
            {/* Database Status */}
            <Stack direction="row" spacing={1} alignItems="center">
              <Typography variant="body2" color="textSecondary">
                System Status:
              </Typography>
              {healthStatus ? (
                <Chip
                  icon={
                    healthStatus.components.db.status === 'UP' ? (
                      <CheckCircleIcon />
                    ) : (
                      <ErrorIcon />
                    )
                  }
                  label={`Database: ${healthStatus.components.db.status}`}
                  color={healthStatus.components.db.status === 'UP' ? 'success' : 'error'}
                  size="small"
                  sx={{ fontWeight: 600 }}
                />
              ) : (
                <Chip
                  label="Checking..."
                  size="small"
                  variant="outlined"
                />
              )}
            </Stack>

            {/* Swagger Documentation Link */}
            <Stack direction="row" spacing={1} alignItems="center">
              <DescriptionIcon fontSize="small" color="action" />
              <Link
                href="http://localhost:8080/swagger-ui/index.html"
                target="_blank"
                rel="noopener noreferrer"
                underline="hover"
                sx={{
                  color: theme.palette.primary.main,
                  fontWeight: 500,
                  fontSize: '0.875rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 0.5,
                }}
              >
                API Documentation (Swagger)
              </Link>
            </Stack>
          </Stack>
        </Container>
      </Box>

      {/* Footer */}
      <Footer copyrightText="Coupon System" />
    </Box>
  );
};

export default HomePage;
