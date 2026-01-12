import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Stack,
  AppBar,
  Toolbar,
  Link,
  CssBaseline,
  Grid,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';
import { Coupon } from '../../types/coupon.types';
import { CouponCard } from '../../components/coupons/CouponCard';
import { CouponFilter } from '../../components/coupons/CouponFilter';
import { CouponForm } from '../../components/coupons/CouponForm';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { Footer } from '../../components/common/Footer';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { companyApi } from '../../api/companyApi';
import { CouponUpdateRequest } from '../../types/api.types';
import Logo from '../../logo.svg';

interface AlertState {
  open: boolean;
  message: string;
  severity: 'error' | 'warning' | 'info' | 'success';
}

interface FilterState {
  category: number | '';
  maxPrice: number | '';
}

const MyCoupons: React.FC = () => {
  const navigate = useNavigate();

  // State management
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [filteredCoupons, setFilteredCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState<FilterState>({
    category: '',
    maxPrice: '',
  });
  const [alert, setAlert] = useState<AlertState>({
    open: false,
    message: '',
    severity: 'success',
  });

  // Dialog states
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedCoupon, setSelectedCoupon] = useState<Coupon | null>(null);
  const [formLoading, setFormLoading] = useState(false);

  // Fetch coupons on component mount
  useEffect(() => {
    fetchCoupons();
  }, []);

  // Apply filters whenever coupons or filters change
  useEffect(() => {
    applyFilters();
  }, [coupons, filters]);

  const fetchCoupons = async () => {
    try {
      setLoading(true);
      const data = await companyApi.getAllCoupons();
      setCoupons(data);
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to load coupons';
      showAlert(errorMessage, 'error');
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let result = [...coupons];

    // Filter by category
    if (filters.category !== '') {
      result = result.filter((coupon) => coupon.CATEGORY === filters.category);
    }

    // Filter by max price
    if (filters.maxPrice !== '') {
      result = result.filter((coupon) => coupon.price <= filters.maxPrice);
    }

    setFilteredCoupons(result);
  };

  const handleFilterChange = (newFilters: FilterState) => {
    setFilters(newFilters);
  };

  const handleEditClick = (coupon: Coupon) => {
    setSelectedCoupon(coupon);
    setEditDialogOpen(true);
  };

  const handleDeleteClick = (coupon: Coupon) => {
    setSelectedCoupon(coupon);
    setDeleteDialogOpen(true);
  };

  const handleEditSubmit = async (formData: Omit<Coupon, 'id' | 'companyID'>) => {
    if (!selectedCoupon) return;

    try {
      setFormLoading(true);
      const updateData: CouponUpdateRequest = {
        CATEGORY: formData.CATEGORY,
        title: formData.title,
        description: formData.description,
        startDate: formData.startDate,
        endDate: formData.endDate,
        amount: formData.amount,
        price: formData.price,
        image: formData.image,
      };

      const updatedCoupon = await companyApi.updateCoupon(selectedCoupon.id, updateData);

      // Update coupons list
      setCoupons((prev) =>
        prev.map((coupon) => (coupon.id === selectedCoupon.id ? updatedCoupon : coupon))
      );

      showAlert('Coupon updated successfully!', 'success');
      setEditDialogOpen(false);
      setSelectedCoupon(null);
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to update coupon';
      showAlert(errorMessage, 'error');
    } finally {
      setFormLoading(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!selectedCoupon) return;

    try {
      setFormLoading(true);
      await companyApi.deleteCoupon(selectedCoupon.id);

      // Update coupons list
      setCoupons((prev) => prev.filter((coupon) => coupon.id !== selectedCoupon.id));

      showAlert('Coupon deleted successfully!', 'success');
      setDeleteDialogOpen(false);
      setSelectedCoupon(null);
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to delete coupon';
      showAlert(errorMessage, 'error');
    } finally {
      setFormLoading(false);
    }
  };

  const showAlert = (message: string, severity: AlertState['severity']) => {
    setAlert({
      open: true,
      message,
      severity,
    });
  };

  const handleCloseAlert = () => {
    setAlert({ ...alert, open: false });
  };

  const handleCloseEditDialog = () => {
    setEditDialogOpen(false);
    setSelectedCoupon(null);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setSelectedCoupon(null);
  };

  const handleCreateCoupon = () => {
    navigate('/company/create');
  };

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
      <AppBar position="static" elevation={2}>
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
            Coupon System
          </Typography>
          <Link
            onClick={() => navigate('/company')}
            sx={{
              color: 'white',
              textDecoration: 'none',
              marginRight: 2,
              fontSize: '1rem',
              cursor: 'pointer',
              '&:hover': {
                textDecoration: 'underline',
              },
            }}
          >
            Dashboard
          </Link>
          <Link
            onClick={() => navigate('/')}
            sx={{
              color: 'white',
              textDecoration: 'none',
              fontSize: '1rem',
              cursor: 'pointer',
              '&:hover': {
                textDecoration: 'underline',
              },
            }}
          >
            Logout
          </Link>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Box component="main" sx={{ flex: 1, py: 4 }}>
        <Container maxWidth="lg">
          {/* Page Header */}
          <Box sx={{ mb: 4 }}>
            <Typography
              variant="h3"
              component="h1"
              sx={{
                fontWeight: 700,
                color: 'text.primary',
                mb: 2,
              }}
            >
              My Coupons
            </Typography>
            <Typography
              variant="body1"
              color="text.secondary"
              sx={{ mb: 3 }}
            >
              Manage your company's coupons. Create, edit, and delete coupons to engage with your customers.
            </Typography>

            {/* Create Button */}
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              onClick={handleCreateCoupon}
              sx={{
                px: 3,
                py: 1,
                fontWeight: 600,
              }}
            >
              Create New Coupon
            </Button>
          </Box>

          {/* Filter Section */}
          <CouponFilter onFilterChange={handleFilterChange} />

          {/* Loading State */}
          {loading ? (
            <LoadingSpinner />
          ) : (
            <>
              {/* Empty State */}
              {filteredCoupons.length === 0 ? (
                <Box
                  sx={{
                    textAlign: 'center',
                    py: 8,
                    px: 3,
                    backgroundColor: 'white',
                    borderRadius: 2,
                    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
                  }}
                >
                  <Typography variant="h6" color="text.secondary" sx={{ mb: 2 }}>
                    {coupons.length === 0
                      ? 'No coupons yet. Create your first coupon to get started!'
                      : 'No coupons match the selected filters.'}
                  </Typography>
                  {coupons.length === 0 && (
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={handleCreateCoupon}
                      startIcon={<AddIcon />}
                    >
                      Create Coupon
                    </Button>
                  )}
                </Box>
              ) : (
                <>
                  {/* Results Count */}
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 3 }}
                  >
                    Showing {filteredCoupons.length} of {coupons.length} coupon
                    {coupons.length !== 1 ? 's' : ''}
                  </Typography>

                  {/* Coupons Grid */}
                  <Grid container spacing={3}>
                    {filteredCoupons.map((coupon) => (
                      <Grid size={{ xs: 12, sm: 6, md: 4 }} key={coupon.id}>
                        <CouponCard
                          coupon={coupon}
                          onEdit={() => handleEditClick(coupon)}
                          onDelete={() => handleDeleteClick(coupon)}
                          showActions={true}
                        />
                      </Grid>
                    ))}
                  </Grid>
                </>
              )}
            </>
          )}
        </Container>
      </Box>

      {/* Edit Dialog */}
      <Dialog
        open={editDialogOpen}
        onClose={handleCloseEditDialog}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: 2,
          }
        }}
      >
        <DialogTitle sx={{ fontWeight: 600 }}>
          Edit Coupon
        </DialogTitle>
        <DialogContent sx={{ pt: 3 }}>
          {selectedCoupon && (
            <CouponForm
              coupon={selectedCoupon}
              onSubmit={handleEditSubmit}
              onCancel={handleCloseEditDialog}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={handleCloseDeleteDialog}
        maxWidth="xs"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: 2,
          }
        }}
      >
        <DialogTitle sx={{ fontWeight: 600 }}>
          Delete Coupon
        </DialogTitle>
        <DialogContent>
          <Typography sx={{ mt: 2 }}>
            Are you sure you want to delete the coupon{' '}
            <strong>"{selectedCoupon?.title}"</strong>? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2, gap: 1 }}>
          <Button
            onClick={handleCloseDeleteDialog}
            variant="outlined"
            disabled={formLoading}
          >
            Cancel
          </Button>
          <Button
            onClick={handleConfirmDelete}
            variant="contained"
            color="error"
            disabled={formLoading}
            startIcon={formLoading ? <CircularProgress size={20} /> : undefined}
          >
            {formLoading ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Alert */}
      {alert.open && (
        <ErrorAlert
          message={alert.message}
          severity={alert.severity}
          open={alert.open}
          onClose={handleCloseAlert}
        />
      )}

      {/* Footer */}
      <Footer />
    </Box>
  );
};

export default MyCoupons;
