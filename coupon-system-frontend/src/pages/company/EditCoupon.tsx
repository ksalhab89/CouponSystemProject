import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  AppBar,
  Toolbar,
  CssBaseline,
  Paper,
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { CouponForm } from '../../components/coupons/CouponForm';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { Footer } from '../../components/common/Footer';
import { companyApi } from '../../api/companyApi';
import { Coupon } from '../../types/coupon.types';
import { CouponUpdateRequest } from '../../types/api.types';
import Logo from '../../logo.svg';

interface AlertState {
  message: string;
  severity: 'error' | 'warning' | 'info' | 'success';
}

const EditCoupon: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [coupon, setCoupon] = useState<Coupon | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [alert, setAlert] = useState<AlertState | null>(null);

  useEffect(() => {
    const fetchCoupon = async () => {
      if (!id) {
        navigate('/company/coupons');
        return;
      }

      try {
        setLoading(true);
        // Backend doesn't have GET /company/coupons/{id}, so fetch all and filter
        const allCoupons = await companyApi.getAllCoupons();
        const foundCoupon = allCoupons.find(c => c.id === parseInt(id));

        if (!foundCoupon) {
          throw new Error('Coupon not found');
        }

        setCoupon(foundCoupon);
      } catch (error: any) {
        const errorMessage = error?.response?.data?.message || 'Failed to load coupon';
        setAlert({
          message: errorMessage,
          severity: 'error',
        });
        setTimeout(() => {
          navigate('/company/coupons');
        }, 2000);
      } finally {
        setLoading(false);
      }
    };

    fetchCoupon();
  }, [id, navigate]);

  const handleSubmit = async (data: CouponUpdateRequest) => {
    if (!id) return;

    try {
      setSubmitLoading(true);
      await companyApi.updateCoupon(parseInt(id), data);
      setSubmitLoading(false);
      setAlert({
        message: 'Coupon updated successfully!',
        severity: 'success',
      });

      // Navigate back after delay (give time for Snackbar to show)
      setTimeout(() => {
        navigate('/company/coupons');
      }, 2000);
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to update coupon';
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
      setSubmitLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/company/coupons');
  };

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
        <LoadingSpinner message="Loading coupon..." />
      </Box>
    );
  }

  if (!coupon) {
    return null;
  }

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        backgroundColor: '#fafafa',
      }}
    >
      <CssBaseline />

      {/* Navbar */}
      <AppBar position="static" elevation={1}>
        <Toolbar>
          <Box
            component="img"
            src={Logo}
            alt="Coupon System Logo"
            sx={{ height: 40, marginRight: 2, cursor: 'pointer' }}
            onClick={() => navigate('/company')}
          />
          <Typography
            variant="h6"
            sx={{ flexGrow: 1, fontWeight: 600, cursor: 'pointer' }}
            onClick={() => navigate('/company')}
          >
            Edit Coupon
          </Typography>
          <Button
            color="inherit"
            onClick={() => navigate('/company/coupons')}
            startIcon={<ArrowBackIcon />}
            sx={{
              textTransform: 'none',
              fontSize: '1rem',
            }}
          >
            Back to Coupons
          </Button>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Box sx={{ flex: 1, py: 4 }}>
        <Container maxWidth="md">
          {/* Page Title */}
          <Typography
            variant="h4"
            component="h1"
            sx={{
              fontWeight: 700,
              color: '#333',
              mb: 3,
            }}
          >
            Edit Coupon: {coupon.title}
          </Typography>

          {/* Form Container */}
          <Paper elevation={2} sx={{ p: 4 }}>
            <CouponForm
              coupon={coupon}
              onSubmit={handleSubmit}
              onCancel={handleCancel}
              loading={submitLoading}
            />
          </Paper>
        </Container>
      </Box>

      {/* Success/Error Alert */}
      <ErrorAlert
        message={alert?.message || ''}
        severity={alert?.severity || 'info'}
        open={!!alert}
        onClose={() => setAlert(null)}
      />

      {/* Footer */}
      <Footer />
    </Box>
  );
};

export default EditCoupon;
