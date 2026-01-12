import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Button,
  Paper,
  Stack,
  Chip,
  Alert,
  Grid,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import AddIcon from '@mui/icons-material/Add';
import ViewListIcon from '@mui/icons-material/ViewList';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { Footer } from '../../components/common/Footer';
import Navbar from '../../components/common/Navbar';
import { companyApi } from '../../api/companyApi';
import { Company, Coupon } from '../../types/coupon.types';

interface AlertState {
  message: string;
  severity: 'error' | 'warning' | 'info' | 'success';
}

const CompanyDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [company, setCompany] = useState<Company | null>(null);
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState<AlertState | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [companyDetails, allCoupons] = await Promise.all([
          companyApi.getCompanyDetails(),
          companyApi.getAllCoupons(),
        ]);
        setCompany(companyDetails);
        setCoupons(allCoupons);
      } catch (error) {
        setAlert({
          message: 'Failed to load dashboard data. Please try again.',
          severity: 'error',
        });
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Calculate statistics
  const totalCoupons = coupons.length;
  const activeCoupons = coupons.filter((coupon) => coupon.amount > 0).length;
  const totalValue = coupons.reduce((sum, coupon) => sum + coupon.price * coupon.amount, 0);

  // Get last 4 coupons (most recent)
  const recentCoupons = coupons.slice(0, 4);

  const handleDeleteCoupon = async (couponId: number) => {
    if (!window.confirm('Are you sure you want to delete this coupon?')) {
      return;
    }

    try {
      await companyApi.deleteCoupon(couponId);
      setCoupons(coupons.filter((c) => c.id !== couponId));
      setAlert({
        message: 'Coupon deleted successfully',
        severity: 'success',
      });
    } catch (error) {
      setAlert({
        message: 'Failed to delete coupon',
        severity: 'error',
      });
      console.error('Error deleting coupon:', error);
    }
  };

  const handleCloseAlert = () => {
    setAlert(null);
  };

  if (loading) {
    return (
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          minHeight: '100vh',
          backgroundColor: '#fafafa',
        }}
      >
        <Navbar title="Coupon System" />
        <Box sx={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <LoadingSpinner message="Loading dashboard..." />
        </Box>
      </Box>
    );
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
      {/* Navbar */}
      <Navbar title="Coupon System" />

      {/* Main Content */}
      <Container maxWidth="lg" sx={{ flex: 1, py: 4 }}>
        {/* Page Title */}
        <Typography
          variant="h4"
          component="h1"
          sx={{
            fontWeight: 700,
            mb: 2,
            color: '#333',
          }}
        >
          Company Dashboard
        </Typography>

        {/* Welcome Message */}
        {company && (
          <Alert
            severity="info"
            sx={{
              mb: 3,
              backgroundColor: '#e3f2fd',
              '& .MuiAlert-icon': {
                color: '#1976d2',
              },
            }}
          >
            <Typography variant="body1" sx={{ fontWeight: 500 }}>
              Welcome, {company.name}! 👋
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Manage your coupons and track performance from this dashboard.
            </Typography>
          </Alert>
        )}

        {/* Alert Messages */}
        {alert && (
          <ErrorAlert
            message={alert.message}
            severity={alert.severity}
            open={!!alert}
            onClose={handleCloseAlert}
          />
        )}

        {/* Quick Stats Section */}
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h6"
            sx={{ fontWeight: 600, mb: 2, color: '#333' }}
          >
            Quick Stats
          </Typography>
          <Grid container spacing={2}>
            {/* Total Coupons Stat */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Paper
                sx={{
                  p: 2.5,
                  textAlign: 'center',
                  backgroundColor: '#e3f2fd',
                  borderLeft: '4px solid #1976d2',
                  boxShadow: 'none',
                }}
              >
                <Typography
                  variant="h4"
                  sx={{ fontWeight: 700, color: '#1976d2', mb: 0.5 }}
                >
                  {totalCoupons}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Total Coupons
                </Typography>
              </Paper>
            </Grid>

            {/* Active Coupons Stat */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Paper
                sx={{
                  p: 2.5,
                  textAlign: 'center',
                  backgroundColor: '#f3e5f5',
                  borderLeft: '4px solid #dc004e',
                  boxShadow: 'none',
                }}
              >
                <Typography
                  variant="h4"
                  sx={{ fontWeight: 700, color: '#dc004e', mb: 0.5 }}
                >
                  {activeCoupons}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Active Coupons
                </Typography>
              </Paper>
            </Grid>

            {/* Total Value Stat */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Paper
                sx={{
                  p: 2.5,
                  textAlign: 'center',
                  backgroundColor: '#e8f5e9',
                  borderLeft: '4px solid #4caf50',
                  boxShadow: 'none',
                }}
              >
                <Typography
                  variant="h4"
                  sx={{ fontWeight: 700, color: '#4caf50', mb: 0.5 }}
                >
                  ${totalValue.toFixed(2)}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Total Value
                </Typography>
              </Paper>
            </Grid>

            {/* Company Email Stat */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Paper
                sx={{
                  p: 2.5,
                  textAlign: 'center',
                  backgroundColor: '#fff3e0',
                  borderLeft: '4px solid #ff9800',
                  boxShadow: 'none',
                }}
              >
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: 600,
                    color: '#ff9800',
                    mb: 0.5,
                    wordBreak: 'break-word',
                    fontSize: '0.9rem',
                  }}
                >
                  {company?.email}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Company Email
                </Typography>
              </Paper>
            </Grid>
          </Grid>
        </Box>

        {/* Action Buttons */}
        <Box sx={{ mb: 4 }}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              size="large"
              onClick={() => navigate('/company/create')}
            >
              Create Coupon
            </Button>
            <Button
              variant="outlined"
              color="primary"
              startIcon={<ViewListIcon />}
              size="large"
              onClick={() => navigate('/company/coupons')}
            >
              View All Coupons
            </Button>
          </Stack>
        </Box>

        {/* Recent Coupons Section */}
        <Box>
          <Typography
            variant="h6"
            sx={{ fontWeight: 600, mb: 2, color: '#333' }}
          >
            Recent Coupons
          </Typography>

          {recentCoupons.length === 0 ? (
            <Alert severity="info" sx={{ mb: 2 }}>
              No coupons yet. Create your first coupon to get started!
            </Alert>
          ) : (
            <Grid container spacing={3}>
              {recentCoupons.map((coupon) => {
                const isActive = coupon.amount > 0;
                const endDate = new Date(coupon.endDate);
                const isExpired = endDate < new Date();

                return (
                  <Grid size={{ xs: 12, sm: 6, md: 3 }} key={coupon.id}>
                    <Card
                      sx={{
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        transition: 'transform 0.2s, box-shadow 0.2s',
                        '&:hover': {
                          transform: 'translateY(-4px)',
                          boxShadow: '0 8px 16px rgba(0,0,0,0.1)',
                        },
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

                      {/* Card Content */}
                      <CardContent sx={{ flex: 1 }}>
                        <Typography
                          gutterBottom
                          variant="h6"
                          component="div"
                          sx={{ fontWeight: 600, mb: 1 }}
                        >
                          {coupon.title}
                        </Typography>

                        <Typography
                          variant="body2"
                          color="textSecondary"
                          sx={{ mb: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}
                        >
                          {coupon.description}
                        </Typography>

                        {/* Status Badges */}
                        <Stack direction="row" spacing={1} sx={{ mb: 2, flexWrap: 'wrap', gap: 1 }}>
                          <Chip
                            label={`$${coupon.price.toFixed(2)}`}
                            size="small"
                            color="primary"
                            variant="outlined"
                          />
                          <Chip
                            label={`${coupon.amount} left`}
                            size="small"
                            color={isActive ? 'success' : 'error'}
                            variant="filled"
                          />
                          {isExpired && (
                            <Chip
                              label="Expired"
                              size="small"
                              color="error"
                              variant="filled"
                            />
                          )}
                        </Stack>

                        <Typography variant="caption" color="textSecondary">
                          Ends: {new Date(coupon.endDate).toLocaleDateString()}
                        </Typography>
                      </CardContent>

                      {/* Card Actions */}
                      <CardActions sx={{ pt: 0 }}>
                        <Button
                          size="small"
                          color="primary"
                          startIcon={<EditIcon />}
                          onClick={() => navigate(`/company/coupons/${coupon.id}/edit`)}
                        >
                          Edit
                        </Button>
                        <Button
                          size="small"
                          color="error"
                          startIcon={<DeleteIcon />}
                          onClick={() => handleDeleteCoupon(coupon.id)}
                        >
                          Delete
                        </Button>
                      </CardActions>
                    </Card>
                  </Grid>
                );
              })}
            </Grid>
          )}
        </Box>
      </Container>

      {/* Footer */}
      <Footer copyrightText="Coupon System" />
    </Box>
  );
};

export default CompanyDashboard;
